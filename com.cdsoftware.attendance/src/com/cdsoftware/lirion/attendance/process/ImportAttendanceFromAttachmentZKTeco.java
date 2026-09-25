/**********************************************************************
 * This file is part of iDempiere ERP Open Source                      *
 * http://www.idempiere.org                                            *
 *                                                                     *
 * Copyright (C) Contributors                                          *
 *                                                                     *
 * This program is free software; you can redistribute it and/or       *
 * modify it under the terms of the GNU General Public License         *
 * as published by the Free Software Foundation; either version 2      *
 * of the License, or (at your option) any later version.              *
 *                                                                     *
 * This program is distributed in the hope that it will be useful,     *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of      *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the        *
 * GNU General Public License for more details.                        *
 *                                                                     *
 * You should have received a copy of the GNU General Public License   *
 * along with this program; if not, write to the Free Software         *
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,          *
 * MA 02110-1301, USA.                                                 *
 *                                                                     *
 * Contributors:                                                       *
 * - Casa del Software                                                 *
 * ********************************************************************/
package com.cdsoftware.lirion.attendance.process;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.compiere.model.MAttachment;
import org.compiere.model.MAttachmentEntry;
import org.compiere.model.MBPartner;
import org.compiere.model.MRefList;
import org.compiere.model.Query;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;

import com.cdsoftware.lirion.attendance.model.MGH_Shifts;
import com.cdsoftware.lirion.attendance.model.MGH_ShiftsLine;
import com.cdsoftware.lirion.attendance.model.MHR_Attendance;
import com.cdsoftware.lirion.attendance.model.MHR_AttendanceLine;

/**
 * Server process to import attendance markings from a ZKTeco CSV file attached to the record.
 * It identifies Business Partners by matching their code against HR_ClockCode, taxid, or value.
 * The process sorts markings by employee and time, then reconciles them into up to 4 potential 
 * clock-in/out times per day.
 * 
 * @author Casa del Software
 * @version 1.0
 */
@org.adempiere.base.annotation.Process
public class ImportAttendanceFromAttachmentZKTeco extends SvrProcess {
    StringBuilder usersNotFoundList = new StringBuilder();
    String p_DateTimeFormat = null;
    String p_DateFormat = null;
    boolean p_HasHeader = false;
    private int RECORD_ID;

    /**
     * Reads the process parameters required for CSV parsing.
     * 
     * Parameters:
     * - DateTimeFormat: Format string for parsing timestamps (e.g., "MM/dd/yy hh:mm a").
     * - DateFormat: Format string for identifying the day part.
     * - HasHeader: Boolean flag to skip the first line of the CSV.
     */
    @Override
    protected void prepare() {
        ProcessInfoParameter[] parameters = getParameter();
        for (ProcessInfoParameter para : parameters) {
            String name = para.getParameterName();
            if (para.getParameter() == null)
                ;
            else if (name.equals("DateTimeFormat"))
                p_DateTimeFormat = para.getParameterAsString();
            else if (name.equals("DateFormat"))
                p_DateFormat = para.getParameterAsString();
            else if (name.equals("HasHeader"))
                p_HasHeader = para.getParameterAsBoolean();
        }
        RECORD_ID = getRecord_ID();
    }

    /**
     * Entry point for the process execution. Triggers the attendance writing logic.
     * 
     * @return null or process message.
     * @throws Exception if processing fails.
     */
    @Override
    protected String doIt() throws Exception {
        return writeAttendance();
    }

    /**
     * Main logic to read the attachment, parse the CSV, sort markings, and 
     * create/update attendance lines.
     * 
     * @return status message.
     * @throws Exception if an error occurs during file reading or DB operations.
     */
    public String writeAttendance() throws Exception {
        MHR_Attendance attendance = new MHR_Attendance(getCtx(), RECORD_ID, get_TrxName());
        MAttachment attachment = attendance.getAttachment();
        Timestamp time1 = null, time2 = null, time3 = null, time4 = null;
        String emp = "", cvsSplitBy = ",";
        int alID = 0, i = 1;

        if (attachment == null) {
            return "@Error@Please attach the attendance file before running the process";
        }
        MAttachmentEntry entry = attachment.getEntry(0);
        if (entry == null) {
            return "@Error@Please attach the attendance file before running the process";
        }
        File csvFile = entry.getFile();
        FileReader fileReader = new FileReader(csvFile);

        try (BufferedReader br = new BufferedReader(fileReader)) {
            int countlines = 0;
            SimpleDateFormat dateFormat = new SimpleDateFormat(p_DateFormat, Locale.US);
            SimpleDateFormat dateTimeFormat = new SimpleDateFormat(p_DateTimeFormat, Locale.US);
            String inputLine;
            ArrayList<ValueDatePair> valueDatelist = new ArrayList<>();

            // Necesitamos ordenar el archivo por empleado y fecha
            Comparator<ValueDatePair> compareByName = Comparator
                    .comparing(ValueDatePair::getValue)
                    .thenComparing(ValueDatePair::getTime);

            while ((inputLine = br.readLine()) != null) {
                // Ignorando la línea de encabezado
                if (countlines == 0 && p_HasHeader) {
                    countlines++;
                    continue;
                }
                if (inputLine.length() == 0)
                    continue;
                if (inputLine.startsWith(","))
                    continue;

                // Elimina las comillas dobles adicionales de la línea
                inputLine = inputLine.replace("\"", "");

                String[] csvLine = inputLine.split(cvsSplitBy);
                String formattedDate = csvLine[1].replace("a.m.", "AM").replace("p.m.", "PM");

                // Intentar parsear la fecha después de limpiar las comillas dobles
                Date parsedDateTime = dateTimeFormat.parse(formattedDate);
                valueDatelist.add(new ValueDatePair(csvLine[0], parsedDateTime));
                countlines++;
            }
            fileReader.close();

            List<ValueDatePair> sortedvalueDatelist = valueDatelist.stream()
                    .sorted(compareByName)
                    .collect(Collectors.toList());

            String lastnotfoundbp = "";
            int count = 0;
            String lastDay = ""; // Variable para guardar el último día procesado
            for (ValueDatePair line : sortedvalueDatelist) {
                count++;
                if (line.getValue().length() == 0)
                    break;
                Date parsedDateTime, parsedDate;
                parsedDateTime = line.getTime();
                parsedDate = dateFormat.parse(dateFormat.format(parsedDateTime));
                String currentDay = dateFormat.format(parsedDate); // Convertir la fecha a un formato de cadena para comparación

                // Filtra las marcas del empleado actual en el día actual
                List<ValueDatePair> employeeDayMarks = sortedvalueDatelist.stream()
                        .filter(d -> d.getValue().equals(line.getValue()) && dateFormat.format(d.getTime()).equals(currentDay))
                        .collect(Collectors.toList());

                if (!lastDay.equals(currentDay) || !emp.equals(line.getValue().trim())) {
                    i = 1;
                    MHR_AttendanceLine al = new MHR_AttendanceLine(getCtx(), 0, get_TrxName());
                    al.setHR_Attendance_ID(attendance.get_ID());
                    StringBuilder whereclause = new StringBuilder();
                    whereclause.append("REPLACE (trim(COALESCE(HR_ClockCode,taxid,value)), '-', '')=?");
                    MBPartner employed = new Query(getCtx(), MBPartner.Table_Name,
                            whereclause.toString(), get_TrxName()).setParameters(line.getValue()).first();
                    if (employed == null) {
                        if (!lastnotfoundbp.equals(line.getValue())) {
                            lastnotfoundbp = line.getValue();
                            usersNotFoundList.append(line.getValue() + ",");
                            log.warning("No se encuentra el empleado " + line.getValue());
                        }
                        continue;
                    }
                    al.setC_BPartner_ID(employed.getC_BPartner_ID());
                    al.setWeekDay(getWeekDayValue(parsedDate));
                    al.setAttendanceDate(new Timestamp(parsedDate.getTime()));
                    time1 = new Timestamp(parsedDateTime.getTime());
                    al.setTime1(time1);
                    al.saveEx();
                    this.statusUpdate("Procesando: " + count + "/" + sortedvalueDatelist.size() + " " + employed.getValue() + " " + employed.getName() + " " + al.getAttendanceDate());
                    lastDay = currentDay; // Actualiza el último día procesado
                    emp = line.getValue();
                    alID = al.get_ID();
                } else {
                    MHR_AttendanceLine al = new MHR_AttendanceLine(getCtx(), alID, get_TrxName());
                    if (line.getTime() != null) {
                        if (employeeDayMarks.size() == 2) {
                            // Si solo hay dos marcas de tiempo, la segunda se asigna a time4
                            time4 = new Timestamp(parsedDateTime.getTime());
                            al.setTime4(time4);
                            BigDecimal QtyOfHours2 = BigDecimal.ZERO;
                            if (time1 != null && time4 != null) {
                                QtyOfHours2 = BigDecimal.valueOf((time4.getTime() - time1.getTime()) / (1000 * 60)).divide(BigDecimal.valueOf(60), RoundingMode.HALF_EVEN);
                            }
                            al.setQtyOfHours2(QtyOfHours2);
                            al.setTotalQtyOfHours(al.getTotalQtyOfHours().add(QtyOfHours2));
                        } else if (i == 2) {
                            // Si hay más de dos marcas, la segunda se asigna a time2
                            time2 = new Timestamp(parsedDateTime.getTime());
                            al.setTime2(time2);
                            BigDecimal QtyOfHours1 = BigDecimal.ZERO;
                            if (time1 != null && time2 != null) {
                                QtyOfHours1 = BigDecimal.valueOf((time2.getTime() - time1.getTime()) / (1000 * 60)).divide(BigDecimal.valueOf(60), RoundingMode.HALF_EVEN);
                            }
                            al.setQtyOfHours1(QtyOfHours1);
                            al.setTotalQtyOfHours(al.getTotalQtyOfHours().add(QtyOfHours1));
                        } else if (i == 3) {
                            time3 = new Timestamp(parsedDateTime.getTime());
                            al.setTime3(time3);
                        } else if (i >= 4) {
                            time4 = new Timestamp(parsedDateTime.getTime());
                            al.setTime4(time4);
                            BigDecimal QtyOfHours2 = BigDecimal.ZERO;
                            if (time3 != null && time4 != null) {
                                QtyOfHours2 = BigDecimal.valueOf((time4.getTime() - time3.getTime()) / (1000 * 60)).divide(BigDecimal.valueOf(60), RoundingMode.HALF_EVEN);
                            }
                            al.setQtyOfHours2(QtyOfHours2);
                            al.setTotalQtyOfHours(al.getTotalQtyOfHours().add(QtyOfHours2));
                        }
                    }
                    al.saveEx();
                }
                i++;
                if (i % 5000 == 0)
                    commitEx();
            }
            if (usersNotFoundList.length() > 0) {
                attendance.set_ValueOfColumn("Description", "Usuarios no encontrados: " + usersNotFoundList);
                attendance.saveEx();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected String getWeekDayValue(Date WeekDayStr) {
        Timestamp time = new Timestamp(WeekDayStr.getTime());
        LocalDateTime attendancedateaux = time.toLocalDateTime();
        List<MRefList> reflist = new Query(getCtx(), MRefList.Table_Name, "AD_Reference_ID=?", get_TrxName()).setParameters(167).list();

        for (MRefList ref : reflist) {
            if (ref.getName().trim().compareToIgnoreCase(attendancedateaux.getDayOfWeek().toString()) == 0)
                return ref.getValue();
        }
        return null;
    }

    public class ValueDatePair {
        public ValueDatePair(String string, Date parsedDateTime) {
            value = string;
            time = parsedDateTime;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public Date getTime() {
            return time;
        }

        public void setTime(Date time) {
            this.time = time;
        }

        public String value;
        public Date time;
    }

    protected BigDecimal getDifference(MHR_AttendanceLine attendanceline, MGH_Shifts pShift) {
        String WeekDay = attendanceline.getWeekDay();
        if (WeekDay.equals(null))
            return BigDecimal.ZERO;

        MGH_ShiftsLine shiftline = new Query(getCtx(), MGH_ShiftsLine.Table_Name, "GH_Shifts_ID=? AND WeekDay=?", get_TrxName()).setParameters(pShift.get_ID(), WeekDay).first();

        LocalDateTime shiftTime1 = prepareTime(attendanceline.getAttendanceDate(), shiftline.getTime1());
        LocalDateTime shiftTime2 = prepareTime(attendanceline.getAttendanceDate(), shiftline.getTime2());
        LocalDateTime shiftTime3 = prepareTime(attendanceline.getAttendanceDate(), shiftline.getTime3());
        LocalDateTime shiftTime4 = prepareTime(attendanceline.getAttendanceDate(), shiftline.getTime4());
        LocalDateTime shiftTime1WithoutTolerance = prepareTime(attendanceline.getAttendanceDate(), shiftline.getTime1());
        LocalDateTime shiftTime2WithoutTolerance = prepareTime(attendanceline.getAttendanceDate(), shiftline.getTime2());
        LocalDateTime shiftTime3WithoutTolerance = prepareTime(attendanceline.getAttendanceDate(), shiftline.getTime3());
        LocalDateTime shiftTime4WithoutTolerance = prepareTime(attendanceline.getAttendanceDate(), shiftline.getTime4());

        LocalDateTime attendanceTime1 = prepareTime(attendanceline.getAttendanceDate(), attendanceline.getTime1());
        attendanceTime1 = (attendanceTime1 == null ? shiftTime1 : attendanceTime1);
        LocalDateTime attendanceTime2 = prepareTime(attendanceline.getAttendanceDate(), attendanceline.getTime2());
        attendanceTime2 = (attendanceTime2 == null ? attendanceTime1 : attendanceTime2);
        LocalDateTime attendanceTime3 = prepareTime(attendanceline.getAttendanceDate(), attendanceline.getTime3());
        LocalDateTime attendanceTime4 = prepareTime(attendanceline.getAttendanceDate(), attendanceline.getTime4());

        if (shiftTime1 == null || shiftTime2 == null || shiftTime3 == null || shiftTime4 == null)
            return BigDecimal.ZERO;

        shiftTime1 = shiftTime1.plusMinutes(shiftline.getTolerance().longValue());
        shiftTime4 = shiftTime4.minusMinutes(shiftline.getTolerance().longValue());
        shiftTime2 = shiftTime2.minusMinutes(shiftline.getRestTolerance().longValue());
        shiftTime3 = shiftTime3.plusMinutes(shiftline.getRestTolerance().longValue());

        Duration duration = Duration.between(shiftTime1WithoutTolerance, attendanceTime1);

        double diffTime1 = duration.toMinutes();
        if (diffTime1 <= shiftline.getTolerance().doubleValue())
            duration = Duration.between(shiftTime1, attendanceTime1);

        diffTime1 = diffTime1 < 0.0 ? 0.0 : diffTime1;
        double diffTime2 = 0.0;
        double diffTime3 = 0.0;
        double diffTime4 = 0.0;
        if (attendanceline.getAuthorizationType() != null && "LC".compareTo(attendanceline.getAuthorizationType()) != 0) {
            shiftTime4 = shiftTime1WithoutTolerance.plusHours(8);
        }
        LocalDateTime attendanceTimeAux = shiftTime3WithoutTolerance;
        duration = Duration.between(attendanceTime2, shiftTime2WithoutTolerance);
        diffTime2 = duration.toMinutes();
        if (diffTime2 <= shiftline.getRestTolerance().doubleValue())
            duration = Duration.between(attendanceTime2, shiftTime2);

        diffTime2 = diffTime2 < 0.0 ? 0.0 : diffTime2;

        if (attendanceTime3 != null) {
            duration = Duration.between(shiftTime3WithoutTolerance, attendanceTime3);
            diffTime3 = duration.toMinutes();
            if (diffTime3 <= shiftline.getRestTolerance().doubleValue())
                duration = Duration.between(shiftTime3, attendanceTime3);

            diffTime3 = diffTime3 < 0.0 ? 0.0 : diffTime3;
        } else if (attendanceTime2.compareTo(shiftTime3) > 0) {
            attendanceTimeAux = attendanceTime2;
        }

        if (attendanceTime4 != null) {
            duration = Duration.between(attendanceTime4, shiftTime4);
            diffTime4 = duration.toMinutes();
            diffTime4 = diffTime4 < 0.0 ? 0.0 : diffTime4;
        }

        if (attendanceTime3 == null && attendanceTime4 == null && attendanceTimeAux.compareTo(shiftTime4) < 0) {
            duration = Duration.between(attendanceTimeAux, shiftTime4WithoutTolerance);
            diffTime4 = duration.toMinutes();
        }

        attendanceline.setQtyMinutesDifference1(BigDecimal.valueOf(diffTime1 + diffTime2));
        attendanceline.setQtyMinutesDifference2(BigDecimal.valueOf(diffTime3 + diffTime4));
        attendanceline.saveEx();

        BigDecimal diference = BigDecimal.valueOf((diffTime1 + diffTime2 + diffTime3 + diffTime4) / 60.0).setScale(2, RoundingMode.HALF_UP);
        log.severe("total" + String.valueOf(diffTime1 + diffTime2 + diffTime3 + diffTime4));

        return (diference.compareTo(BigDecimal.ZERO) > 0) ? diference : BigDecimal.ZERO;
    }

    private LocalDateTime prepareTime(Timestamp attendanceDate, Timestamp time1) {
        if (attendanceDate == null)
            return null;
        if (time1 == null)
            return null;

        LocalDateTime aux = time1.toLocalDateTime();
        LocalDateTime time = attendanceDate.toLocalDateTime();
        time = time.plusHours(aux.getHour());
        time = time.plusMinutes(aux.getMinute());
        return time;
    }
}
