package com.cdsoftware.lirion.attendance.process;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
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
 * [bpIndex]	Codigo de Empleado: Primer no nulo de (HR_ClockCode,taxid,value)
 * [dateIndex]	fecha marcacion
 * [2]			Hora Entrada (opcional)	
 * [3]			Hora Salida Almuerzo (opcional)
 * [4]			Hora regreso Almuerzo (opcional)
 * [5]			Hora Salida (opcional)
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
	
	@Override
	protected void prepare() {
		// TODO Auto-generated method stub
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
			//SimpleDateFormat dateFormat = new SimpleDateFormat(p_DateFormat, Locale.US);
			SimpleDateFormat dateTimeFormat = new SimpleDateFormat(p_DateTimeFormat, Locale.US);
			String inputLine;
			ArrayList<attendanceCsvLine> valueDatelist = new ArrayList<>();
			
			//we need to order the file by employee an date
		    Comparator<attendanceCsvLine> compareByName = Comparator
                    .comparing(attendanceCsvLine::getValue)	
                    .thenComparing(attendanceCsvLine::getDate);
			while ((inputLine = br.readLine()) != null) {
				//ignoring header line
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
			String lastnotfoundbp = "";
			int count = 0;
			for (attendanceCsvLine line : sortedvalueDatelist) {
				count++;
				if(line.getValue().length()==0)
					break;
				Date parsedDateTime,parsedDate;
				parsedDateTime = line.getDate();
				//parsedDate = dateFormat.parse(dateFormat.format(parsedDateTime));
				parsedDate = extraerFecha(parsedDateTime, dateTimeFormat);
				if(day.compareTo(parsedDate.toString())!=0 || emp.compareTo(line.getValue().trim())!=0) {
					//Cambio de dia o de tercero, valido momento de la ultima marcacion, si no fue en la salida ajusto la ultima marcación a hora de salida
					checkLastAttendanceTime(alID);
					i=1;
					MHR_AttendanceLine al= new MHR_AttendanceLine(getCtx(), 0, get_TrxName());
					al.setHR_Attendance_ID(attendance.get_ID());
					StringBuilder whereclause = new StringBuilder();
					whereclause.append("REPLACE (trim(COALESCE(HR_ClockCode,taxid,value)), '-', '')= REPLACE (trim(?), '-', '')");
					//Realizar busqueda solo en terceros activos y que tengan el check de colaborador
					whereclause.append(" AND IsEmployee='Y' AND Isactive='Y'");
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
					al.saveEx();
					this.statusUpdate("Procesando: "+count+"/"+sortedvalueDatelist.size()+" "+employed.getValue()+" "+employed.getName()+" "+parsedDateTime);
					day=parsedDate.toString();
					emp=line.getValue();
					alID=al.get_ID();
				}else {
					MHR_AttendanceLine al= new MHR_AttendanceLine(getCtx(), alID, get_TrxName());
					if(p_HasHoursColumns) {
						if(line.getTime1()!=null) {
							if(i==2) {
								//time2 = formatTimeField(parsedDateTime);
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
								//time3 = formatTimeField(parsedDateTime);
								time3 = formatTimeField(line.getTime3());
								al.setTime3(time3);
							}
							if(i>=4) {
								//time4 = formatTimeField(parsedDateTime);
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
		
		//delete old lines
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
			
			//we need to order the file by employee an date
		    Comparator<attendanceCsvLine> compareByName = Comparator
                    .comparing(attendanceCsvLine::getValue)	
                    .thenComparing(attendanceCsvLine::getDate);
		    
			attendance.setName(new Timestamp(System.currentTimeMillis()).toString());
			attendance.saveEx();
			
			
			while ((inputLine = br.readLine()) != null) {
				//ignoring header line
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
				String bpCode=csvLine[bpIndex];
				bpCode = bpCode.replace("\"", "");
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
			String lastnotfoundbp = "";
			int count = 0;
			for (attendanceCsvLine line : sortedvalueDatelist) {
				count++;
				if(line.getValue().length()==0)
					break;
				Date parsedDateTime,parsedDate;
				parsedDateTime = line.getDate();
				//parsedDate = dateFormat.parse(dateFormat.format(parsedDateTime));
				parsedDate = extraerFecha(parsedDateTime, dateTimeFormat);
				if(day.compareTo(parsedDate.toString())!=0 || emp.compareTo(line.getValue().trim())!=0) {
					//Cambio de dia o de tercero, valido momento de la ultima marcacion, si no fue en la salida ajusto la ultima marcación a hora de salida
					checkLastAttendanceTime(alID);
					i=1;
					MHR_AttendanceLine al= new MHR_AttendanceLine(getCtx(), 0, get_TrxName());
					al.setHR_Attendance_ID(attendance.get_ID());
					StringBuilder whereclause = new StringBuilder();
					whereclause.append("REPLACE (trim(COALESCE(HR_ClockCode,taxid,value)), '-', '')= REPLACE (trim(?), '-', '')");
					//Realizar busqueda solo en terceros activos y que tengan el check de colaborador
					whereclause.append(" AND IsEmployee='Y' AND Isactive='Y'");
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
					al.saveEx();
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
								//time3 = formatTimeField(parsedDateTime);
								time3 = formatTimeField(line.getTime3());
								al.setTime3(time3);
							}
							if(i>=4) {
								//time4 = formatTimeField(parsedDateTime);
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
			// Mover el archivo CSV a la carpeta procesado
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
	        String dateTimeSuffix = LocalDateTime.now().format(formatter);
	        
			String newFileName = ATTENDANCE_FILE_LOCATION + "/procesado/" + csvFile.getName().substring(0, csvFile.getName().length() - 4);
			newFileName = newFileName+"_"+dateTimeSuffix+".csv";;
			
			// Verificar si la carpeta de destino existe, si no, crearla
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

			// Intentar mover el archivo
			boolean moved = csvFile.renameTo(newFile);
			if (moved) {
			    log.info("Archivo movido exitosamente a: " + newFile.getAbsolutePath());
			} else {
			    log.severe("Error al mover el archivo a: " + newFile.getAbsolutePath());

			    // Intentar copiar el archivo como alternativa
			    try {
			        Files.copy(csvFile.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			        log.info("Archivo copiado exitosamente a: " + newFile.getAbsolutePath());
			        
			        // Eliminar el archivo original después de copiar
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
		    /*  // Mover el archivo CSV a la carpeta procesado
	        String newfile = ATTENDANCE_FILE_LOCATION + "procesado/" + csvFile.getName().substring(0, (int)(csvFile.getName().length()) - 4) + ".csv";
	        log.warning("Moviendo archivo a :" + newfile);
	        boolean moved = csvFile.renameTo(new File(newfile));
	        if (moved) {
	            log.info("Archivo movido exitosamente a: " + newfile);
	        } else {
	            log.severe("Error al mover el archivo a: " + newfile);
	        }
	        
			/*String newfile=ATTENDANCE_FILE_LOCATION+"procesado/"+csvFile.getName().substring(0,  (int)(csvFile.getName().length())-4)+"-"+firstDate.toString()+".csv";
			log.warning("Moviendo archivo a :"+newfile);
			csvFile.renameTo(new File(newfile));*/
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
		                // Aquí puedes hacer algo con cada archivo encontrado
		                // Por ejemplo, imprimir el nombre de cada archivo:
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

	
	/*protected BigDecimal getDifference(MMarking attendance) {
		// TODO Auto-generated method stub
		String WeekDay = getWeekDayValue(attendance.getWeekDayStr());
		if (WeekDay.equals(null))
			return BigDecimal.ZERO;
		MGH_ShiftsLine shiftline = new Query(getCtx(), MGH_ShiftsLine.Table_Name, "WeekDay=?", get_TrxName()).setParameters(WeekDay).first();
		BigDecimal diference = shiftline.getQtyOfHours1().subtract(attendance.getQtyOfHours1());
		diference = diference.subtract(shiftline.getTolerance());
		return (diference.compareTo(BigDecimal.ZERO)>0)? diference: BigDecimal.ZERO;
	}*/



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
		// TODO Auto-generated method stub
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
		
		//Boolean LunchTolerance=true;
		//Boolean ExitTolerance=true;
		LocalDateTime attendanceTime1 = prepareTime(attendanceline.getAttendanceDate(),attendanceline.getTime1());
		attendanceTime1 = (attendanceTime1==null? shiftTime1 : attendanceTime1);
		LocalDateTime attendanceTime2 = prepareTime(attendanceline.getAttendanceDate(),attendanceline.getTime2());
		//if(attendanceTime2!=null) 
		//LunchTolerance=true;
		attendanceTime2 = (attendanceTime2==null? attendanceTime1 : attendanceTime2);
		LocalDateTime attendanceTime3 = prepareTime(attendanceline.getAttendanceDate(),attendanceline.getTime3());
		//attendanceTime3 = (attendanceTime3 == null? shiftTime3 : attendanceTime3);
		/*if(attendanceTime3 == null) {
			if(attendanceTime2.compareTo(shiftTime3)>0) {
				attendanceTime3=attendanceTime2;
				attendanceTime2=shiftTime3;
			}
		}*/
		LocalDateTime attendanceTime4 = prepareTime(attendanceline.getAttendanceDate(),attendanceline.getTime4());
		//if(attendanceTime4!=null) 
		//ExitTolerance=true;
		//attendanceTime4 = (attendanceTime4 == null? attendanceTime3 : attendanceTime4);

		if(shiftTime1==null || shiftTime2==null ||shiftTime3==null ||shiftTime4==null) return BigDecimal.ZERO;
		//modify time1 and time4 with tolerance 
		//if(ExitTolerance)
			shiftTime1 = shiftTime1.plusMinutes(shiftline.getTolerance().longValue());
		//if(ExitTolerance)
			shiftTime4 = shiftTime4.minusMinutes(shiftline.getTolerance().longValue());

		//modify time2 and time3 with  restTolerance 
		//if(LunchTolerance)
			shiftTime2 = shiftTime2.minusMinutes(shiftline.getRestTolerance().longValue());
		//if(LunchTolerance)
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
		// TODO Auto-generated method stub
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
            // Usar Calendar para eliminar la parte de la hora
            Calendar cal = Calendar.getInstance();
            cal.setTime(fecha);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);            
            // Devolver solo la parte de la fecha
            return cal.getTime();
        } catch (Exception e) {
            System.out.println("Error al formatear la fecha: " + e.getMessage());
            return null;
        }
    }

	protected void checkLastAttendanceTime(int id) {
		MHR_AttendanceLine al= new MHR_AttendanceLine(getCtx(), id, get_TrxName());
		//Check if the last hour has data
		if(al.getTime4()==null && al.getTime3()!=null) {
			al.setTime4(al.getTime3());
			al.setTime3(null);						
			al.saveEx();
		}
		else if(al.getTime4()==null && al.getTime3()==null && al.getTime2()!=null) {
			al.setTime4(al.getTime2());
			al.setTime2(null);
			al.setQtyOfHours1(Env.ZERO);
			al.saveEx();
		}			
	}
	
}
