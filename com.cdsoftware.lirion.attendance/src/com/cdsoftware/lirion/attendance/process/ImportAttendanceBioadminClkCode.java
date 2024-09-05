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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

import org.compiere.model.MBPartner;
import org.compiere.model.MRefList;
import org.compiere.model.MSysConfig;
import org.compiere.model.Query;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.DB;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.cdsoftware.lirion.attendance.model.MGH_ShiftsLine;
import com.cdsoftware.lirion.attendance.model.MHR_Attendance;
import com.cdsoftware.lirion.attendance.model.MHR_AttendanceLine;
import com.cdsoftware.lirion.attendance.model.MMarking;

/**
 * Proceso para importar archivo de marcaciones desde una ubicación en el servidor
 * la ruta de los archivos esta indicada por la variable del sistema ATTENDANCE_FILE_LOCATION
 * estructura del csv
 * [0]	dump
 * [1]	codigo tercero
 * [2]	fecha marcacion
 * @author angel
 *
 */
@org.adempiere.base.annotation.Process
public class ImportAttendanceBioadminClkCode extends SvrProcess{

	private String ATTENDANCE_FILE_LOCATION="";
	private int RECORD_ID=0;
	@Override
	protected void prepare() {
		// TODO Auto-generated method stub
		ProcessInfoParameter[] parameters = getParameter();
		for (ProcessInfoParameter para: parameters)
		{
			String name = para.getParameterName();
			if (para.getParameter() == null)
				;

		}
		RECORD_ID = getRecord_ID();
	}

	@Override
	protected String doIt() throws Exception {
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
		//writeAttendance();
		return "Proceso Terminado";
	}

	public String writeAttendance() throws Exception{
		// TODO Auto-generated method stub
		//MBPartner bp = new MBPartner(getCtx(), C_BPartner_ID, get_TrxName());
		StringBuilder clientCheck = new StringBuilder(" AND AD_Client_ID=").append(getAD_Client_ID());
		StringBuilder sql = new StringBuilder ("DELETE FROM HR_AttendanceLine ")
				.append("WHERE HR_Attendance_ID=").append(RECORD_ID).append (clientCheck);
		int	no = DB.executeUpdate(sql.toString(), get_TrxName());
		if (log.isLoggable(Level.FINE)) log.fine("Delete Attendance =" + no);		
		//List<MMarking> listattendance = new Query(getCtx(),MMarking.Table_Name,"MarkingDate between ? and ? AND QtyOfHours1 is not null ",get_TrxName()).setParameters(MarkingDateFrom,MarkingDateTo).setOrderBy("Value").list();
		MHR_Attendance attendance;
		if(RECORD_ID >0)
			attendance = new MHR_Attendance(getCtx(), RECORD_ID, get_TrxName());
		else 
			attendance = new MHR_Attendance(getCtx(), 0, get_TrxName());
		
		String ATTENDANCE_FILE_LOCATION=MSysConfig.getValue("ATTENDANCE_FILE_LOCATION", "/home/admin1/txt/", getAD_Client_ID());
		File csvFile = new File (ATTENDANCE_FILE_LOCATION+"Marcacion.csv");//entry.getFile();
		log.severe(csvFile.getName());
		
		//String line = "";
		String cvsSplitBy = ",";
		FileReader fileReader = new FileReader(csvFile);
		try (BufferedReader br = new BufferedReader(fileReader)) {
			String day = "";
			String emp = "";
			int alID = 0;
			//SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			//SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			//SimpleDateFormat dateFormat2 = new SimpleDateFormat("dd/MM/yyyy");
			//SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyy/MM/dd");
			SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy h:mm a", Locale.getDefault());  // Formato con AM/PM y horas con dos dígitos
            SimpleDateFormat dateFormat2 = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());  // Solo fecha
			int i = 1;
			Timestamp time1=null;
			Timestamp time2=null;
			Timestamp time3=null;
			Timestamp time4=null;
			int countlines=0;
			
			
			//we need to order the file by employee an date
			String inputLine;
			List<String> lineList = new ArrayList<String>();
			
			while ((inputLine = br.readLine()) != null) {
				//System.out.println("Leyendo línea: " + inputLine); // Imprimir el contenido de cada línea leída
				//ignoring header line
				if(countlines==0) {
					countlines++;
					continue;					
				}
				
				if(inputLine.length()==0)
					continue;
				String cleanLine = inputLine.replaceAll("\"", "");
				lineList.add(cleanLine);
				countlines++;
				
			}
			fileReader.close();

			Collections.sort(lineList);
			String line0=lineList.get(0);
			String[] csvLine0 = line0.split(cvsSplitBy);
			Date parsedDate0;
			parsedDate0 = dateFormat2.parse(csvLine0[1]);
			
			attendance.setDateFrom(new Timestamp(parsedDate0.getTime()));
			//attendance.setDateTo(new Timestamp(parsedDate0.getTime()));
			attendance.setName(parsedDate0.toString());
			attendance.saveEx();
			Date parsedDate = null;
			for (String line : lineList) {

				if(line.length()==0)
					break;
				
				// use comma as separator
				String[] csvLine = line.split(cvsSplitBy);
				//Date parsedDate;
				parsedDate = dateFormat2.parse(csvLine[1]);

				if(day.compareTo(dateFormat2.parse(csvLine[1]).toString()) != 0 || emp.compareTo(csvLine[0].trim()) != 0) {
				    i = 1;
				    MHR_AttendanceLine al = new MHR_AttendanceLine(getCtx(), 0, get_TrxName());
				    al.setHR_Attendance_ID(attendance.get_ID());

				    String hrClockCode = csvLine[0].trim();
				    int employedID = DB.getSQLValueEx(get_TrxName(), 
				                                      "SELECT C_BPartner_ID FROM C_BPartner WHERE hr_clockcode = ?", 
				                                      hrClockCode);

				    if (employedID <= 0) {
				        log.severe("No se encuentra el empleado " + csvLine[0]);
				        log.warning("@Error@ No se encuentra el empleado " + csvLine[0]);
				    } else {
				        // Cargar el objeto MBPartner usando el ID
				        MBPartner employed = new MBPartner(getCtx(), employedID, get_TrxName());

				        al.setC_BPartner_ID(employed.getC_BPartner_ID());
				        parsedDate = dateFormat2.parse(csvLine[1]);
				        al.setWeekDay(getWeekDayValue(parsedDate));

				        al.setAttendanceDate(new Timestamp(parsedDate.getTime()));
				        parsedDate = dateFormat.parse(csvLine[1]);
				        time1 = new Timestamp(parsedDate.getTime()); 
				        al.setTime1(time1);

				        al.saveEx();
				        this.statusUpdate("Procesando: " + employed.getValue() + " " + employed.getName() + " " + al.getAttendanceDate());
				        day = dateFormat2.parse(csvLine[1].replace("\"", "")).toString();
				        emp = csvLine[0];
				        alID = al.get_ID();
				    }
				

				}else {
					MHR_AttendanceLine al= new MHR_AttendanceLine(getCtx(), alID, get_TrxName());
					if(csvLine.length>=2) {
						parsedDate = dateFormat2.parse(csvLine[1]);

						if(i==2) {
							parsedDate = dateFormat.parse(csvLine[1]);
							time2 = new Timestamp(parsedDate.getTime());
							al.setTime2(time2);
							BigDecimal QtyOfHours1=BigDecimal.ZERO;
							if(time1!=null && time2!=null) {
								QtyOfHours1 = BigDecimal.valueOf((time2.getTime()-time1.getTime())/(1000*60)).divide(BigDecimal.valueOf(60),2,RoundingMode.HALF_EVEN);
								
							}
							al.setQtyOfHours1(QtyOfHours1);
							al.setTotalQtyOfHours(al.getTotalQtyOfHours().add(QtyOfHours1));
							
						}


						if(i==3) {
							parsedDate = dateFormat.parse(csvLine[1]);
							time3 = new Timestamp(parsedDate.getTime());
							al.setTime3(time3);
						}

						if(i>=4) {
							//if(csvLine[6]!=null) {
							parsedDate = dateFormat.parse(csvLine[1].replace("\"", ""));
							time4 = new Timestamp(parsedDate.getTime());
							al.setTime4(time4);
							//}

							BigDecimal QtyOfHours2=BigDecimal.ZERO;
							if(time3!=null && time4!=null) {
								QtyOfHours2 = BigDecimal.valueOf((time4.getTime()-time3.getTime())/(1000*60)).divide(BigDecimal.valueOf(60),2,RoundingMode.HALF_EVEN);

							}
							al.setQtyOfHours2(QtyOfHours2);
							al.setTotalQtyOfHours(al.getTotalQtyOfHours().add(QtyOfHours2));
							
						}
					}
					al.saveEx();


				}
				i++;
			}
			attendance.setDateTo(new Timestamp(parsedDate.getTime()));
			attendance.saveEx();
		      // Mover el archivo CSV a la carpeta procesado
	        String newfile = ATTENDANCE_FILE_LOCATION + "procesado/" + csvFile.getName().substring(0, (int)(csvFile.getName().length()) - 4) + "-" + parsedDate0.toString() + ".csv";
	        log.warning("Moviendo archivo a :" + newfile);
	        boolean moved = csvFile.renameTo(new File(newfile));
	        if (moved) {
	            log.info("Archivo movido exitosamente a: " + newfile);
	        } else {
	            log.severe("Error al mover el archivo a: " + newfile);
	        }
	        
			//String newfile=ATTENDANCE_FILE_LOCATION+"procesado/Marcacion-"+parsedDate0.toString()+".csv";
			//log.warning("Moviendo archivo a :"+newfile);
			//csvFile.renameTo(new File(newfile));
			//agregamos los trabajadores que no marcaron
			

		} catch (IOException e) {
			e.printStackTrace();
		}


		return null;
	}

	public String writeAttendance(String pathName) throws Exception{
		// TODO Auto-generated method stub
		//MBPartner bp = new MBPartner(getCtx(), C_BPartner_ID, get_TrxName());
		StringBuilder clientCheck = new StringBuilder(" AND AD_Client_ID=").append(getAD_Client_ID());
		StringBuilder sql = new StringBuilder ("DELETE FROM HR_AttendanceLine ")
				.append("WHERE HR_Attendance_ID=").append(RECORD_ID).append (clientCheck);
		int	no = DB.executeUpdate(sql.toString(), get_TrxName());
		if (log.isLoggable(Level.FINE)) log.fine("Delete Attendance =" + no);		
		//List<MMarking> listattendance = new Query(getCtx(),MMarking.Table_Name,"MarkingDate between ? and ? AND QtyOfHours1 is not null ",get_TrxName()).setParameters(MarkingDateFrom,MarkingDateTo).setOrderBy("Value").list();
		MHR_Attendance attendance;
		if(RECORD_ID >0)
			attendance = new MHR_Attendance(getCtx(), RECORD_ID, get_TrxName());
		else 
			attendance = new MHR_Attendance(getCtx(), 0, get_TrxName());		
		File csvFile = new File (pathName);
		log.severe(csvFile.getName());
		
		//String line = "";
		String cvsSplitBy = ",";
		FileReader fileReader = new FileReader(csvFile);
		try (BufferedReader br = new BufferedReader(fileReader)) {
			String day = "";
			String emp = "";
			int alID = 0;
			//SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			//SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
			//SimpleDateFormat dateFormat2 = new SimpleDateFormat("dd/MM/yyyy");
			//SimpleDateFormat dateFormat2 = new SimpleDateFormat("dd-MM-yyyy");
			SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy h:mm a", Locale.getDefault());  
            SimpleDateFormat dateFormat2 = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());  // Solo fecha
			int i = 1;
			Timestamp time1=null;
			Timestamp time2=null;
			Timestamp time3=null;
			Timestamp time4=null;
			int countlines=0;
			
			
			//we need to order the file by employee an date
			String inputLine;
			List<String> lineList = new ArrayList<String>();
			while ((inputLine = br.readLine()) != null) {
				//ignoring header line
				if(countlines==0) {
					countlines++;
					continue;					
				}
				if(inputLine.length()==0)
					continue;
				lineList.add(inputLine);
				countlines++;
				
			}
			fileReader.close();
			if(lineList.size()==0)
				return "@Error@No hay archivos validos en el directorio";
			
		      
			Comparator<String> dateComparator = new Comparator<String>() {
			    @Override
			    public int compare(String s1, String s2) {
			        String[] parts1 = s1.split(",");
			        String[] parts2 = s2.split(",");

			        // Asegura de que no haya espacios adicionales
			        String id1 = parts1[0].replaceAll("\"", "").trim();
			        String dateStr1 = parts1[1].replaceAll("\"", "").trim();
			        String id2 = parts2[0].replaceAll("\"", "").trim();
			        String dateStr2 = parts2[1].replaceAll("\"", "").trim();
			        

			        //System.out.println("Procesando parte 1: " + Arrays.toString(parts1));
			        //System.out.println("Procesando parte 2: " + Arrays.toString(parts2));

			        // Compara la primera columna
			        int idComparison = id1.compareTo(id2);

			        if (idComparison == 0) {
			            // Si las primeras columnas son iguales, compara por fecha
			            try {
			                //System.out.println("Fecha parte 1: " + dateStr1);
			                //System.out.println("Fecha parte 2: " + dateStr2);

			                // Intenta analizar la fecha con múltiples formatos usando Joda-Time
			                String cleanDateStr1 = dateStr1.replace("\"", "");
			                String cleanDateStr2 = dateStr2.replace("\"", "");
			                DateTime date1 = parseDate(cleanDateStr1);
			                DateTime date2 = parseDate(cleanDateStr2);

			                return date1.compareTo(date2);
			            } catch (IllegalArgumentException e) {
			                e.printStackTrace();
			                System.err.println("Error al analizar la fecha: " + e.getMessage());
			                //System.err.println("Fecha parte 1: " + dateStr1);
			                //System.err.println("Fecha parte 2: " + dateStr2);
			                return 0; 
			            }
			        }

			        return idComparison;
			    }
			};
	        Collections.sort(lineList, dateComparator);
	        
			String line0=lineList.get(0);
			String[] csvLine0 = line0.replaceAll("\"", "").split(cvsSplitBy);
			Date parsedDate0;
			String cleancsvLine0 = csvLine0[1].replace("\"", "");
			parsedDate0 = dateFormat2.parse(cleancsvLine0);
			
			attendance.setDateFrom(new Timestamp(parsedDate0.getTime()));
			//attendance.setDateTo(new Timestamp(parsedDate0.getTime()));
			attendance.setName(parsedDate0.toString());
			attendance.saveEx();
			Date parsedDate = null;
			for (String line : lineList) {

			    if(line.length() == 0)
			        break;

			    // use comma as separator
			    String[] csvLine = line.replace("\"", "").split(cvsSplitBy);

			    //Date parsedDate;
			    //System.out.println("Linea del cvs, tiene comillas o no?" + csvLine[1]);
			    parsedDate = dateFormat2.parse(csvLine[1]);
			    

			    if(day.compareTo(dateFormat2.parse(csvLine[1]).toString()) != 0 || emp.compareTo(csvLine[0].trim()) != 0) {
			        i = 1;
			        MHR_AttendanceLine al = new MHR_AttendanceLine(getCtx(), 0, get_TrxName());
			        al.setHR_Attendance_ID(attendance.get_ID());

			        String hrClockCode = csvLine[0].trim();
			        int employedID = DB.getSQLValueEx(get_TrxName(), 
			                                          "SELECT C_BPartner_ID FROM C_BPartner WHERE hr_clockcode = ?", 
			                                          hrClockCode);

			        if (employedID <= 0) {
			            log.severe("No se encuentra el empleado " + csvLine[0]);
			            log.warning("@Error@ No se encuentra el empleado " + csvLine[0]);
			        } else {
			        	// Cargar el objeto MBPartner usando el ID
			        	MBPartner employed = new MBPartner(getCtx(), employedID, get_TrxName());

			        	al.setC_BPartner_ID(employed.getC_BPartner_ID());
			        	parsedDate = dateFormat2.parse(csvLine[1]);
			        	al.setWeekDay(getWeekDayValue(parsedDate));

			        	al.setAttendanceDate(new Timestamp(parsedDate.getTime()));
			        	//System.err.println("Acá está dando el error ahora ojo: " + csvLine[1]);

			        	try {
			        	    DateTime parsedDateTime = parseDate(csvLine[1]);
			        	    time1 = new Timestamp(parsedDateTime.getMillis());
			        	    al.setTime1(time1);
			        	} catch (IllegalArgumentException e) {
			        	    log.severe("Error al analizar la fecha: " + csvLine[1]);
			        	    continue; // saltar esta línea en caso de error
			        	}

			        	al.saveEx();
			        	this.statusUpdate("Procesando: " + employed.getValue() + " " + employed.getName() + " " + al.getAttendanceDate());
			        	day = dateFormat2.parse(csvLine[1].replace("\"", "")).toString();
			        	emp = csvLine[0];
			        	alID = al.get_ID();

			        }
			    } else {
			        MHR_AttendanceLine al = new MHR_AttendanceLine(getCtx(), alID, get_TrxName());
			        if(csvLine.length >= 2) {
			            //parsedDate = dateFormat2.parse(csvLine[1]);
			        	DateTime parsedDate1 = parseDate(csvLine[1]);

			            if(i == 2) {
			            	Timestamp time21 = new Timestamp(parsedDate1.getMillis());
		                    al.setTime2(time21);
		                    BigDecimal QtyOfHours1 = BigDecimal.ZERO;
		                    if (time1 != null && time21 != null) {
		                        QtyOfHours1 = BigDecimal.valueOf((time21.getTime() - time1.getTime()) / (1000 * 60)).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_EVEN);
		                    }
		                    al.setQtyOfHours1(QtyOfHours1);
		                    al.setTotalQtyOfHours(al.getTotalQtyOfHours().add(QtyOfHours1));
			                
			            }

			            if(i == 3) {
			            	Timestamp time31 = new Timestamp(parsedDate1.getMillis());
		                    al.setTime3(time31);
			            }

			            if(i >= 4) {
			            	Timestamp time41 = new Timestamp(parsedDate1.getMillis());
		                    al.setTime4(time41);

		                    BigDecimal QtyOfHours2 = BigDecimal.ZERO;
		                    if (time3 != null && time41 != null) {
		                        QtyOfHours2 = BigDecimal.valueOf((time41.getTime() - time3.getTime()) / (1000 * 60)).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_EVEN);
			                }
			                al.setQtyOfHours2(QtyOfHours2);
			                al.setTotalQtyOfHours(al.getTotalQtyOfHours().add(QtyOfHours2));
			            }
			        }
			        al.saveEx();
			    }
			    i++;
			}
			attendance.setDateTo(new Timestamp(parsedDate.getTime()));
			attendance.saveEx();
			// Mover el archivo CSV a la carpeta procesado
			String newFileName = ATTENDANCE_FILE_LOCATION + "/procesado/" + csvFile.getName().substring(0, csvFile.getName().length() - 4) + "-" + parsedDate0.toString() + ".csv";

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
			//String newfile=ATTENDANCE_FILE_LOCATION+"procesado/"+csvFile.getName().substring(0,  (int)(csvFile.getName().length())-4)+"-"+parsedDate0.toString()+".csv";
			//log.warning("Moviendo archivo a :"+newfile);
			//csvFile.renameTo(new File(newfile));
			//agregamos los trabajadores que no marcaron
			

		} catch (IOException e) {
			e.printStackTrace();
		}


		return null;
	}
	
	protected BigDecimal getDifference(MMarking attendance) {
		// TODO Auto-generated method stub
		String WeekDay = getWeekDayValue(attendance.getWeekDayStr());
		if (WeekDay.equals(null))
			return BigDecimal.ZERO;
		MGH_ShiftsLine shiftline = new Query(getCtx(), MGH_ShiftsLine.Table_Name, "WeekDay=?", get_TrxName()).setParameters(WeekDay).first();
		BigDecimal diference = shiftline.getQtyOfHours1().subtract(attendance.getQtyOfHours1());
		diference = diference.subtract(shiftline.getTolerance());
		return (diference.compareTo(BigDecimal.ZERO)>0)? diference: BigDecimal.ZERO;
	}

	protected BigDecimal getDifference(MHR_AttendanceLine attendance,int Shift_ID ) {
		// TODO Auto-generated method stub
		String WeekDay = getWeekDayValue(attendance.getWeekDay());
		if (WeekDay.equals(null))
			return BigDecimal.ZERO;
		MGH_ShiftsLine shiftline = new Query(getCtx(), MGH_ShiftsLine.Table_Name, "WeekDay=? and "+MGH_ShiftsLine.COLUMNNAME_GH_Shifts_ID+"=?", get_TrxName()).setParameters(WeekDay,Shift_ID).first();
		BigDecimal diference = shiftline.getQtyOfHours1().subtract(attendance.getQtyOfHours1());
		diference = diference.subtract(shiftline.getTolerance());
		return (diference.compareTo(BigDecimal.ZERO)>0)? diference: BigDecimal.ZERO;
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
	
	protected String getWeekDayValue(String WeekDayStr) {

		List<MRefList> reflist = new Query(getCtx(), MRefList.Table_Name, "AD_Reference_ID=?",get_TrxName()).setParameters(167).list();

		for(MRefList ref : reflist) {
			if(ref.getName().trim().compareToIgnoreCase(WeekDayStr)==0) 
				return ref.getValue();

		}
		return null;

	}	
	// Método para intentar analizar la fecha con múltiples formatos usando Joda-Time
    private DateTime parseDate(String dateStr) {
        List<DateTimeFormatter> dateFormats = Arrays.asList(
            DateTimeFormat.forPattern("MM/dd/yyyy h:mm a").withLocale(Locale.US),
            DateTimeFormat.forPattern("MM/dd/yyyy hh:mm a").withLocale(Locale.US)
        );

        for (DateTimeFormatter format : dateFormats) {
            try {
                return format.parseDateTime(dateStr);
            } catch (IllegalArgumentException e) {
                System.err.println("Error al analizar con formato: " + format.toString() + " Fecha: " + dateStr);
                // Ignorar y probar con el siguiente formato
            }
        }
        throw new IllegalArgumentException("Fecha no analizable: " + dateStr);
    }
};
	
	
	

