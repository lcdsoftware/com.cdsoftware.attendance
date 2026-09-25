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
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

import org.compiere.model.MBPartner;
import org.compiere.model.MRefList;
import org.compiere.model.MSysConfig;
import org.compiere.model.Query;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.DB;

import com.cdsoftware.lirion.attendance.model.MGH_ShiftsLine;
import com.cdsoftware.lirion.attendance.model.MHR_Attendance;
import com.cdsoftware.lirion.attendance.model.MHR_AttendanceLine;
import com.cdsoftware.lirion.attendance.model.MMarking;

/**
 * Process to import attendance markings from a CSV file located on the server.
 * The file location is defined by the system configuration "ATTENDANCE_FILE_LOCATION".
 * It expects a specific CSV structure for Bioadmin devices.
 * 
 * CSV structure:
 * [0] Employee Code (TaxID)
 * [1] Marking Timestamp
 * 
 * @author Casa del Software
 */
@org.adempiere.base.annotation.Process
public class ImportAttendanceBioadmin extends SvrProcess{

	private String ATTENDANCE_FILE_LOCATION="";
	private int RECORD_ID=0;
    /**
     * Prepares the process by reading parameters and initializing the record ID.
     */
	@Override
	protected void prepare() {
		ProcessInfoParameter[] parameters = getParameter();
		for (ProcessInfoParameter para: parameters)
		{
			String name = para.getParameterName();
			if (para.getParameter() == null)
				;

		}
		RECORD_ID = getRecord_ID();
	}

    /**
     * Searches for attendance files in the configured directory and processes each one.
     * 
     * @return Process termination message.
     * @throws Exception if processing fails.
     */
	@Override
	protected String doIt() throws Exception {
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
		//writeAttendance();
		return "Proceso Terminado";
	}

    /**
     * Legacy method to write attendance from a fixed "Marcacion.csv" file.
     * 
     * @return null or status message.
     * @throws Exception if an error occurs.
     */
	public String writeAttendance() throws Exception{
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
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			//SimpleDateFormat dateFormat2 = new SimpleDateFormat("dd/MM/yyyy");
			SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyy/MM/dd");
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

			Collections.sort(lineList);
			String line0=lineList.get(0);
			String[] csvLine0 = line0.split(cvsSplitBy);
			Date parsedDate0;
			parsedDate0 = dateFormat2.parse(csvLine0[1]);
			
			attendance.setDateFrom(new Timestamp(parsedDate0.getTime()));
			attendance.setDateTo(new Timestamp(parsedDate0.getTime()));
			attendance.setName(parsedDate0.toString());
			attendance.saveEx();
			
			for (String line : lineList) {

				if(line.length()==0)
					break;
				
				// use comma as separator
				String[] csvLine = line.split(cvsSplitBy);
				Date parsedDate;
				parsedDate = dateFormat2.parse(csvLine[1]);

				if(day.compareTo(dateFormat2.parse(csvLine[1]).toString())!=0 || emp.compareTo(csvLine[0].trim())!=0) {
					i=1;
					MHR_AttendanceLine al= new MHR_AttendanceLine(getCtx(), 0, get_TrxName());
					al.setHR_Attendance_ID(attendance.get_ID());
					MBPartner employed = new Query(getCtx(), MBPartner.Table_Name, "REPLACE (trim(taxid), '-', '')=?", get_TrxName()).setParameters(csvLine[0]).first();
					if(employed==null) {
						log.severe("No se encuentra el empleado "+csvLine[0]);
						log.warning("@Error@ No se encuentra el empleado "+csvLine[0]);
					}else {
					
					al.setC_BPartner_ID(employed.getC_BPartner_ID());
					parsedDate = dateFormat2.parse(csvLine[1]);
					//Date parsedDateaux = dateFormat2.parse(csvLine[1].replace("\"", ""));
					al.setWeekDay(getWeekDayValue(parsedDate));


					al.setAttendanceDate(new Timestamp(parsedDate.getTime()));
					//if(i==1) {
					parsedDate = dateFormat.parse(csvLine[1]);
					time1 = new Timestamp(parsedDate.getTime()); 
					al.setTime1(time1);
					//}

					//}

					al.saveEx();
					this.statusUpdate("Procesando: "+employed.getValue()+" "+employed.getName()+" "+al.getAttendanceDate());
					day=dateFormat2.parse(csvLine[1].replace("\"", "")).toString();
					emp=csvLine[0];
					alID=al.get_ID();
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
			String newfile=ATTENDANCE_FILE_LOCATION+"procesado/Marcacion-"+parsedDate0.toString()+".csv";
			log.warning("Moviendo archivo a :"+newfile);
			csvFile.renameTo(new File(newfile));
			//agregamos los trabajadores que no marcaron
			

		} catch (IOException e) {
			e.printStackTrace();
		}


		return null;
	}

    /**
     * Processes an attendance CSV file from the given path.
     * It sorts markings by employee and date, then creates HR_AttendanceLine records.
     * 
     * @param pathName Absolute path to the CSV file.
     * @return null or error message.
     * @throws Exception if an error occurs.
     */
	public String writeAttendance(String pathName) throws Exception{
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
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
			//SimpleDateFormat dateFormat2 = new SimpleDateFormat("dd/MM/yyyy");
			SimpleDateFormat dateFormat2 = new SimpleDateFormat("dd-MM-yyyy");
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
			
		       // Personaliza el comparador para ordenar primero por la primera columna y luego por la fecha
	        Comparator<String> dateComparator = new Comparator<String>() {
	            @Override
	            public int compare(String s1, String s2) {
	                String[] parts1 = s1.split(",");
	                String[] parts2 = s2.split(",");

	                // Compara la primera columna
	                int idComparison = parts1[0].compareTo(parts2[0]);

	                if (idComparison == 0) {
	                    // Si las primeras columnas son iguales, compara por fecha
	                    try {
	                        Date date1 = dateFormat.parse(parts1[1]);
	                        Date date2 = dateFormat.parse(parts2[1]);
	                        return date1.compareTo(date2);
	                    } catch (ParseException e) {
	                        e.printStackTrace();
	                        return 0; // Manejo de errores
	                    }
	                }

	                return idComparison;
	            }
	        };
	        Collections.sort(lineList, dateComparator);
	        
			String line0=lineList.get(0);
			String[] csvLine0 = line0.split(cvsSplitBy);
			Date parsedDate0;
			parsedDate0 = dateFormat2.parse(csvLine0[1]);
			
			attendance.setDateFrom(new Timestamp(parsedDate0.getTime()));
			attendance.setDateTo(new Timestamp(parsedDate0.getTime()));
			attendance.setName(parsedDate0.toString());
			attendance.saveEx();
			
			for (String line : lineList) {

				if(line.length()==0)
					break;
				
				// use comma as separator
				String[] csvLine = line.split(cvsSplitBy);
				Date parsedDate;
				parsedDate = dateFormat2.parse(csvLine[1]);

				if(day.compareTo(dateFormat2.parse(csvLine[1]).toString())!=0 || emp.compareTo(csvLine[0].trim())!=0) {
					i=1;
					MHR_AttendanceLine al= new MHR_AttendanceLine(getCtx(), 0, get_TrxName());
					al.setHR_Attendance_ID(attendance.get_ID());
					MBPartner employed = new Query(getCtx(), MBPartner.Table_Name, "REPLACE (trim(taxid), '-', '')=REPLACE (trim('"+csvLine[0]+"'), '-', '')", get_TrxName()).first();
					if(employed==null) {
						//log.severe("No se encuentra el empleado "+csvLine[0]);
						this.addLog("No se encuentra el empleado "+csvLine[0]);
						log.warning("@Error@ No se encuentra el empleado "+csvLine[0]);
					}else {
					
					al.setC_BPartner_ID(employed.getC_BPartner_ID());
					parsedDate = dateFormat2.parse(csvLine[1]);
					//Date parsedDateaux = dateFormat2.parse(csvLine[1].replace("\"", ""));
					al.setWeekDay(getWeekDayValue(parsedDate));


					al.setAttendanceDate(new Timestamp(parsedDate.getTime()));
					//if(i==1) {
					parsedDate = dateFormat.parse(csvLine[1]);
					time1 = new Timestamp(parsedDate.getTime()); 
					al.setTime1(time1);
					//}

					//}

					al.saveEx();
					this.statusUpdate("Procesando: "+employed.getValue()+" "+employed.getName()+" "+al.getAttendanceDate());
					day=dateFormat2.parse(csvLine[1].replace("\"", "")).toString();
					emp=csvLine[0];
					alID=al.get_ID();
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
			String newfile=ATTENDANCE_FILE_LOCATION+"procesado/"+csvFile.getName().substring(0,  (int)(csvFile.getName().length())-4)+"-"+parsedDate0.toString()+".csv";
			log.warning("Moviendo archivo a :"+newfile);
			csvFile.renameTo(new File(newfile));
			//agregamos los trabajadores que no marcaron
			

		} catch (IOException e) {
			e.printStackTrace();
		}


		return null;
	}
	
    /**
     * Calculates the difference between shift hours and actual worked hours for a marking.
     * 
     * @param attendance The marking record.
     * @return Positive difference representing lateness, or zero.
     */
	protected BigDecimal getDifference(MMarking attendance) {
		String WeekDay = getWeekDayValue(attendance.getWeekDayStr());
		if (WeekDay.equals(null))
			return BigDecimal.ZERO;
		MGH_ShiftsLine shiftline = new Query(getCtx(), MGH_ShiftsLine.Table_Name, "WeekDay=?", get_TrxName()).setParameters(WeekDay).first();
		BigDecimal diference = shiftline.getQtyOfHours1().subtract(attendance.getQtyOfHours1());
		diference = diference.subtract(shiftline.getTolerance());
		return (diference.compareTo(BigDecimal.ZERO)>0)? diference: BigDecimal.ZERO;
	}

    /**
     * Calculates the difference between shift hours and actual worked hours for an attendance line.
     * 
     * @param attendance The attendance line record.
     * @param Shift_ID The shift ID to compare against.
     * @return Positive difference representing lateness, or zero.
     */
	protected BigDecimal getDifference(MHR_AttendanceLine attendance,int Shift_ID ) {
		String WeekDay = getWeekDayValue(attendance.getWeekDay());
		if (WeekDay.equals(null))
			return BigDecimal.ZERO;
		MGH_ShiftsLine shiftline = new Query(getCtx(), MGH_ShiftsLine.Table_Name, "WeekDay=? and "+MGH_ShiftsLine.COLUMNNAME_GH_Shifts_ID+"=?", get_TrxName()).setParameters(WeekDay,Shift_ID).first();
		BigDecimal diference = shiftline.getQtyOfHours1().subtract(attendance.getQtyOfHours1());
		diference = diference.subtract(shiftline.getTolerance());
		return (diference.compareTo(BigDecimal.ZERO)>0)? diference: BigDecimal.ZERO;
	}

    /**
     * Converts a Date to its corresponding WeekDay reference value.
     * 
     * @param WeekDayStr The date to convert.
     * @return The reference value for the day of the week, or null if not found.
     */
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
	
    /**
     * Converts a string day name to its corresponding WeekDay reference value.
     * 
     * @param WeekDayStr The day name (English).
     * @return The reference value for the day of the week, or null if not found.
     */
	protected String getWeekDayValue(String WeekDayStr) {

		List<MRefList> reflist = new Query(getCtx(), MRefList.Table_Name, "AD_Reference_ID=?",get_TrxName()).setParameters(167).list();

		for(MRefList ref : reflist) {
			if(ref.getName().trim().compareToIgnoreCase(WeekDayStr)==0) 
				return ref.getValue();

		}
		return null;

	}	

}
