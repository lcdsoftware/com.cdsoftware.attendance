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
 **********************************************************************/
package com.cdsoftware.lirion.attendance.process;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.compiere.model.MAttachment;
import org.compiere.model.MAttachmentEntry;
import org.compiere.model.MBPartner;
import org.compiere.model.MRefList;
import org.compiere.model.Query;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;

import com.cdsoftware.lirion.attendance.model.MGH_ShiftsLine;
import com.cdsoftware.lirion.attendance.model.MHR_Attendance;
import com.cdsoftware.lirion.attendance.model.MHR_AttendanceLine;
import com.cdsoftware.lirion.attendance.model.MMarking;

/**
 * Server process to import attendance records from a CSV file attached to the current record.
 * This process is intended to be run from the Attendance window. It reads the CSV file
 * attached to the record, identifies employees by their search key (Value), and
 * generates the corresponding attendance lines.
 * 
 * Business Logic:
 * - Retrieves the first attachment entry from the current MHR_Attendance record.
 * - Identifies BPartners using their Value (Search Key).
 * - Handles split shifts: if multiple markings exist for the same day and employee, 
 *   it populates Time3 and Time4 on the existing line.
 * - Calculates QtyOfHours1 and QtyOfHours2 based on time intervals.
 * - Supports automatic completion with shift times if "Complete with Shift" is enabled on the header.
 * 
 * CSV Structure:
 * [1] BPartner Value (Employee Code)
 * [2] Weekday Name (e.g., "Lunes")
 * [3] Date (dd/MM/yyyy)
 * [4] Start Time (HH:mm:ss)
 * [6] End Time (HH:mm:ss)
 * 
 * @author Casa del Software
 */
@org.adempiere.base.annotation.Process
public class ImportAttendanceFromAttachment extends SvrProcess {

	/** Attendance Record ID identified from the current context */
	private int RECORD_ID;

	/**
	 * Prepares the process by identifying the current record ID from the UI context.
	 * This process does not use external parameters.
	 */
	@Override
	protected void prepare() {
		ProcessInfoParameter[] parameters = getParameter();
		for (ProcessInfoParameter para : parameters) {
			// No external parameters used in this process
		}
		RECORD_ID = getRecord_ID();
	}

	/**
	 * Executes the attendance import logic from an attached CSV file.
	 * 
	 * Logic flow:
	 * 1. Loads the current MHR_Attendance header.
	 * 2. Fetches the first attachment entry (CSV file).
	 * 3. Parses CSV lines, identifying employees and mapping dates/times.
	 * 4. Creates MHR_AttendanceLine records.
	 * 5. If a second marking line is found for the same employee/date, it updates 
	 *    the previously created record (split shift handling).
	 * 
	 * @return null on success, or an error message prefixed with @Error@.
	 * @throws Exception if an error occurs during parsing or persistence.
	 */
	@Override
	protected String doIt() throws Exception {
		MHR_Attendance attendance = new MHR_Attendance(getCtx(), RECORD_ID, get_TrxName());

		MAttachment attachment = attendance.getAttachment();
		if (attachment == null) {
			return "@Error@Please attach the attendance file before running the process";
		}

		MAttachmentEntry entry = attachment.getEntry(0);
		if (entry == null) {
			return "@Error@Please attach the attendance file before running the process";
		}
		File csvFile = entry.getFile();

		String line = "";
		String cvsSplitBy = ",";

		try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
			String day = "";
			String emp = "";
			int alID = 0;
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			SimpleDateFormat dateFormat2 = new SimpleDateFormat("dd/MM/yyyy");
			
			while ((line = br.readLine()) != null) {
				// use comma as separator
				String[] csvLine = line.split(cvsSplitBy);

				// New day or new employee - create new line
				if (day.compareTo(csvLine[2]) != 0 || emp.compareTo(csvLine[1]) != 0) {
					MHR_AttendanceLine al = new MHR_AttendanceLine(getCtx(), 0, get_TrxName());
					al.setHR_Attendance_ID(attendance.get_ID());
					
					MBPartner employed = new Query(getCtx(), MBPartner.Table_Name, "Value=?", get_TrxName())
						.setParameters(csvLine[1])
						.first();
					
					if (employed == null) {
						log.severe("No se encuentra el empleado " + csvLine[1]);
						return "@Error@ No se encuentra el empleado " + csvLine[1];
					}

					al.setC_BPartner_ID(employed.getC_BPartner_ID());
					al.setWeekDay(getWeekDayValue(csvLine[2].replace("\"", "")));
					
					if (csvLine.length >= 5) {
						Date parsedDate = dateFormat2.parse(csvLine[3].replace("\"", ""));
						al.setAttendanceDate(new Timestamp(parsedDate.getTime()));
						
						Timestamp time1 = null;
						if (csvLine[4] != null) {
							parsedDate = dateFormat.parse(csvLine[3].replace("\"", "") + " " + csvLine[4].replace("\"", ""));
							time1 = new Timestamp(parsedDate.getTime());
							al.setTime1(time1);
						}
						
						Timestamp time2 = null;
						if (csvLine.length >= 7) {
							if (csvLine[6] != null) {
								parsedDate = dateFormat.parse(csvLine[3].replace("\"", "") + " " + csvLine[6].replace("\"", ""));
								time2 = new Timestamp(parsedDate.getTime());
								al.setTime2(time2);
							}
						}
						
						BigDecimal QtyOfHours1 = BigDecimal.ZERO;
						if (time1 != null && time2 != null) {
							QtyOfHours1 = BigDecimal.valueOf((time2.getTime() - time1.getTime()) / (1000 * 60))
								.divide(BigDecimal.valueOf(60), 2, BigDecimal.ROUND_HALF_EVEN);
						}
						al.setQtyOfHours1(QtyOfHours1);
						al.setTotalQtyOfHours(al.getTotalQtyOfHours().add(QtyOfHours1));
					} else if (attendance.isCompleteWithShift()) {
						MGH_ShiftsLine sl = new Query(getCtx(), MGH_ShiftsLine.Table_Name, "WeekDay=?", get_TrxName())
								.setParameters(getWeekDayValue(csvLine[2].replace("\"", "")))
								.first();
						al.setTime1(sl.getTime1());
						al.setTime2(sl.getTime2());
						BigDecimal QtyOfHours1 = BigDecimal.valueOf((sl.getTime2().getTime() - sl.getTime1().getTime()) / (1000 * 60))
								.divide(BigDecimal.valueOf(60), 2, BigDecimal.ROUND_HALF_EVEN);
						al.setQtyOfHours1(QtyOfHours1);
						al.setTotalQtyOfHours(al.getTotalQtyOfHours().add(QtyOfHours1));
					}

					al.saveEx();

					day = csvLine[2];
					emp = csvLine[1];
					alID = al.get_ID();
				} else {
					// Same day and employee - update existing line with shift 2
					MHR_AttendanceLine al = new MHR_AttendanceLine(getCtx(), alID, get_TrxName());
					if (csvLine.length >= 5) {
						Date parsedDate = dateFormat2.parse(csvLine[3].replace("\"", ""));
						Timestamp time3 = null;
						if (csvLine[4] != null) {
							parsedDate = dateFormat.parse(csvLine[3] + " " + csvLine[4].replace("\"", ""));
							time3 = new Timestamp(parsedDate.getTime());
							al.setTime3(time3);
						}
						
						Timestamp time4 = null;
						if (csvLine.length >= 7) {
							if (csvLine[6] != null) {
								parsedDate = dateFormat.parse(csvLine[3].replace("\"", "") + " " + csvLine[6].replace("\"", ""));
								time4 = new Timestamp(parsedDate.getTime());
								al.setTime4(time4);
							}
						}
						
						BigDecimal QtyOfHours2 = BigDecimal.ZERO;
						if (time3 != null && time4 != null) {
							QtyOfHours2 = BigDecimal.valueOf((time4.getTime() - time3.getTime()) / (1000 * 60))
								.divide(BigDecimal.valueOf(60), 2, BigDecimal.ROUND_HALF_EVEN);
						}
						al.setQtyOfHours2(QtyOfHours2);
						al.setTotalQtyOfHours(al.getTotalQtyOfHours().add(QtyOfHours2));
					} else if (attendance.isCompleteWithShift()) {
						MGH_ShiftsLine sl = new Query(getCtx(), MGH_ShiftsLine.Table_Name, "WeekDay=?", get_TrxName())
								.setParameters(getWeekDayValue(csvLine[2].replace("\"", "")))
								.first();
						al.setTime3(sl.getTime3());
						al.setTime4(sl.getTime4());
						BigDecimal QtyOfHours2 = BigDecimal.valueOf((sl.getTime4().getTime() - sl.getTime3().getTime()) / (1000 * 60))
								.divide(BigDecimal.valueOf(60), 2, BigDecimal.ROUND_HALF_EVEN);
						al.setQtyOfHours2(QtyOfHours2);
						al.setTotalQtyOfHours(al.getTotalQtyOfHours().add(QtyOfHours2));
					}
					al.saveEx();
					day = "";
					emp = "";
					alID = 0;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Calculates the difference between the expected shift hours and actual attendance markings.
	 * 
	 * @param attendance The marking record containing actual work hours.
	 * @return The difference in hours (positive values only) after applying tolerance.
	 */
	protected BigDecimal getDifference(MMarking attendance) {
		String WeekDay = getWeekDayValue(attendance.getWeekDayStr());
		if (WeekDay == null)
			return BigDecimal.ZERO;
		
		MGH_ShiftsLine shiftline = new Query(getCtx(), MGH_ShiftsLine.Table_Name, "WeekDay=?", get_TrxName())
			.setParameters(WeekDay)
			.first();
		
		BigDecimal diference = shiftline.getQtyOfHours1().subtract(attendance.getQtyOfHours1());
		diference = diference.subtract(shiftline.getTolerance());
		return (diference.compareTo(BigDecimal.ZERO) > 0) ? diference : BigDecimal.ZERO;
	}

	/**
	 * Translates a weekday name string into a reference value using iDempiere List Reference 167.
	 * 
	 * @param WeekDayStr The name of the weekday (e.g., "Lunes").
	 * @return The reference value (e.g., "1") or null if not found.
	 */
	protected String getWeekDayValue(String WeekDayStr) {
		List<MRefList> reflist = new Query(getCtx(), MRefList.Table_Name, "AD_Reference_ID=?", get_TrxName())
			.setParameters(167)
			.list();

		for (MRefList ref : reflist) {
			if (ref.getName().trim().compareToIgnoreCase(WeekDayStr) == 0)
				return ref.getValue();
		}
		return null;
	}

}
