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
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.stream.Collectors;

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
 * [4]        Hora regreso Almuerzo (opcional).
 * [5]        Hora Salida (opcional)
 * [6]
 * @author alara
 *
 */

@org.adempiere.base.annotation.Process
public class ImportAttendanceFromAttachmentBioadmin extends SvrProcess{
	StringBuilder usersNotFoundList = new StringBuilder();
	String p_DateTimeFormat = null;
	String p_FormatType = "";
	boolean p_HasHeader = false;
	private int RECORD_ID;
	private String ATTENDANCE_FILE_LOCATION="";
	private int bpIndex = 0;
	private int dateIndex = 1;
	private int Hour1Index = 2;
	private int Hour2Index = 3;
	private int Hour3Index = 4;
	private int Hour4Index = 5;

	private boolean p_HasHoursColumns = false;

	private static final int DEFAULT_ATTENDANCE_TIME_BLOCK_MINUTES = 1;
	private static final String ATTENDANCE_TIME_BLOCK_SYSCONFIG = "CDS_AttendanceTimeBlock";

	@Override
	protected void prepare() {
		ProcessInfoParameter[] parameters = getParameter();
		for (ProcessInfoParameter para: parameters)
		{
			String name = para.getParameterName();
			if (para.getParameter() == null)
				;
			else if (name.equals("DateTimeFormat"))
				p_DateTimeFormat = para.getParameterAsString();
			else if (name.equals("FormatType"))
				p_FormatType = para.getParameterAsString();
			else if (name.equals("HasHeader"))
				p_HasHeader = para.getParameterAsBoolean();
			else if (name.equals("bpIndex"))
				bpIndex = para.getParameterAsInt();
			else if (name.equals("dateIndex"))
				dateIndex = para.getParameterAsInt();
			else if (name.equals("HasHoursColumns"))
				p_HasHoursColumns = para.getParameterAsBoolean();
			else if (name.equals("Hour1Index"))
				Hour1Index = para.getParameterAsInt();
			else if (name.equals("Hour2Index"))
				Hour2Index = para.getParameterAsInt();
			else if (name.equals("Hour3Index"))
				Hour3Index = para.getParameterAsInt();
			else if (name.equals("Hour4Index"))
				Hour4Index = para.getParameterAsInt();
		}
		RECORD_ID = getRecord_ID();
	}

	@Override
	protected String doIt() throws Exception {
		if(RECORD_ID>0 && this.getTable_ID()==MHR_Attendance.Table_ID)
			return writeAttendance();
		else
			return writeAttendanceFromServer();
	}

	protected String writeAttendance() throws Exception{

		Timestamp time1=null,time2=null,time3=null,time4=null;
		String day = "",emp = "";
		int alID = 0,i = 1;
		MHR_Attendance attendance = new MHR_Attendance(getCtx(), RECORD_ID, get_TrxName());
		MAttachment attachment = attendance.getAttachment();
		if (attachment == null) {
			return "@Error@Please attach the attendance file before running the process";
		}
		MAttachmentEntry entry = attachment.getEntry(0);
		if (entry == null) {
			return "@Error@Please attach the attendance file before running the process";
		}

		String delimiter = "";
		if (p_FormatType.equals(X_AD_ImpFormat.FORMATTYPE_CommaSeparated)) {
			delimiter = ",";
		} else if (p_FormatType.equals(X_AD_ImpFormat.FORMATTYPE_TabSeparated)) {
			delimiter = "\t";
		} else {
			throw new IllegalArgumentException ("separador no Valido: " + p_FormatType);
		}

		File csvFile = entry.getFile();
		FileReader fileReader = new FileReader(csvFile);

		try (BufferedReader br = new BufferedReader(fileReader)) {
			int countlines=0;
			SimpleDateFormat dateTimeFormat = new SimpleDateFormat(p_DateTimeFormat, Locale.US);
			String inputLine;
			ArrayList<attendanceCsvLine> valueDatelist = new ArrayList<>();

			Comparator<attendanceCsvLine> compareByName = Comparator
                    .comparing(attendanceCsvLine::getValue)
                    .thenComparing(attendanceCsvLine::getDate);

			while ((inputLine = br.readLine()) != null) {
				if(countlines==0 && p_HasHeader) {
					countlines++;
					continue;
				}
				if (inputLine.isEmpty() || inputLine.startsWith(String.valueOf(delimiter))) {
				    continue;
				}

				String[] csvLine = inputLine.split(String.valueOf(delimiter));
				if(csvLine.length<=1)
					throw new IllegalArgumentException ("Separador incorrecto: '" + delimiter+"'");

				String formattedDate = csvLine[dateIndex]
					    .replace("a.m.", "AM").replace("p.m.", "PM")
					    .replace("a. m.", "AM").replace("p. m.", "PM");

				Date parsedDateTime = dateTimeFormat.parse(formattedDate);
				String bpCode=csvLine[bpIndex].trim();
				attendanceCsvLine atcsvLine = new attendanceCsvLine(bpCode,parsedDateTime);

				if(p_HasHoursColumns) {
					SimpleDateFormat formatTime = new SimpleDateFormat("HH:mm", Locale.US);
					int[] hourIndexes = {Hour1Index, Hour2Index, Hour3Index, Hour4Index};
				    Date[] parsedDateTimes = new Date[4];
				    for (int j = 0; j < hourIndexes.length; j++) {
				    	 int index = hourIndexes[i];
				         if (csvLine.length > index && csvLine[index] != null && !csvLine[index].isEmpty()) {
				             parsedDateTimes[i] = formatTime.parse(csvLine[index]
				            		 .replace("a.m.", "AM").replace("p.m.", "PM")
									 .replace("a. m.", "AM").replace("p. m.", "PM"));
				         }
				     }
					atcsvLine.setTime1(formatTimeField(parsedDateTimes[0]));
					atcsvLine.setTime2(formatTimeField(parsedDateTimes[1]));
					atcsvLine.setTime3(formatTimeField(parsedDateTimes[2]));
					atcsvLine.setTime4(formatTimeField(parsedDateTimes[3]));
				}

				valueDatelist.add(atcsvLine);
				countlines++;
			}
			fileReader.close();

		    List<attendanceCsvLine> sortedvalueDatelist = valueDatelist.stream()
                    .sorted(compareByName)
                    .collect(Collectors.toList());

		    sortedvalueDatelist = filterNearbyMarkings(sortedvalueDatelist, dateTimeFormat);

			String lastnotfoundbp = "";
			int count = 0;
			for (attendanceCsvLine line : sortedvalueDatelist) {
				count++;
				if(line.getValue() == null || line.getValue().trim().length() == 0) {
				    log.warning("Linea ignorada por codigo de empleado vacio");
				    continue;
				};

				Date parsedDateTime,parsedDate;
				parsedDateTime = line.getDate();
				parsedDate = extraerFecha(parsedDateTime, dateTimeFormat);

				if(day.compareTo(parsedDate.toString())!=0 || emp.compareTo(line.getValue().trim())!=0) {
					checkLastAttendanceTime(alID);
					i=1;

					StringBuilder whereclause = new StringBuilder();
					whereclause.append("REPLACE (trim(COALESCE(HR_ClockCode,taxid,value)), '-', '')= REPLACE (trim(?), '-', '')");
					whereclause.append(" AND IsEmployee='Y'");
					MBPartner employed = new Query(getCtx(), MBPartner.Table_Name,
							whereclause.toString(), get_TrxName()).setParameters(line.getValue())
							.setClient_ID()
							.first();
					if(employed==null) {
						if(lastnotfoundbp.compareTo(line.getValue())!=0) {
							lastnotfoundbp=line.getValue();
							usersNotFoundList.append(line.getValue()+",");
							log.warning("No se encuentra el empleado "+line.getValue());
						}
						continue;
					}

					MHR_AttendanceLine al = findExistingAttendanceLine(employed.getC_BPartner_ID(), parsedDate);
					if (al == null) {
						al = new MHR_AttendanceLine(getCtx(), 0, get_TrxName());
						al.setHR_Attendance_ID(attendance.get_ID());
						al.setC_BPartner_ID(employed.getC_BPartner_ID());
						al.setWeekDay(getWeekDayValue(parsedDate));
						al.setAttendanceDate(new Timestamp(parsedDate.getTime()));
						if(p_HasHoursColumns) {
							time1 = formatTimeField(line.getTime1());
							al.setTime1(time1);
							time2 = formatTimeField(line.getTime2());
							al.setTime2(time2);
							time3 = formatTimeField(line.getTime3());
							al.setTime3(time3);
							time4 = formatTimeField(line.getTime4());
							al.setTime4(time4);
						}
						else {
							time1 = formatTimeField(parsedDateTime);
							al.setTime1(time1);
						}
						try {
							al.saveEx();
						} catch (Exception e) {
							if (isUniqueViolation(e)) {
								MHR_AttendanceLine existing = findExistingAttendanceLine(employed.getC_BPartner_ID(), parsedDate);
								if (existing != null) {
									al = existing;
									al.setHR_Attendance_ID(attendance.get_ID());
									if (p_HasHoursColumns) {
										Timestamp t1 = formatTimeField(line.getTime1());
										Timestamp t2 = formatTimeField(line.getTime2());
										Timestamp t3 = formatTimeField(line.getTime3());
										Timestamp t4 = formatTimeField(line.getTime4());
										if (al.getTime1()==null && t1!=null) al.setTime1(t1);
										if (al.getTime2()==null && t2!=null) al.setTime2(t2);
										if (al.getTime3()==null && t3!=null) al.setTime3(t3);
										if (al.getTime4()==null && t4!=null) al.setTime4(t4);
									} else {
										Timestamp t1 = formatTimeField(parsedDateTime);
										if (al.getTime1()==null && t1!=null) al.setTime1(t1);
									}
									al.saveEx();
								} else {
									throw e;
								}
							} else {
								throw e;
							}
						}
					} else {
						al.setHR_Attendance_ID(attendance.get_ID());
						if(p_HasHoursColumns) {
							Timestamp t1 = formatTimeField(line.getTime1());
							Timestamp t2 = formatTimeField(line.getTime2());
							Timestamp t3 = formatTimeField(line.getTime3());
							Timestamp t4 = formatTimeField(line.getTime4());
							if (al.getTime1()==null && t1!=null) al.setTime1(t1);
							if (al.getTime2()==null && t2!=null) al.setTime2(t2);
							if (al.getTime3()==null && t3!=null) al.setTime3(t3);
							if (al.getTime4()==null && t4!=null) al.setTime4(t4);
						} else {
							Timestamp t1 = formatTimeField(parsedDateTime);
							if (al.getTime1()==null && t1!=null) al.setTime1(t1);
						}
						al.saveEx();
					}

					this.statusUpdate("Procesando: "+count+"/"+sortedvalueDatelist.size()+" "+employed.getValue()+" "+employed.getName()+" "+parsedDateTime);
					day=parsedDate.toString();
					emp=line.getValue();
					alID=al.get_ID();
				}else {
					MHR_AttendanceLine al= new MHR_AttendanceLine(getCtx(), alID, get_TrxName());
					if(p_HasHoursColumns) {
						if(line.getTime1()!=null) {
							if(i==2) {
								time2 = formatTimeField(line.getTime2());
								al.setTime2(time2);
								BigDecimal QtyOfHours1=BigDecimal.ZERO;
								if(time1!=null && time2!=null) {
									QtyOfHours1 = BigDecimal.valueOf((time2.getTime()-time1.getTime())/(1000*60)).divide(BigDecimal.valueOf(60),2,RoundingMode.HALF_EVEN);
								}
								al.setQtyOfHours1(QtyOfHours1);
								al.setTotalQtyOfHours(al.getTotalQtyOfHours().add(QtyOfHours1));
							}
							if(i==3) {
								time3 = formatTimeField(line.getTime3());
								al.setTime3(time3);
							}
							if(i>=4) {
								time4 = formatTimeField(line.getTime4());
								al.setTime4(time4);
								BigDecimal QtyOfHours2=BigDecimal.ZERO;
								if(time3!=null && time4!=null) {
									QtyOfHours2 = BigDecimal.valueOf((time4.getTime()-time3.getTime())/(1000*60)).divide(BigDecimal.valueOf(60),2,RoundingMode.HALF_EVEN);
								}
								al.setQtyOfHours2(QtyOfHours2);
								al.setTotalQtyOfHours(al.getTotalQtyOfHours().add(QtyOfHours2));
							}
						}
					}
					else {
						if(line.getDate()!=null) {
							if(i==2) {
								time2 = formatTimeField(line.getDate());
								al.setTime2(time2);
								BigDecimal QtyOfHours1=BigDecimal.ZERO;
								if(time1!=null && time2!=null) {
									QtyOfHours1 = BigDecimal.valueOf((time2.getTime()-time1.getTime())/(1000*60)).divide(BigDecimal.valueOf(60),2,RoundingMode.HALF_EVEN);
								}
								al.setQtyOfHours1(QtyOfHours1);
								al.setTotalQtyOfHours(al.getTotalQtyOfHours().add(QtyOfHours1));
							}
							if(i==3) {
								time3 = formatTimeField(line.getDate());
								al.setTime3(time3);
							}
							if(i>=4) {
								time4 = formatTimeField(line.getDate());
								al.setTime4(time4);
								BigDecimal QtyOfHours2=BigDecimal.ZERO;
								if(time3!=null && time4!=null) {
									QtyOfHours2 = BigDecimal.valueOf((time4.getTime()-time3.getTime())/(1000*60)).divide(BigDecimal.valueOf(60),2,RoundingMode.HALF_EVEN);
								}
								al.setQtyOfHours2(QtyOfHours2);
								al.setTotalQtyOfHours(al.getTotalQtyOfHours().add(QtyOfHours2));
							}
						}
					}
					al.saveEx();
					if(count==sortedvalueDatelist.size())
						checkLastAttendanceTime(al.get_ID());
				}
				i++;
				if(i%5000==0)
					commitEx();
			}
			if(usersNotFoundList.length()>0) {
				attendance.set_ValueOfColumn("Description", "Usuarios no encontrados: "+usersNotFoundList);
				attendance.saveEx();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	protected String writeAttendance(String pathName) throws Exception{

		Timestamp time1=null,time2=null,time3=null,time4=null;
		String day = "",emp = "";
		int alID = 0,i = 1;

		StringBuilder clientCheck = new StringBuilder(" AND AD_Client_ID=").append(getAD_Client_ID());
		StringBuilder sql = new StringBuilder ("DELETE FROM HR_AttendanceLine ")
				.append("WHERE HR_Attendance_ID=").append(RECORD_ID).append (clientCheck);
		int	no = DB.executeUpdate(sql.toString(), get_TrxName());
		if (log.isLoggable(Level.FINE)) log.fine("Delete Attendance =" + no);
		MHR_Attendance attendance;
		if(RECORD_ID >0)
			attendance = new MHR_Attendance(getCtx(), RECORD_ID, get_TrxName());
		else
			attendance = new MHR_Attendance(getCtx(), 0, get_TrxName());

		String delimiter = "";
		if (p_FormatType.equals(X_AD_ImpFormat.FORMATTYPE_CommaSeparated)) {
			delimiter = ",";
		} else if (p_FormatType.equals(X_AD_ImpFormat.FORMATTYPE_TabSeparated)) {
			delimiter = "\t";
		} else {
			throw new IllegalArgumentException ("separador no Valido: " + p_FormatType);
		}

		File csvFile = new File (pathName);
		FileReader fileReader = new FileReader(csvFile);

		try (BufferedReader br = new BufferedReader(fileReader)) {
			int countlines=0;
			SimpleDateFormat dateTimeFormat = new SimpleDateFormat(p_DateTimeFormat, Locale.US);
			String inputLine;
			ArrayList<attendanceCsvLine> valueDatelist = new ArrayList<>();
			Date firstDate = null;

			Comparator<attendanceCsvLine> compareByName = Comparator
                    .comparing(attendanceCsvLine::getValue)
                    .thenComparing(attendanceCsvLine::getDate);

			attendance.setName(new Timestamp(System.currentTimeMillis()).toString());
			attendance.saveEx();

			while ((inputLine = br.readLine()) != null) {
				if(countlines==0 && p_HasHeader) {
					countlines++;
					continue;
				}
				if (inputLine.isEmpty() || inputLine.startsWith(String.valueOf(delimiter))) {
				    continue;
				}

				String[] csvLine = inputLine.split(String.valueOf(delimiter));
				if(csvLine.length<=1)
					throw new IllegalArgumentException ("Separador incorrecto: '" + delimiter+"'");
				String formattedDate = csvLine[dateIndex]
					    .replace("a.m.", "AM").replace("p.m.", "PM")
					    .replace("a. m.", "AM").replace("p. m.", "PM");
				formattedDate = formattedDate.replace("\"", "");

				Date parsedDateTime = dateTimeFormat.parse(formattedDate);
				firstDate = extraerFecha(parsedDateTime, dateTimeFormat);

				String bpCode = csvLine[bpIndex];
				bpCode = bpCode.replace("\"", "").trim();
				attendanceCsvLine atcsvLine = new attendanceCsvLine(bpCode,parsedDateTime);
				if(p_HasHoursColumns) {
					SimpleDateFormat formatTime = new SimpleDateFormat("HH:mm", Locale.US);
					int[] hourIndexes = {Hour1Index, Hour2Index, Hour3Index, Hour4Index};
				    Date[] parsedDateTimes = new Date[4];
				    for (int j = 0; j < hourIndexes.length; j++) {
				    	 int index = hourIndexes[i];
				         if (csvLine.length > index && csvLine[index] != null && !csvLine[index].isEmpty()) {
				             parsedDateTimes[i] = formatTime.parse(csvLine[index]
				            		 .replace("a.m.", "AM").replace("p.m.", "PM")
									 .replace("a. m.", "AM").replace("p. m.", "PM").replace("\"", ""));
				         }
				     }
					atcsvLine.setTime1(formatTimeField(parsedDateTimes[0]));
					atcsvLine.setTime2(formatTimeField(parsedDateTimes[1]));
					atcsvLine.setTime3(formatTimeField(parsedDateTimes[2]));
					atcsvLine.setTime4(formatTimeField(parsedDateTimes[3]));
				}
				valueDatelist.add(atcsvLine);
				countlines++;
			}
			fileReader.close();

		    List<attendanceCsvLine> sortedvalueDatelist = valueDatelist.stream()
                    .sorted(compareByName)
                    .collect(Collectors.toList());

		    sortedvalueDatelist = filterNearbyMarkings(sortedvalueDatelist, dateTimeFormat);

			String lastnotfoundbp = "";
			int count = 0;
			for (attendanceCsvLine line : sortedvalueDatelist) {
				count++;
				if(line.getValue() == null || line.getValue().trim().length() == 0) {
				    log.warning("Linea ignorada por codigo de empleado vacio");
				    continue;
				}
				Date parsedDateTime,parsedDate;
				parsedDateTime = line.getDate();
				parsedDate = extraerFecha(parsedDateTime, dateTimeFormat);
				if(day.compareTo(parsedDate.toString())!=0 || emp.compareTo(line.getValue().trim())!=0) {
					checkLastAttendanceTime(alID);
					i=1;

					StringBuilder whereclause = new StringBuilder();
					whereclause.append("REPLACE (trim(COALESCE(HR_ClockCode,taxid,value)), '-', '')= REPLACE (trim(?), '-', '')");
					whereclause.append(" AND IsEmployee='Y'");
					MBPartner employed = new Query(getCtx(), MBPartner.Table_Name,
							whereclause.toString(), get_TrxName()).setParameters(line.getValue())
							.setClient_ID()
							.first();
					if(employed==null) {
						if(lastnotfoundbp.compareTo(line.getValue())!=0) {
							lastnotfoundbp=line.getValue();
							usersNotFoundList.append(line.getValue()+",");
							log.warning("No se encuentra el empleado "+line.getValue());
						}
						continue;
					}

					MHR_AttendanceLine al = findExistingAttendanceLine(employed.getC_BPartner_ID(), parsedDate);
					if (al == null) {
						al = new MHR_AttendanceLine(getCtx(), 0, get_TrxName());
						al.setHR_Attendance_ID(attendance.get_ID());
						al.setC_BPartner_ID(employed.getC_BPartner_ID());
						al.setWeekDay(getWeekDayValue(parsedDate));
						al.setAttendanceDate(new Timestamp(parsedDate.getTime()));
						if(p_HasHoursColumns) {
							time1 = formatTimeField(line.getTime1());
							al.setTime1(time1);
							time2 = formatTimeField(line.getTime2());
							al.setTime2(time2);
							time3 = formatTimeField(line.getTime3());
							al.setTime3(time3);
							time4 = formatTimeField(line.getTime4());
							al.setTime4(time4);
						}
						else {
							time1 = formatTimeField(parsedDateTime);
							al.setTime1(time1);
						}
						try {
							al.saveEx();
						} catch (Exception e) {
							if (isUniqueViolation(e)) {
								MHR_AttendanceLine existing = findExistingAttendanceLine(employed.getC_BPartner_ID(), parsedDate);
								if (existing != null) {
                                    al = existing;
									al.setHR_Attendance_ID(attendance.get_ID());
									if (p_HasHoursColumns) {
										Timestamp t1 = formatTimeField(line.getTime1());
										Timestamp t2 = formatTimeField(line.getTime2());
										Timestamp t3 = formatTimeField(line.getTime3());
										Timestamp t4 = formatTimeField(line.getTime4());
										if (al.getTime1()==null && t1!=null) al.setTime1(t1);
										if (al.getTime2()==null && t2!=null) al.setTime2(t2);
										if (al.getTime3()==null && t3!=null) al.setTime3(t3);
										if (al.getTime4()==null && t4!=null) al.setTime4(t4);
									} else {
										Timestamp t1 = formatTimeField(parsedDateTime);
										if (al.getTime1()==null && t1!=null) al.setTime1(t1);
									}
									al.saveEx();
								} else {
									throw e;
								}
							} else {
								throw e;
							}
						}
					} else {
						al.setHR_Attendance_ID(attendance.get_ID());
						if(p_HasHoursColumns) {
							Timestamp t1 = formatTimeField(line.getTime1());
							Timestamp t2 = formatTimeField(line.getTime2());
							Timestamp t3 = formatTimeField(line.getTime3());
							Timestamp t4 = formatTimeField(line.getTime4());
							if (al.getTime1()==null && t1!=null) al.setTime1(t1);
							if (al.getTime2()==null && t2!=null) al.setTime2(t2);
							if (al.getTime3()==null && t3!=null) al.setTime3(t3);
							if (al.getTime4()==null && t4!=null) al.setTime4(t4);
						} else {
							Timestamp t1 = formatTimeField(parsedDateTime);
							if (al.getTime1()==null && t1!=null) al.setTime1(t1);
						}
						al.saveEx();
					}
					this.statusUpdate("Procesando: "+count+"/"+sortedvalueDatelist.size()+" "+employed.getValue()+" "+employed.getName()+" "+al.getAttendanceDate());
					day=parsedDate.toString();
					emp=line.getValue();
					alID=al.get_ID();
				}else {
					MHR_AttendanceLine al= new MHR_AttendanceLine(getCtx(), alID, get_TrxName());
					if(p_HasHoursColumns) {
					if(line.getTime1()!=null) {
						if(i==2) {
							time2 = formatTimeField(line.getTime2());
							al.setTime2(time2);
							BigDecimal QtyOfHours1=BigDecimal.ZERO;
							if(time1!=null && time2!=null) {
								QtyOfHours1 = BigDecimal.valueOf((time2.getTime()-time1.getTime())/(1000*60)).divide(BigDecimal.valueOf(60),2,RoundingMode.HALF_EVEN);
								}
								al.setQtyOfHours1(QtyOfHours1);
								al.setTotalQtyOfHours(al.getTotalQtyOfHours().add(QtyOfHours1));
							}
							if(i==3) {
								time3 = formatTimeField(line.getTime3());
								al.setTime3(time3);
							}
							if(i>=4) {
								time4 = formatTimeField(line.getTime4());
								al.setTime4(time4);
								BigDecimal QtyOfHours2=BigDecimal.ZERO;
								if(time3!=null && time4!=null) {
									QtyOfHours2 = BigDecimal.valueOf((time4.getTime()-time3.getTime())/(1000*60)).divide(BigDecimal.valueOf(60),2,RoundingMode.HALF_EVEN);
								}
								al.setQtyOfHours2(QtyOfHours2);
								al.setTotalQtyOfHours(al.getTotalQtyOfHours().add(QtyOfHours2));
							}
						}
					}
					else {
						if(line.getDate()!=null) {
							if(i==2) {
								time2 = formatTimeField(line.getDate());
								al.setTime2(time2);
								BigDecimal QtyOfHours1=BigDecimal.ZERO;
								if(time1!=null && time2!=null) {
									QtyOfHours1 = BigDecimal.valueOf((time2.getTime()-time1.getTime())/(1000*60)).divide(BigDecimal.valueOf(60),2,RoundingMode.HALF_EVEN);
								}
								al.setQtyOfHours1(QtyOfHours1);
								al.setTotalQtyOfHours(al.getTotalQtyOfHours().add(QtyOfHours1));
							}
							if(i==3) {
								time3 = formatTimeField(line.getDate());
								al.setTime3(time3);
							}
							if(i>=4) {
								time4 = formatTimeField(line.getDate());
								al.setTime4(time4);
								BigDecimal QtyOfHours2=BigDecimal.ZERO;
								if(time3!=null && time4!=null) {
									QtyOfHours2 = BigDecimal.valueOf((time4.getTime()-time3.getTime())/(1000*60)).divide(BigDecimal.valueOf(60),2,RoundingMode.HALF_EVEN);
								}
								al.setQtyOfHours2(QtyOfHours2);
								al.setTotalQtyOfHours(al.getTotalQtyOfHours().add(QtyOfHours2));
							}
						}
					}
					al.saveEx();
					if(count==sortedvalueDatelist.size())
						checkLastAttendanceTime(al.get_ID());
				}
				i++;
				if(i%5000==0)
					commitEx();
			}
			if(usersNotFoundList.length()>0) {
				attendance.set_ValueOfColumn("Description", "Usuarios no encontrados: "+usersNotFoundList);
				attendance.saveEx();
			}

			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
	        String dateTimeSuffix = LocalDateTime.now().format(formatter);

			String newFileName = ATTENDANCE_FILE_LOCATION + "/procesado/" + csvFile.getName().substring(0, csvFile.getName().length() - 4);
			newFileName = newFileName+"_"+dateTimeSuffix+".csv";

			File destDir = new File(ATTENDANCE_FILE_LOCATION + "/procesado/");
			if (!destDir.exists()) {
			    boolean dirCreated = destDir.mkdirs();
			    if (!dirCreated) {
			        log.severe("No se pudo crear la carpeta de destino: " + destDir.getAbsolutePath());
			        return "@Error@ No se pudo crear la carpeta de destino: " + destDir.getAbsolutePath();
			    }
			}

			File newFile = new File(newFileName);
			log.warning("Moviendo archivo a: " + newFile.getAbsolutePath());

			boolean moved = csvFile.renameTo(newFile);
			if (moved) {
			    log.info("Archivo movido exitosamente a: " + newFile.getAbsolutePath());
			} else {
			    log.severe("Error al mover el archivo a: " + newFile.getAbsolutePath());

			    try {
			        Files.copy(csvFile.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			        log.info("Archivo copiado exitosamente a: " + newFile.getAbsolutePath());

			        boolean deleted = csvFile.delete();
			        if (deleted) {
			            log.info("Archivo original eliminado después de copiar: " + csvFile.getAbsolutePath());
			        } else {
			            log.severe("Error al eliminar el archivo original: " + csvFile.getAbsolutePath());
			        }
			    } catch (IOException e) {
			        log.severe("Error al copiar el archivo: " + e.getMessage());
			        e.printStackTrace();
			    }
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	protected String writeAttendanceFromServer() throws Exception{
		ATTENDANCE_FILE_LOCATION = MSysConfig.getValue("ATTENDANCE_FILE_LOCATION", "/home/admin1/txt/", getAD_Client_ID());
		File directory = new File(ATTENDANCE_FILE_LOCATION);

		if (directory.exists() && directory.isDirectory()) {
		    File[] files = directory.listFiles();

		    if (files != null) {
		        for (File file : files) {
		            if (file.isFile()) {
		                System.out.println("Nombre del archivo: " + file.getName());
		                writeAttendance(file.getPath());
		            }
		        }
		    } else {
		        System.out.println("El directorio está vacío.");
		    }
		} else {
		    System.out.println("La ubicación del archivo no es un directorio válido.");
		}
		return "Proceso Terminado";
	}

	protected String getWeekDayValue(Date WeekDayStr) {
		Timestamp time = new Timestamp(WeekDayStr.getTime());
		LocalDateTime attendancedateaux = time.toLocalDateTime();
		List<MRefList> reflist = new Query(getCtx(), MRefList.Table_Name, "AD_Reference_ID=?",get_TrxName()).setParameters(167).list();

		for(MRefList ref : reflist) {
			if(ref.getName().trim().compareToIgnoreCase(attendancedateaux.getDayOfWeek().toString())==0)
				return ref.getValue();

		}
		return null;
	}

	protected class attendanceCsvLine {

	     public attendanceCsvLine(String string, Date parsedDateTime) {
	    	 value=string;
	    	 date=parsedDateTime;
		}

	     public attendanceCsvLine(String string, Date recordDate, Timestamp parsedDate1,Timestamp parsedDate2,
	    		 Timestamp parsedDate3,Timestamp parsedDate4) {
	    	 value=string;
	    	 date=recordDate;
	    	 time1=parsedDate1;
	    	 time2=parsedDate2;
	    	 time3=parsedDate3;
	    	 time4=parsedDate4;
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
		if (WeekDay.equals(null))
			return BigDecimal.ZERO;

		MGH_ShiftsLine shiftline = new Query(getCtx(), MGH_ShiftsLine.Table_Name, "GH_Shifts_ID=? AND WeekDay=?", get_TrxName()).setParameters(pShift.get_ID(),WeekDay).first();

		LocalDateTime shiftTime1 = prepareTime(attendanceline.getAttendanceDate(),shiftline.getTime1());
		LocalDateTime shiftTime2 = prepareTime(attendanceline.getAttendanceDate(),shiftline.getTime2());
		LocalDateTime shiftTime3 = prepareTime(attendanceline.getAttendanceDate(),shiftline.getTime3());
		LocalDateTime shiftTime4 = prepareTime(attendanceline.getAttendanceDate(),shiftline.getTime4());
		LocalDateTime shiftTime1WithoutTolerance = prepareTime(attendanceline.getAttendanceDate(),shiftline.getTime1());
		LocalDateTime shiftTime2WithoutTolerance = prepareTime(attendanceline.getAttendanceDate(),shiftline.getTime2());
		LocalDateTime shiftTime3WithoutTolerance = prepareTime(attendanceline.getAttendanceDate(),shiftline.getTime3());
		LocalDateTime shiftTime4WithoutTolerance = prepareTime(attendanceline.getAttendanceDate(),shiftline.getTime4());

		LocalDateTime attendanceTime1 = prepareTime(attendanceline.getAttendanceDate(),attendanceline.getTime1());
		attendanceTime1 = (attendanceTime1==null? shiftTime1 : attendanceTime1);
		LocalDateTime attendanceTime2 = prepareTime(attendanceline.getAttendanceDate(),attendanceline.getTime2());
		attendanceTime2 = (attendanceTime2==null? attendanceTime1 : attendanceTime2);
		LocalDateTime attendanceTime3 = prepareTime(attendanceline.getAttendanceDate(),attendanceline.getTime3());
		LocalDateTime attendanceTime4 = prepareTime(attendanceline.getAttendanceDate(),attendanceline.getTime4());

		if(shiftTime1==null || shiftTime2==null ||shiftTime3==null ||shiftTime4==null) return BigDecimal.ZERO;
		shiftTime1 = shiftTime1.plusMinutes(shiftline.getTolerance().longValue());
		shiftTime4 = shiftTime4.minusMinutes(shiftline.getTolerance().longValue());
		shiftTime2 = shiftTime2.minusMinutes(shiftline.getRestTolerance().longValue());
		shiftTime3 = shiftTime3.plusMinutes(shiftline.getRestTolerance().longValue());

		Duration duration = Duration.between(shiftTime1WithoutTolerance, attendanceTime1);

		double diffTime1 = duration.toMinutes();
		if(diffTime1 <= shiftline.getTolerance().doubleValue())
			duration = Duration.between(shiftTime1, attendanceTime1);

		diffTime1 = diffTime1 < 0.0 ?0.0:diffTime1;
		double diffTime2=0.0;
		double diffTime3=0.0;
		double diffTime4=0.0;
		if(attendanceline.getAuthorizationType()!=null && "LC".compareTo(attendanceline.getAuthorizationType())!=0) {
			shiftTime4 = shiftTime1WithoutTolerance.plusHours(8);
		}
			LocalDateTime attendanceTimeAux=shiftTime3WithoutTolerance;
			duration = Duration.between(attendanceTime2,shiftTime2WithoutTolerance);
			diffTime2 = duration.toMinutes();
			if(diffTime2 <= shiftline.getRestTolerance().doubleValue())
				duration = Duration.between(attendanceTime2,shiftTime2);

			diffTime2 = diffTime2 < 0.0 ?0.0:diffTime2;

			if(attendanceTime3!=null) {

				duration = Duration.between(shiftTime3WithoutTolerance,attendanceTime3);
				diffTime3 = duration.toMinutes();
				if(diffTime3 <= shiftline.getRestTolerance().doubleValue())
					duration = Duration.between(shiftTime3, attendanceTime3);

				diffTime3 = diffTime3 < 0.0 ?0.0:diffTime3;
			}else if(attendanceTime2.compareTo(shiftTime3)>0){
				attendanceTimeAux=attendanceTime2;
			}

			if(attendanceTime4!=null) {

				duration = Duration.between(attendanceTime4,shiftTime4);
				diffTime4 = duration.toMinutes();
				diffTime4 = diffTime4 < 0.0 ? 0.0:diffTime4;
			}

			if(attendanceTime3==null && attendanceTime4==null && attendanceTimeAux.compareTo(shiftTime4)<0) {
				duration= Duration.between(attendanceTimeAux,shiftTime4WithoutTolerance);
				diffTime4 =duration.toMinutes();
			}

		attendanceline.setQtyMinutesDifference1(BigDecimal.valueOf(diffTime1+diffTime2));
		attendanceline.setQtyMinutesDifference2(BigDecimal.valueOf(diffTime3+diffTime4));
		attendanceline.saveEx();

		BigDecimal diference = BigDecimal.valueOf((diffTime1+diffTime2+diffTime3+diffTime4)/60.0).setScale(2, RoundingMode.HALF_UP);
		log.severe("total"+String.valueOf(diffTime1+diffTime2+diffTime3+diffTime4));

		return (diference.compareTo(BigDecimal.ZERO)>0)? diference: BigDecimal.ZERO;
	}

	protected LocalDateTime prepareTime(Timestamp attendanceDate, Timestamp time1) {
		if(attendanceDate==null)
			return null;
		if(time1==null)
			return null;
		LocalDateTime aux = time1.toLocalDateTime();
		LocalDateTime time = attendanceDate.toLocalDateTime();
		time = time.plusHours(aux.getHour());
		time = time.plusMinutes(aux.getMinute());
		return time;
	}

	protected Timestamp formatTimeField(Date dateTime) {
		if(dateTime==null)
			return null;
		Calendar cal = Calendar.getInstance();
		cal.setTime(dateTime);
        cal.set(Calendar.YEAR, 1970);
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        cal.set(Calendar.DAY_OF_MONTH, 1);
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
            System.out.println("Error al formatear la fecha: " + e.getMessage());
            return null;
        }
    }

	protected void checkLastAttendanceTime(int id) {
		if (id <= 0) {
			return;
		}

		MHR_AttendanceLine al = new MHR_AttendanceLine(getCtx(), id, get_TrxName());
		if (al.get_ID() <= 0) {
			return;
		}

		if(al.getTime4()==null && al.getTime3()!=null) {
			al.setTime4(al.getTime3());
			al.setTime3(null);
		}
		else if(al.getTime4()==null && al.getTime3()==null && al.getTime2()!=null) {
			al.setTime4(al.getTime2());
			al.setTime2(null);
		}

		recalculateCollapsedHours(al);
		al.saveEx();
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

			boolean sameEmployee = current.getValue().trim()
					.equalsIgnoreCase(lastAccepted.getValue().trim());

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
	            if (msg.contains("SaveErrorNotUnique") ||
	                msg.contains("duplicate key value") ||
	                (msg.contains("restricci") && msg.contains("unicidad")) ||
	                msg.contains("nonduplicatedlines")) {
	                return true;
	            }
	        }
	        t = t.getCause();
	    }
	    return false;
	}
}