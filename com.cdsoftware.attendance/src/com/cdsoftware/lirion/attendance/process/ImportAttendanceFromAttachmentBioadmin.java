package com.cdsoftware.lirion.attendance.process;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MAttachment;
import org.compiere.model.MAttachmentEntry;
import org.compiere.model.MBPartner;
import org.compiere.model.MRefList;
import org.compiere.model.MSysConfig;
import org.compiere.model.Query;
import org.compiere.model.X_AD_ImpFormat;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.DB;
import org.compiere.util.Env;

import com.cdsoftware.lirion.attendance.model.MGH_Shifts;
import com.cdsoftware.lirion.attendance.model.MGH_ShiftsLine;
import com.cdsoftware.lirion.attendance.model.MHR_Attendance;
import com.cdsoftware.lirion.attendance.model.MHR_AttendanceLine;

/**
 * estructura del csv
 * [bpIndex]  Codigo de Empleado: Primer no nulo de (HR_ClockCode,taxid,value)
 * [dateIndex] fecha marcacion
 * [2]        Hora Entrada (opcional)
 * [3]        Hora Salida Almuerzo (opcional)
 * [4]        Hora regreso Almuerzo (opcional)
 * [5]        Hora Salida (opcional)
 */
@org.adempiere.base.annotation.Process
public class ImportAttendanceFromAttachmentBioadmin extends SvrProcess {

	private StringBuilder usersNotFoundList = new StringBuilder();
	private StringBuilder invalidLinesLog = new StringBuilder();

	private String p_DateTimeFormat = null;
	private String p_FormatType = "";
	private boolean p_HasHeader = false;
	private boolean p_HasHoursColumns = false;

	private int RECORD_ID;
	private String ATTENDANCE_FILE_LOCATION = "";

	private int bpIndex = 0;
	private int dateIndex = 1;
	private int Hour1Index = 2;
	private int Hour2Index = 3;
	private int Hour3Index = 4;
	private int Hour4Index = 5;

	private static final int DEFAULT_ATTENDANCE_TIME_BLOCK_MINUTES = 1;
	private static final String ATTENDANCE_TIME_BLOCK_SYSCONFIG = "CDS_AttendanceTimeBlock";

	@Override
	protected void prepare() {
		ProcessInfoParameter[] parameters = getParameter();
		for (ProcessInfoParameter para : parameters) {
			String name = para.getParameterName();
			if (para.getParameter() == null) {
				continue;
			} else if (name.equals("DateTimeFormat")) {
				p_DateTimeFormat = para.getParameterAsString();
			} else if (name.equals("FormatType")) {
				p_FormatType = para.getParameterAsString();
			} else if (name.equals("HasHeader")) {
				p_HasHeader = para.getParameterAsBoolean();
			} else if (name.equals("bpIndex")) {
				bpIndex = para.getParameterAsInt();
			} else if (name.equals("dateIndex")) {
				dateIndex = para.getParameterAsInt();
			} else if (name.equals("HasHoursColumns")) {
				p_HasHoursColumns = para.getParameterAsBoolean();
			} else if (name.equals("Hour1Index")) {
				Hour1Index = para.getParameterAsInt();
			} else if (name.equals("Hour2Index")) {
				Hour2Index = para.getParameterAsInt();
			} else if (name.equals("Hour3Index")) {
				Hour3Index = para.getParameterAsInt();
			} else if (name.equals("Hour4Index")) {
				Hour4Index = para.getParameterAsInt();
			}
		}
		RECORD_ID = getRecord_ID();
	}

	@Override
	protected String doIt() throws Exception {
		usersNotFoundList = new StringBuilder();
		invalidLinesLog = new StringBuilder();

		if (RECORD_ID > 0 && this.getTable_ID() == MHR_Attendance.Table_ID) {
			return writeAttendance();
		}
		return writeAttendanceFromServer();
	}

	protected String writeAttendance() throws Exception {
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
		importAttendanceFile(csvFile, attendance, false);

		appendImportNotes(attendance);
		return "@OK@";
	}

	protected String writeAttendance(String pathName) throws Exception {
		MHR_Attendance attendance;
		if (RECORD_ID > 0) {
			attendance = new MHR_Attendance(getCtx(), RECORD_ID, get_TrxName());
		} else {
			attendance = new MHR_Attendance(getCtx(), 0, get_TrxName());
			attendance.setName(new Timestamp(System.currentTimeMillis()).toString());
			attendance.saveEx();
		}

		File csvFile = new File(pathName);
		importAttendanceFile(csvFile, attendance, true);

		appendImportNotes(attendance);
		moveProcessedFile(csvFile);
		return "@OK@";
	}

	protected String writeAttendanceFromServer() throws Exception {
		ATTENDANCE_FILE_LOCATION = MSysConfig.getValue("ATTENDANCE_FILE_LOCATION", "/home/admin1/txt/", getAD_Client_ID());
		File directory = new File(ATTENDANCE_FILE_LOCATION);

		if (!directory.exists() || !directory.isDirectory()) {
			return "@Error@ La ubicación del archivo no es un directorio válido";
		}

		File[] files = directory.listFiles();
		if (files == null || files.length == 0) {
			return "@OK@ El directorio está vacío";
		}

		for (File file : files) {
			if (file.isFile()) {
				try {
					writeAttendance(file.getPath());
				} catch (Exception e) {
					log.log(Level.SEVERE, "Error procesando archivo: " + file.getAbsolutePath(), e);
				}
			}
		}
		return "@OK@ Proceso Terminado";
	}

	private void importAttendanceFile(File csvFile, MHR_Attendance attendance, boolean deleteCurrentAttendanceLines) throws Exception {
		if (csvFile == null || !csvFile.exists() || !csvFile.isFile()) {
			throw new AdempiereException("Archivo no válido: " + csvFile);
		}

		if (attendance == null || attendance.get_ID() <= 0) {
			throw new AdempiereException("No se encontró el encabezado de asistencia");
		}

		if (deleteCurrentAttendanceLines && RECORD_ID > 0) {
			deleteCurrentAttendanceLines();
		}

		String delimiter = resolveDelimiter();
		SimpleDateFormat dateTimeFormat = buildStrictDateFormat(p_DateTimeFormat);

		List<attendanceCsvLine> importedLines = readCsvLines(csvFile, delimiter);
		if (importedLines.isEmpty()) {
			log.warning("No se encontraron líneas válidas en el archivo: " + csvFile.getAbsolutePath());
			return;
		}

		List<attendanceCsvLine> sortedLines = importedLines.stream()
				.sorted(buildAttendanceComparator())
				.collect(Collectors.toList());

		sortedLines = filterNearbyMarkings(sortedLines, dateTimeFormat);

		processAttendanceLines(sortedLines, attendance, dateTimeFormat);
	}

	private void deleteCurrentAttendanceLines() {
		StringBuilder clientCheck = new StringBuilder(" AND AD_Client_ID=").append(getAD_Client_ID());
		StringBuilder sql = new StringBuilder("DELETE FROM HR_AttendanceLine ")
				.append("WHERE HR_Attendance_ID=").append(RECORD_ID).append(clientCheck);

		int no = DB.executeUpdate(sql.toString(), get_TrxName());
		if (log.isLoggable(Level.FINE)) {
			log.fine("Delete Attendance =" + no);
		}
	}

	private String resolveDelimiter() {
		if (X_AD_ImpFormat.FORMATTYPE_CommaSeparated.equals(p_FormatType)) {
			return ",";
		}
		if (X_AD_ImpFormat.FORMATTYPE_TabSeparated.equals(p_FormatType)) {
			return "\\t";
		}
		throw new IllegalArgumentException("Separador no válido: " + p_FormatType);
	}

	private List<attendanceCsvLine> readCsvLines(File csvFile, String delimiter) throws Exception {
		List<attendanceCsvLine> valueDatelist = new ArrayList<>();

		try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
			int countlines = 0;
			String inputLine;

			while ((inputLine = br.readLine()) != null) {
				if (countlines == 0 && p_HasHeader) {
					countlines++;
					continue;
				}

				if (inputLine == null || inputLine.trim().isEmpty()) {
					countlines++;
					continue;
				}

				String[] csvLine = inputLine.split(String.valueOf(delimiter), -1);
				if (csvLine.length <= 1) {
					logInvalidLine("Separador incorrecto", inputLine, null);
					countlines++;
					continue;
				}

				try {
					attendanceCsvLine parsed = parseCsvLine(csvLine);
					if (parsed != null) {
						valueDatelist.add(parsed);
					}
				} catch (Exception e) {
					logInvalidLine("Línea ignorada por error de parseo", inputLine, e);
				}

				countlines++;
			}
		}

		return valueDatelist;
	}

	private attendanceCsvLine parseCsvLine(String[] csvLine) throws Exception {
		if (bpIndex >= csvLine.length || dateIndex >= csvLine.length) {
			throw new ParseException("Índices de columnas fuera de rango", 0);
		}

		String bpCode = normalizeCsvValue(csvLine[bpIndex]);
		if (bpCode == null || bpCode.trim().isEmpty()) {
			throw new ParseException("Código de empleado vacío", 0);
		}

		String formattedDate = normalizeCsvValue(csvLine[dateIndex]);
		Date parsedDateTime = parseDateWithFallbacks(formattedDate);

		attendanceCsvLine atcsvLine = new attendanceCsvLine(bpCode, parsedDateTime);

		if (p_HasHoursColumns) {
			atcsvLine.setTime1(parseOptionalHour(csvLine, Hour1Index));
			atcsvLine.setTime2(parseOptionalHour(csvLine, Hour2Index));
			atcsvLine.setTime3(parseOptionalHour(csvLine, Hour3Index));
			atcsvLine.setTime4(parseOptionalHour(csvLine, Hour4Index));
		}

		return atcsvLine;
	}

	private Timestamp parseOptionalHour(String[] csvLine, int index) throws ParseException {
		if (index < 0 || index >= csvLine.length) {
			return null;
		}
		String raw = normalizeCsvValue(csvLine[index]);
		if (raw == null || raw.trim().isEmpty()) {
			return null;
		}
		return formatTimeField(parseHourWithFallbacks(raw));
	}

	private void processAttendanceLines(List<attendanceCsvLine> sortedvalueDatelist, MHR_Attendance attendance,
			SimpleDateFormat dateTimeFormat) throws Exception {

		Timestamp time1 = null, time2 = null, time3 = null, time4 = null;
		String day = "", emp = "";
		int alID = 0, i = 1;
		String lastnotfoundbp = "";
		int count = 0;

		for (attendanceCsvLine line : sortedvalueDatelist) {
			count++;

			if (line == null || line.getValue() == null || line.getValue().trim().isEmpty()) {
				continue;
			}

			Date parsedDateTime = line.getDate();
			if (parsedDateTime == null) {
				continue;
			}

			Date parsedDate = extraerFecha(parsedDateTime, dateTimeFormat);
			if (parsedDate == null) {
				log.warning("No se pudo extraer fecha de: " + parsedDateTime);
				continue;
			}

			if (!sameEmployeeSameDay(day, emp, parsedDate, line.getValue().trim())) {
				checkLastAttendanceTime(alID);
				i = 1;

				MBPartner employed = findEmployee(line.getValue());
				if (employed == null) {
					if (!line.getValue().equals(lastnotfoundbp)) {
						lastnotfoundbp = line.getValue();
						usersNotFoundList.append(line.getValue()).append(",");
						log.warning("No se encuentra el empleado " + line.getValue());
					}
					continue;
				}

				MHR_AttendanceLine al = upsertInitialLine(attendance, employed, parsedDate, parsedDateTime, line);

				time1 = al.getTime1();
				time2 = al.getTime2();
				time3 = al.getTime3();
				time4 = al.getTime4();

				this.statusUpdate("Procesando: " + count + "/" + sortedvalueDatelist.size() + " "
						+ employed.getValue() + " " + employed.getName() + " " + parsedDateTime);

				day = parsedDate.toString();
				emp = line.getValue();
				alID = al.get_ID();
			} else {
				MHR_AttendanceLine al = new MHR_AttendanceLine(getCtx(), alID, get_TrxName());
				if (al.get_ID() <= 0) {
					continue;
				}

				if (p_HasHoursColumns) {
					if (line.getTime1() != null || line.getTime2() != null || line.getTime3() != null || line.getTime4() != null) {
						if (i == 2) {
							time2 = line.getTime2() != null ? formatTimeField(line.getTime2()) : line.getTime1();
							al.setTime2(time2);

							BigDecimal qtyOfHours1 = calculateHoursBetween(time1, time2);
							al.setQtyOfHours1(qtyOfHours1);
							al.setTotalQtyOfHours(safeBigDecimal(al.getTotalQtyOfHours()).add(qtyOfHours1));
						}
						if (i == 3) {
							time3 = line.getTime3() != null ? formatTimeField(line.getTime3()) : line.getTime1();
							al.setTime3(time3);
						}
						if (i >= 4) {
							time4 = line.getTime4() != null ? formatTimeField(line.getTime4()) : line.getTime1();
							al.setTime4(time4);

							BigDecimal qtyOfHours2 = calculateHoursBetween(time3, time4);
							al.setQtyOfHours2(qtyOfHours2);
							al.setTotalQtyOfHours(safeBigDecimal(al.getTotalQtyOfHours()).add(qtyOfHours2));
						}
					}
				} else {
					if (line.getDate() != null) {
						if (i == 2) {
							time2 = formatTimeField(line.getDate());
							al.setTime2(time2);

							BigDecimal qtyOfHours1 = calculateHoursBetween(time1, time2);
							al.setQtyOfHours1(qtyOfHours1);
							al.setTotalQtyOfHours(safeBigDecimal(al.getTotalQtyOfHours()).add(qtyOfHours1));
						}
						if (i == 3) {
							time3 = formatTimeField(line.getDate());
							al.setTime3(time3);
						}
						if (i >= 4) {
							time4 = formatTimeField(line.getDate());
							al.setTime4(time4);

							BigDecimal qtyOfHours2 = calculateHoursBetween(time3, time4);
							al.setQtyOfHours2(qtyOfHours2);
							al.setTotalQtyOfHours(safeBigDecimal(al.getTotalQtyOfHours()).add(qtyOfHours2));
						}
					}
				}

				al = saveAttendanceLineSafely(al, al.getC_BPartner_ID(), al.getAttendanceDate(), attendance,
						al.getTime1(), al.getTime2(), al.getTime3(), al.getTime4());

				if (count == sortedvalueDatelist.size()) {
					checkLastAttendanceTime(al.get_ID());
				}
			}

			i++;
			if (count % 5000 == 0) {
				commitEx();
			}
		}
	}

	private boolean sameEmployeeSameDay(String day, String emp, Date parsedDate, String currentEmp) {
		return day.compareTo(parsedDate.toString()) == 0 && emp.compareTo(currentEmp) == 0;
	}

	private MBPartner findEmployee(String rawCode) {
		StringBuilder whereclause = new StringBuilder();
		whereclause.append("REPLACE (trim(COALESCE(HR_ClockCode,taxid,value)), '-', '')= REPLACE (trim(?), '-', '')");
		whereclause.append(" AND IsEmployee='Y'");

		return new Query(getCtx(), MBPartner.Table_Name, whereclause.toString(), get_TrxName())
				.setParameters(rawCode)
				.setClient_ID()
				.first();
	}

	private MHR_AttendanceLine upsertInitialLine(MHR_Attendance attendance, MBPartner employed, Date parsedDate,
			Date parsedDateTime, attendanceCsvLine line) throws Exception {

		MHR_AttendanceLine al = findExistingAttendanceLine(employed.getC_BPartner_ID(), parsedDate);

		Timestamp newT1 = null;
		Timestamp newT2 = null;
		Timestamp newT3 = null;
		Timestamp newT4 = null;

		if (p_HasHoursColumns) {
			newT1 = line.getTime1() != null ? formatTimeField(line.getTime1()) : null;
			newT2 = line.getTime2() != null ? formatTimeField(line.getTime2()) : null;
			newT3 = line.getTime3() != null ? formatTimeField(line.getTime3()) : null;
			newT4 = line.getTime4() != null ? formatTimeField(line.getTime4()) : null;
		} else {
			newT1 = formatTimeField(parsedDateTime);
		}

		if (al == null) {
			al = new MHR_AttendanceLine(getCtx(), 0, get_TrxName());
			al.setHR_Attendance_ID(attendance.get_ID());
			al.setC_BPartner_ID(employed.getC_BPartner_ID());
			al.setWeekDay(getWeekDayValue(parsedDate));
			al.setAttendanceDate(new Timestamp(parsedDate.getTime()));
			al.setTime1(newT1);
			al.setTime2(newT2);
			al.setTime3(newT3);
			al.setTime4(newT4);
			recalculateCollapsedHours(al);
		} else {
			al.setHR_Attendance_ID(attendance.get_ID());

			if (allStoredTimesAreZeroOrNull(al) && hasAnyRealHour(newT1, newT2, newT3, newT4)) {
				overwriteTimes(al, newT1, newT2, newT3, newT4);
			} else {
				mergeMissingTimes(al, newT1, newT2, newT3, newT4);
			}
		}

		return saveAttendanceLineSafely(al, employed.getC_BPartner_ID(), parsedDate, attendance, newT1, newT2, newT3, newT4);
	}

	protected String getWeekDayValue(Date WeekDayStr) {
		Timestamp time = new Timestamp(WeekDayStr.getTime());
		LocalDateTime attendancedateaux = time.toLocalDateTime();
		List<MRefList> reflist = new Query(getCtx(), MRefList.Table_Name, "AD_Reference_ID=?", get_TrxName())
				.setParameters(167).list();

		for (MRefList ref : reflist) {
			if (ref.getName().trim().compareToIgnoreCase(attendancedateaux.getDayOfWeek().toString()) == 0) {
				return ref.getValue();
			}
		}
		return null;
	}

	protected class attendanceCsvLine {

		public attendanceCsvLine(String string, Date parsedDateTime) {
			value = string;
			date = parsedDateTime;
		}

		public attendanceCsvLine(String string, Date recordDate, Timestamp parsedDate1, Timestamp parsedDate2,
				Timestamp parsedDate3, Timestamp parsedDate4) {
			value = string;
			date = recordDate;
			time1 = parsedDate1;
			time2 = parsedDate2;
			time3 = parsedDate3;
			time4 = parsedDate4;
		}

		public Date getDate() {
			return date;
		}

		public void setDate(Date date) {
			this.date = date;
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

		public Timestamp getTime1() {
			return time1;
		}

		public void setTime1(Timestamp time1) {
			this.time1 = time1;
		}

		public Timestamp getTime2() {
			return time2;
		}

		public void setTime2(Timestamp time2) {
			this.time2 = time2;
		}

		public Timestamp getTime3() {
			return time3;
		}

		public void setTime3(Timestamp time3) {
			this.time3 = time3;
		}

		public Timestamp getTime4() {
			return time4;
		}

		public void setTime4(Timestamp time4) {
			this.time4 = time4;
		}

		public String value;
		public Date date;
		public Timestamp time1;
		public Timestamp time2;
		public Timestamp time3;
		public Timestamp time4;
	}

	protected BigDecimal getDifference(MHR_AttendanceLine attendanceline, MGH_Shifts pShift) {
		String WeekDay = attendanceline.getWeekDay();
		if (WeekDay == null) {
			return BigDecimal.ZERO;
		}

		MGH_ShiftsLine shiftline = new Query(getCtx(), MGH_ShiftsLine.Table_Name, "GH_Shifts_ID=? AND WeekDay=?", get_TrxName())
				.setParameters(pShift.get_ID(), WeekDay).first();

		if (shiftline == null) {
			return BigDecimal.ZERO;
		}

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

		if (shiftTime1 == null || shiftTime2 == null || shiftTime3 == null || shiftTime4 == null) {
			return BigDecimal.ZERO;
		}

		shiftTime1 = shiftTime1.plusMinutes(shiftline.getTolerance().longValue());
		shiftTime4 = shiftTime4.minusMinutes(shiftline.getTolerance().longValue());
		shiftTime2 = shiftTime2.minusMinutes(shiftline.getRestTolerance().longValue());
		shiftTime3 = shiftTime3.plusMinutes(shiftline.getRestTolerance().longValue());

		Duration duration = Duration.between(shiftTime1WithoutTolerance, attendanceTime1);

		double diffTime1 = duration.toMinutes();
		if (diffTime1 <= shiftline.getTolerance().doubleValue()) {
			duration = Duration.between(shiftTime1, attendanceTime1);
		}

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
		if (diffTime2 <= shiftline.getRestTolerance().doubleValue()) {
			duration = Duration.between(attendanceTime2, shiftTime2);
		}

		diffTime2 = diffTime2 < 0.0 ? 0.0 : diffTime2;

		if (attendanceTime3 != null) {
			duration = Duration.between(shiftTime3WithoutTolerance, attendanceTime3);
			diffTime3 = duration.toMinutes();
			if (diffTime3 <= shiftline.getRestTolerance().doubleValue()) {
				duration = Duration.between(shiftTime3, attendanceTime3);
			}
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

		BigDecimal diference = BigDecimal.valueOf((diffTime1 + diffTime2 + diffTime3 + diffTime4) / 60.0)
				.setScale(2, RoundingMode.HALF_UP);

		return diference.compareTo(BigDecimal.ZERO) > 0 ? diference : BigDecimal.ZERO;
	}

	protected LocalDateTime prepareTime(Timestamp attendanceDate, Timestamp time1) {
		if (attendanceDate == null || time1 == null) {
			return null;
		}
		LocalDateTime aux = time1.toLocalDateTime();
		LocalDateTime time = attendanceDate.toLocalDateTime();
		time = time.plusHours(aux.getHour());
		time = time.plusMinutes(aux.getMinute());
		return time;
	}

	protected Timestamp formatTimeField(Date dateTime) {
		if (dateTime == null) {
			return null;
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(dateTime);
		cal.set(Calendar.YEAR, 1970);
		cal.set(Calendar.MONTH, Calendar.JANUARY);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.set(Calendar.MILLISECOND, 0);
		Date newDate = cal.getTime();
		return new Timestamp(newDate.getTime());
	}

	protected static Date extraerFecha(Date fecha, SimpleDateFormat formato) {
		try {
			Calendar cal = Calendar.getInstance();
			cal.setTime(fecha);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			return cal.getTime();
		} catch (Exception e) {
			return null;
		}
	}

	protected void checkLastAttendanceTime(int id) throws Exception {
		if (id <= 0) {
			return;
		}

		MHR_AttendanceLine al = new MHR_AttendanceLine(getCtx(), id, get_TrxName());
		if (al.get_ID() <= 0) {
			return;
		}

		if (al.getTime4() == null && al.getTime3() != null) {
			al.setTime4(al.getTime3());
			al.setTime3(null);
		} else if (al.getTime4() == null && al.getTime3() == null && al.getTime2() != null) {
			al.setTime4(al.getTime2());
			al.setTime2(null);
		}

		recalculateCollapsedHours(al);

		Date parsedDate = al.getAttendanceDate();
		if (parsedDate == null) {
			al.saveEx();
			return;
		}

		MHR_Attendance attendance = new MHR_Attendance(getCtx(), al.getHR_Attendance_ID(), get_TrxName());
		saveAttendanceLineSafely(al, al.getC_BPartner_ID(), parsedDate, attendance,
				al.getTime1(), al.getTime2(), al.getTime3(), al.getTime4());
	}

	private void recalculateCollapsedHours(MHR_AttendanceLine al) {
		BigDecimal qty1 = Env.ZERO;
		BigDecimal qty2 = Env.ZERO;
		BigDecimal total = Env.ZERO;

		Timestamp time1 = al.getTime1();
		Timestamp time2 = al.getTime2();
		Timestamp time3 = al.getTime3();
		Timestamp time4 = al.getTime4();

		if (time1 != null && time4 != null && (time2 == null || time3 == null)) {
			qty1 = calculateHoursBetween(time1, time4);
			al.setTime2(null);
			al.setTime3(null);
			al.setQtyOfHours1(qty1);
			al.setQtyOfHours2(Env.ZERO);
			al.setTotalQtyOfHours(qty1);
			return;
		}

		if (time1 != null && time2 != null) {
			qty1 = calculateHoursBetween(time1, time2);
		}

		if (time3 != null && time4 != null) {
			qty2 = calculateHoursBetween(time3, time4);
		}

		total = qty1.add(qty2);
		al.setQtyOfHours1(qty1);
		al.setQtyOfHours2(qty2);
		al.setTotalQtyOfHours(total);
	}

	private BigDecimal calculateHoursBetween(Timestamp from, Timestamp to) {
		if (from == null || to == null) {
			return Env.ZERO;
		}

		long diffMillis = to.getTime() - from.getTime();
		if (diffMillis <= 0) {
			return Env.ZERO;
		}

		return BigDecimal.valueOf(diffMillis / (1000.0 * 60.0))
				.divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_EVEN);
	}

	private BigDecimal safeBigDecimal(BigDecimal value) {
		return value == null ? Env.ZERO : value;
	}

	private long getAttendanceTimeBlockMillis() {
		String configuredValue = MSysConfig.getValue(
				ATTENDANCE_TIME_BLOCK_SYSCONFIG,
				String.valueOf(DEFAULT_ATTENDANCE_TIME_BLOCK_MINUTES),
				getAD_Client_ID());

		int minutes = DEFAULT_ATTENDANCE_TIME_BLOCK_MINUTES;

		try {
			if (configuredValue != null && configuredValue.trim().length() > 0) {
				minutes = Integer.parseInt(configuredValue.trim());
			}
		} catch (Exception e) {
			minutes = DEFAULT_ATTENDANCE_TIME_BLOCK_MINUTES;
		}

		if (minutes <= 0) {
			minutes = DEFAULT_ATTENDANCE_TIME_BLOCK_MINUTES;
		}

		return Duration.ofMinutes(minutes).toMillis();
	}

	private List<attendanceCsvLine> filterNearbyMarkings(List<attendanceCsvLine> source, SimpleDateFormat dateTimeFormat) {
		List<attendanceCsvLine> filtered = new ArrayList<>();
		attendanceCsvLine lastAccepted = null;
		long blackoutMillis = getAttendanceTimeBlockMillis();

		for (attendanceCsvLine current : source) {
			if (current == null || current.getValue() == null || current.getDate() == null) {
				continue;
			}

			if (lastAccepted == null) {
				filtered.add(current);
				lastAccepted = current;
				continue;
			}

			boolean sameEmployee = current.getValue().trim().equalsIgnoreCase(lastAccepted.getValue().trim());

			Date currentDay = extraerFecha(current.getDate(), dateTimeFormat);
			Date lastDay = extraerFecha(lastAccepted.getDate(), dateTimeFormat);
			boolean sameDay = currentDay != null && lastDay != null && currentDay.equals(lastDay);

			long diffMillis = current.getDate().getTime() - lastAccepted.getDate().getTime();

			if (sameEmployee && sameDay && diffMillis >= 0 && diffMillis < blackoutMillis) {
				continue;
			}

			filtered.add(current);
			lastAccepted = current;
		}

		return filtered;
	}

	private Comparator<attendanceCsvLine> buildAttendanceComparator() {
		return new Comparator<attendanceCsvLine>() {
			@Override
			public int compare(attendanceCsvLine o1, attendanceCsvLine o2) {
				String v1 = o1 != null ? o1.getValue() : null;
				String v2 = o2 != null ? o2.getValue() : null;

				if (v1 == null && v2 == null) {
					return compareDates(o1 != null ? o1.getDate() : null, o2 != null ? o2.getDate() : null);
				}
				if (v1 == null) {
					return -1;
				}
				if (v2 == null) {
					return 1;
				}

				int cmp = v1.compareTo(v2);
				if (cmp != 0) {
					return cmp;
				}

				return compareDates(o1.getDate(), o2.getDate());
			}
		};
	}

	private int compareDates(Date d1, Date d2) {
		if (d1 == null && d2 == null) {
			return 0;
		}
		if (d1 == null) {
			return -1;
		}
		if (d2 == null) {
			return 1;
		}
		return d1.compareTo(d2);
	}

	private MHR_AttendanceLine findExistingAttendanceLine(int bpartnerId, Date dateOnly) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(dateOnly);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		Timestamp start = new Timestamp(cal.getTimeInMillis());
		cal.add(Calendar.DAY_OF_MONTH, 1);
		Timestamp end = new Timestamp(cal.getTimeInMillis());

		String where = "C_BPartner_ID=? AND AttendanceDate>=? AND AttendanceDate<?";
		return new Query(getCtx(), MHR_AttendanceLine.Table_Name, where, get_TrxName())
				.setParameters(bpartnerId, start, end)
				.setOnlyActiveRecords(true)
				.first();
	}

	private boolean isUniqueViolation(Throwable t) {
		while (t != null) {
			if (t instanceof SQLException) {
				String sqlState = ((SQLException) t).getSQLState();
				if ("23505".equals(sqlState)) {
					return true;
				}
			}
			String msg = t.getMessage();
			if (msg != null) {
				if (msg.contains("SaveErrorNotUnique")
						|| msg.contains("duplicate key value")
						|| (msg.contains("restricci") && msg.contains("unicidad"))
						|| msg.contains("nonduplicatedlines")) {
					return true;
				}
			}
			t = t.getCause();
		}
		return false;
	}

	private boolean isZeroTime(Timestamp ts) {
		if (ts == null) {
			return true;
		}

		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(ts.getTime());

		return cal.get(Calendar.HOUR_OF_DAY) == 0
				&& cal.get(Calendar.MINUTE) == 0
				&& cal.get(Calendar.SECOND) == 0
				&& cal.get(Calendar.MILLISECOND) == 0;
	}

	private boolean hasAnyRealHour(Timestamp t1, Timestamp t2, Timestamp t3, Timestamp t4) {
		return !isZeroTime(t1) || !isZeroTime(t2) || !isZeroTime(t3) || !isZeroTime(t4);
	}

	private boolean allStoredTimesAreZeroOrNull(MHR_AttendanceLine al) {
		if (al == null) {
			return false;
		}

		return isZeroTime(al.getTime1())
				&& isZeroTime(al.getTime2())
				&& isZeroTime(al.getTime3())
				&& isZeroTime(al.getTime4());
	}

	private void overwriteTimes(MHR_AttendanceLine al, Timestamp t1, Timestamp t2, Timestamp t3, Timestamp t4) {
		al.setTime1(t1);
		al.setTime2(t2);
		al.setTime3(t3);
		al.setTime4(t4);
		recalculateCollapsedHours(al);
	}

	private void mergeMissingTimes(MHR_AttendanceLine al, Timestamp t1, Timestamp t2, Timestamp t3, Timestamp t4) {
		if (al == null) {
			return;
		}

		if ((al.getTime1() == null || isZeroTime(al.getTime1())) && t1 != null && !isZeroTime(t1)) {
			al.setTime1(t1);
		}
		if ((al.getTime2() == null || isZeroTime(al.getTime2())) && t2 != null && !isZeroTime(t2)) {
			al.setTime2(t2);
		}
		if ((al.getTime3() == null || isZeroTime(al.getTime3())) && t3 != null && !isZeroTime(t3)) {
			al.setTime3(t3);
		}
		if ((al.getTime4() == null || isZeroTime(al.getTime4())) && t4 != null && !isZeroTime(t4)) {
			al.setTime4(t4);
		}

		recalculateCollapsedHours(al);
	}

	private MHR_AttendanceLine saveAttendanceLineSafely(MHR_AttendanceLine al, int c_BPartner_ID, Date parsedDate,
			MHR_Attendance attendance, Timestamp newT1, Timestamp newT2, Timestamp newT3, Timestamp newT4) throws Exception {

		try {
			al.saveEx();
			return al;
		} catch (Exception e) {
			if (!isUniqueViolation(e)) {
				throw e;
			}

			MHR_AttendanceLine existing = findExistingAttendanceLine(c_BPartner_ID, parsedDate);
			if (existing == null) {
				throw e;
			}

			existing.setHR_Attendance_ID(attendance.get_ID());

			if (allStoredTimesAreZeroOrNull(existing) && hasAnyRealHour(newT1, newT2, newT3, newT4)) {
				overwriteTimes(existing, newT1, newT2, newT3, newT4);
			} else {
				mergeMissingTimes(existing, newT1, newT2, newT3, newT4);
			}

			existing.saveEx();
			return existing;
		}
	}

	private SimpleDateFormat buildStrictDateFormat(String pattern) {
		String effectivePattern = pattern;
		if (effectivePattern == null || effectivePattern.trim().isEmpty()) {
			effectivePattern = "dd/MM/yyyy HH:mm";
		}

		SimpleDateFormat sdf = new SimpleDateFormat(effectivePattern, Locale.US);
		sdf.setLenient(false);
		return sdf;
	}

	private Date parseDateWithFallbacks(String value) throws ParseException {
		String normalized = normalizeCsvValue(value);
		if (normalized == null || normalized.isEmpty()) {
			throw new ParseException("Fecha vacía", 0);
		}

		List<String> patterns = Arrays.asList(
				p_DateTimeFormat,
				"dd/MM/yyyy HH:mm",
				"dd/MM/yyyy HH:mm:ss",
				"MM/dd/yyyy HH:mm",
				"MM/dd/yyyy HH:mm:ss",
				"yyyy-MM-dd HH:mm",
				"yyyy-MM-dd HH:mm:ss",
				"dd-MM-yyyy HH:mm",
				"dd-MM-yyyy HH:mm:ss",
				"MM-dd-yyyy HH:mm",
				"MM-dd-yyyy HH:mm:ss",
				"dd/MM/yyyy hh:mm a",
				"dd/MM/yyyy hh:mm:ss a",
				"MM/dd/yyyy hh:mm a",
				"MM/dd/yyyy hh:mm:ss a"
		);

		ParseException last = null;

		for (String pattern : patterns) {
			if (pattern == null || pattern.trim().isEmpty()) {
				continue;
			}
			try {
				SimpleDateFormat sdf = buildStrictDateFormat(pattern);
				return sdf.parse(normalized);
			} catch (ParseException e) {
				last = e;
			}
		}

		throw last != null ? last : new ParseException("Unparseable date: \"" + normalized + "\"", 0);
	}

	private Date parseHourWithFallbacks(String value) throws ParseException {
		String normalized = normalizeCsvValue(value);
		if (normalized == null || normalized.isEmpty()) {
			return null;
		}

		List<String> patterns = Arrays.asList(
				"HH:mm",
				"HH:mm:ss",
				"H:mm",
				"H:mm:ss",
				"hh:mm a",
				"hh:mm:ss a"
		);

		ParseException last = null;

		for (String pattern : patterns) {
			try {
				SimpleDateFormat sdf = buildStrictDateFormat(pattern);
				return sdf.parse(normalized);
			} catch (ParseException e) {
				last = e;
			}
		}

		throw last != null ? last : new ParseException("Unparseable hour: \"" + normalized + "\"", 0);
	}

	private String normalizeCsvValue(String value) {
		if (value == null) {
			return null;
		}

		return value
				.replace("\"", "")
				.trim()
				.replace("a.m.", "AM")
				.replace("p.m.", "PM")
				.replace("a. m.", "AM")
				.replace("p. m.", "PM")
				.replace("am", "AM")
				.replace("pm", "PM");
	}

	private void logInvalidLine(String prefix, String inputLine, Exception e) {
		String msg = prefix + ": " + inputLine;
		if (e != null) {
			log.log(Level.WARNING, msg, e);
		} else {
			log.warning(msg);
		}

		if (invalidLinesLog.length() < 3500) {
			invalidLinesLog.append(msg).append("\n");
		}
	}

	private void appendImportNotes(MHR_Attendance attendance) {
		if (attendance == null || attendance.get_ID() <= 0) {
			return;
		}

		StringBuilder notes = new StringBuilder();

		if (usersNotFoundList.length() > 0) {
			notes.append("Usuarios no encontrados: ").append(usersNotFoundList).append("\n");
		}
		if (invalidLinesLog.length() > 0) {
			notes.append("Líneas ignoradas:\n").append(invalidLinesLog);
		}

		if (notes.length() > 0) {
			attendance.set_ValueOfColumn("Description", notes.toString());
			attendance.saveEx();
		}
	}

	private void moveProcessedFile(File csvFile) {
		try {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
			String dateTimeSuffix = LocalDateTime.now().format(formatter);

			File destDir = new File(ATTENDANCE_FILE_LOCATION + "/procesado/");
			if (!destDir.exists() && !destDir.mkdirs()) {
				log.severe("No se pudo crear la carpeta de destino: " + destDir.getAbsolutePath());
				return;
			}

			String baseName = csvFile.getName();
			int extPos = baseName.lastIndexOf('.');
			String nameWithoutExt = extPos > 0 ? baseName.substring(0, extPos) : baseName;
			String ext = extPos > 0 ? baseName.substring(extPos) : ".csv";

			File newFile = new File(destDir, nameWithoutExt + "_" + dateTimeSuffix + ext);

			boolean moved = csvFile.renameTo(newFile);
			if (!moved) {
				Files.copy(csvFile.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
				if (!csvFile.delete()) {
					log.severe("Error al eliminar el archivo original: " + csvFile.getAbsolutePath());
				}
			}
		} catch (IOException e) {
			log.log(Level.SEVERE, "Error moviendo archivo procesado: " + csvFile.getAbsolutePath(), e);
		}
	}
}