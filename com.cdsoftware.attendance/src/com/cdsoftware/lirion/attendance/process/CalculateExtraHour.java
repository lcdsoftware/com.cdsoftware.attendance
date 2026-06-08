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

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Properties;

import org.compiere.model.MBPartner;
import org.compiere.model.Query;
import org.compiere.process.ProcessInfo;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;

import com.cdsoftware.lirion.attendance.model.MGH_Shifts;
import com.cdsoftware.lirion.attendance.model.MGH_ShiftsLine;
import com.cdsoftware.lirion.attendance.model.MHR_AttendanceLine;
import com.cdsoftware.lirion.attendance.base.CustomProcess;
import com.cdsoftware.lirion.payroll.model.MHRAttribute;
import com.cdsoftware.lirion.payroll.model.MHRConcept;
import com.cdsoftware.lirion.payroll.model.MHRPeriod;
import com.cdsoftware.lirion.payroll.model.MHRProcess;

/**
 * Server process to calculate extra hours based on attendance records and assigned shifts.
 * It identifies diurnal, nocturnal, Sunday, and rest day extra hours, then 
 * inserts them as payroll attributes (MHRAttribute) for processing in payroll.
 * 
 * Identification strategy:
 * - Compares marking times (Time1, Time2) against the employee's shift boundaries.
 * - Uses a hardcoded diurnal limit (usually 18:00) to split extra hours.
 * - Triggers different payroll concepts based on the day type (working day, Sunday, rest day).
 * 
 * @author Casa del Software
 */
@org.adempiere.base.annotation.Process
public class CalculateExtraHour extends SvrProcess{

	private int C_BPartner_ID;
	private Timestamp p_AttendanceDate;
	private Timestamp p_AttendanceDate2;
	private Timestamp p_Time1;
	private Timestamp p_Time2;
	
	private int HR_Process_ID;
	private int HR_Attendance_ID;
	
	private static int PA_A_HORAS_EXTRAS_DIURNAS = 0;
	private static int PA_A_NOV_HORAS_EXTRAS_EXT_DIURNAS= 0;
	private static int PA_A_HORAS_EXTRAS_NOCT = 0;
	private static int PA_A_NOV_HORAS_EXTRAS_EXT_NOCT = 0;
	
	private static int PA_A_HORAS_DESCANSO_LABORADO = 0;
	private static int PA_A_HORAS_EXTRAS_DIURNAS_DES = 0;
	private static int PA_A_HORAS_EXTRAS_NOCT_DES = 0;
	
	private static int PA_A_NOV_HORAS_EXTRAS_DIURNAS_DOM = 0;
	private static int PA_A_HORAS_EXTRAS_NOCT_DOM = 0;
	private static int PA_A_HORAS_DOMINGO_TRABAJADAS = 0;

	/**
	 * Reads the process parameters required for extra hour calculation and 
	 * resolves the necessary payroll concept IDs.
	 * 
	 * Parameters:
	 * - AttendanceDate: The date for which extra hours are being calculated.
	 * - AttendanceDate2: The end date for the calculation (used for cross-day shifts).
	 * - C_BPartner_ID: The employee (Business Partner) record.
	 * - Time1: The clock-in time marking.
	 * - Time2: The clock-out time marking.
	 * - HR_Process_ID: The target payroll process ID where attributes will be created.
	 * - HR_Attendance_ID: The source attendance header record ID.
	 */
	@Override
	protected void prepare() {
		ProcessInfoParameter[] parameters = getParameter();
		for (ProcessInfoParameter para: parameters)
		{
			String name = para.getParameterName();
			if (para.getParameter() == null)
				;
			else if (name.equals("AttendanceDate")) {
				p_AttendanceDate = para.getParameterAsTimestamp();
				
			}else if (name.equals("AttendanceDate2")) {
				p_AttendanceDate2 = para.getParameterAsTimestamp();
			}else if (name.equals("C_BPartner_ID")) {
				C_BPartner_ID = para.getParameterAsInt();
				
			}else if (name.equals("Time1")) {
				p_Time1 = para.getParameterAsTimestamp();
				
			}else if (name.equals("Time2")) {
				p_Time2 = para.getParameterAsTimestamp();
				
			}else if (name.equals("HR_Process_ID")) {
				HR_Process_ID = para.getParameterAsInt();
				
			}else if (name.equals("HR_Attendance_ID")) {
				HR_Attendance_ID = para.getParameterAsInt();
				
			}
		}
		
		PA_A_HORAS_EXTRAS_DIURNAS = MHRConcept.getByValue(getCtx(), "PA_A_HORAS_EXTRAS_DIURNAS",get_TrxName()).getHR_Concept_ID();
		PA_A_NOV_HORAS_EXTRAS_EXT_DIURNAS= MHRConcept.getByValue(getCtx(), "PA_A_NOV_HORAS_EXTRAS_EXT_DIURNAS",get_TrxName()).getHR_Concept_ID();
		PA_A_HORAS_EXTRAS_NOCT = MHRConcept.getByValue(getCtx(), "PA_A_HORAS_EXTRAS_NOCT",get_TrxName()).getHR_Concept_ID();
		PA_A_NOV_HORAS_EXTRAS_EXT_NOCT = MHRConcept.getByValue(getCtx(), "PA_A_NOV_HORAS_EXTRAS_EXT_NOCT",get_TrxName()).getHR_Concept_ID();
		PA_A_HORAS_DESCANSO_LABORADO = MHRConcept.getByValue(getCtx(), "PA_A_HORAS_DESCANSO_LABORADO",get_TrxName()).getHR_Concept_ID();
		PA_A_HORAS_EXTRAS_DIURNAS_DES = MHRConcept.getByValue(getCtx(), "PA_A_HORAS_EXTRAS_DIURNAS_DES",get_TrxName()).getHR_Concept_ID();
		PA_A_HORAS_EXTRAS_NOCT_DES = MHRConcept.getByValue(getCtx(), "PA_A_HORAS_EXTRAS_NOCT_DES",get_TrxName()).getHR_Concept_ID();
		PA_A_NOV_HORAS_EXTRAS_DIURNAS_DOM = MHRConcept.getByValue(getCtx(), "PA_A_NOV_HORAS_EXTRAS_DIURNAS_DOM",get_TrxName()).getHR_Concept_ID();
		PA_A_HORAS_EXTRAS_NOCT_DOM = MHRConcept.getByValue(getCtx(), "PA_A_HORAS_EXTRAS_NOCT_DOM",get_TrxName()).getHR_Concept_ID();
		PA_A_HORAS_DOMINGO_TRABAJADAS = MHRConcept.getByValue(getCtx(), "PA_A_HORAS_DOMINGO_TRABAJADAS",get_TrxName()).getHR_Concept_ID();
	}

	/**
	 * Executes the logic to calculate and distribute extra hours.
	 * 
	 * Logic Flow:
	 * 1. Identifies the employee's shift (GH_Shifts_ID).
	 * 2. Retrieves shift configuration for the specific day of the week.
	 * 3. Handles rest day / Sunday logic (full hours as special concepts).
	 * 4. For working days:
	 *    - Splits worked hours into diurnal (before 18:00) and nocturnal (after 18:00).
	 *    - Caps extra hours at a standard limit (3 hours / 180 mins) and assigns 
	 *      remaining time to "extended" extra hour concepts.
	 * 5. Persists results as MHRAttribute records.
	 * 
	 * @return "Ok" string on success.
	 * @throws Exception if shifts configuration is missing for the day.
	 */
	@Override
	protected String doIt() throws Exception {
		MBPartner bpartner = new MBPartner(getCtx(), C_BPartner_ID, get_TrxName());
		int p_GH_Shifts_ID = bpartner.get_ValueAsInt("GH_Shifts_ID");
		
		if(p_GH_Shifts_ID == 0) {
			p_GH_Shifts_ID = 1000000; // TODO: Implement a 'default' checkbox in shifts to avoid hardcoding this ID
			
		}
		MGH_Shifts shift = new MGH_Shifts(getCtx(), p_GH_Shifts_ID , get_TrxName());
				
		// Get the number of days to evaluate
		LocalDateTime attendanceDate = p_AttendanceDate.toLocalDateTime();
		
		LocalDateTime maxDiurnalTime = attendanceDate.withHour(18);
		
		MGH_ShiftsLine shiftsLine = GetShiftLineByDayOfWeek(attendanceDate.getDayOfWeek(),shift);
		
		
		if(shiftsLine.equals(null))
			return "Error en el turno: No hay configuracion de horarios para el dia indicado";
		LocalDateTime time1 = p_Time1.toLocalDateTime();
		LocalDateTime time2 = p_Time2.toLocalDateTime();
				
		LocalDateTime time1WithDay = p_AttendanceDate.toLocalDateTime();
		time1WithDay=time1WithDay.plusHours(time1.getHour());
		time1WithDay=time1WithDay.plusMinutes(time1.getMinute());
		
		LocalDateTime time2WithDay = p_AttendanceDate2.toLocalDateTime();
		time2WithDay=time2WithDay.plusHours(time2.getHour());
		time2WithDay=time2WithDay.plusMinutes(time2.getMinute());
		
		if(shiftsLine.getTime3().equals(shiftsLine.getTime4())) {
			LocalDateTime shifttime2=shiftsLine.getTime2().toLocalDateTime();
		
			
			maxDiurnalTime = attendanceDate.withHour(shifttime2.getHour());
		}
		//LocalDateTime endOfShiftWithExtraHours = endOfShiftWithDay.plusMinutes(QtyExtraHours.longValue()*60);
		double maxTotalExtraMinutes = 180.0;
		Duration duration =Duration.between(time1WithDay, time2WithDay);
		double extraMinutes = duration.toMinutes();
		
		if(isRestDay(attendanceDate.getDayOfWeek(),shift)) {
			if(attendanceDate.getDayOfWeek().compareTo(DayOfWeek.SUNDAY)==0) {
				
				insertExtraHour(C_BPartner_ID,BigDecimal.valueOf(extraMinutes/60.0<=8.0?extraMinutes/60.0:8.0),PA_A_HORAS_DOMINGO_TRABAJADAS,
						new MHRProcess(getCtx(), HR_Process_ID, get_TrxName()),"Fecha: "+ time1WithDay.toString() );
			}else {
				
				insertExtraHour(C_BPartner_ID,BigDecimal.valueOf(extraMinutes/60.0<=8.0?extraMinutes/60.0:8.0),PA_A_HORAS_DESCANSO_LABORADO,
						new MHRProcess(getCtx(), HR_Process_ID, get_TrxName()),"Fecha: "+ time1WithDay.toString() );
			}
			// return "Ok";
		}
		
		
		if(maxDiurnalTime.compareTo(time1WithDay)>0) {
		
			if(maxDiurnalTime.compareTo(time2WithDay)<0) {
				
				duration = Duration.between(time1WithDay, maxDiurnalTime);

			}else {
				if(isRestDay(attendanceDate.getDayOfWeek(),shift)) {
					duration = Duration.ZERO;
				}
			}
			
		    double diffDiurnalMinutes = duration.toMinutes();
		    
		    
		    
		    if(diffDiurnalMinutes>0 && diffDiurnalMinutes <= extraMinutes) {
		    	if(isRestDay(attendanceDate.getDayOfWeek(),shift)) {
					if(attendanceDate.getDayOfWeek().compareTo(DayOfWeek.SUNDAY)==0) {
						
						insertExtraHour(C_BPartner_ID,BigDecimal.valueOf((diffDiurnalMinutes>maxTotalExtraMinutes?
				    			maxTotalExtraMinutes:diffDiurnalMinutes)/60.0),PA_A_NOV_HORAS_EXTRAS_DIURNAS_DOM,
								new MHRProcess(getCtx(), HR_Process_ID, get_TrxName()),"Fecha: "+ time1WithDay.toString() );
					}else {
						
						insertExtraHour(C_BPartner_ID,BigDecimal.valueOf((diffDiurnalMinutes>maxTotalExtraMinutes?
				    			maxTotalExtraMinutes:diffDiurnalMinutes)/60.0),PA_A_HORAS_EXTRAS_DIURNAS_DES,
								new MHRProcess(getCtx(), HR_Process_ID, get_TrxName()),"Fecha: "+ time1WithDay.toString() );
					}
					
				}else {
					insertExtraHour(C_BPartner_ID,BigDecimal.valueOf((diffDiurnalMinutes>maxTotalExtraMinutes?
		    			maxTotalExtraMinutes:diffDiurnalMinutes)/60.0),PA_A_HORAS_EXTRAS_DIURNAS,
		    			new MHRProcess(getCtx(), HR_Process_ID, get_TrxName()),"Fecha: "+ time1WithDay.toString() );
				}
				
		    	maxTotalExtraMinutes = maxTotalExtraMinutes - diffDiurnalMinutes;
		    }
		    if(maxTotalExtraMinutes<0) {
		    	time1WithDay= time1WithDay.plusHours(3);
		    	duration =Duration.between(time1WithDay, time2WithDay);
		    	if(maxDiurnalTime.compareTo(time2WithDay)<0) {
					
					duration = Duration.between(time1WithDay, maxDiurnalTime);

				}
		    	diffDiurnalMinutes = duration.toMinutes();
		    	
		    
				insertExtraHour(C_BPartner_ID,BigDecimal.valueOf(diffDiurnalMinutes/60.0),PA_A_NOV_HORAS_EXTRAS_EXT_DIURNAS,
						new MHRProcess(getCtx(), HR_Process_ID, get_TrxName()),"Fecha: "+ time1WithDay.toString() );
		    	
		    }
		}
	    
	    duration = Duration.between(maxDiurnalTime,time2WithDay);
	    double diffNightTime = duration.toMinutes();
	    if(diffNightTime>0 && maxTotalExtraMinutes >0) {
	    	if(isRestDay(attendanceDate.getDayOfWeek(),shift)) {
				if(attendanceDate.getDayOfWeek().compareTo(DayOfWeek.SUNDAY)==0) {
					
					insertExtraHour(C_BPartner_ID,BigDecimal.valueOf((diffNightTime>maxTotalExtraMinutes?
			    			maxTotalExtraMinutes:diffNightTime)/60.0),PA_A_HORAS_EXTRAS_NOCT_DOM,
							new MHRProcess(getCtx(), HR_Process_ID, get_TrxName()),"Fecha: "+ time1WithDay.toString() );
				}else {
					
					insertExtraHour(C_BPartner_ID,BigDecimal.valueOf((diffNightTime>maxTotalExtraMinutes?
			    			maxTotalExtraMinutes:diffNightTime)/60.0),PA_A_HORAS_EXTRAS_NOCT_DES,
							new MHRProcess(getCtx(), HR_Process_ID, get_TrxName()),"Fecha: "+ time1WithDay.toString() );
				}
				
			}else {
				insertExtraHour(C_BPartner_ID,BigDecimal.valueOf((diffNightTime>maxTotalExtraMinutes?
	    			maxTotalExtraMinutes:diffNightTime)/60.0),PA_A_HORAS_EXTRAS_NOCT,new MHRProcess(getCtx(), HR_Process_ID, get_TrxName()),
	    			"Fecha: "+ time1WithDay.toString() );
			}
	    }
	    
	    if(diffNightTime>maxTotalExtraMinutes) {
	    	insertExtraHour(C_BPartner_ID,BigDecimal.valueOf((diffNightTime-(maxTotalExtraMinutes>0?
	    			maxTotalExtraMinutes:0.0))/60.0),PA_A_NOV_HORAS_EXTRAS_EXT_NOCT,new MHRProcess(getCtx(), HR_Process_ID, get_TrxName()),
	    			"Fecha: "+ time1WithDay.toString() );
	    }
	    
	    
	    
		return "Ok";
	}

	/**
	 * Checks if a specific day is configured as a rest day in the given shift.
	 * 
	 * @param dayOfWeek day to check
	 * @param shift shift configuration
	 * @return true if it is a rest day
	 */
	private boolean isRestDay(DayOfWeek dayOfWeek,MGH_Shifts shift) {
		List<MGH_ShiftsLine> sl = new Query(getCtx(), MGH_ShiftsLine.Table_Name, "RestDay='Y' AND GH_Shifts_ID="+shift.get_ID(), get_TrxName()).list();
		
		for(MGH_ShiftsLine sline : sl){
			
			if(dayOfWeek.getValue()==Integer.valueOf(sline.getWeekDay()))
				return true;
		}
		return false;
	}

	/**
	 * Retrieves the shift line configuration for a specific day of the week.
	 * 
	 * @param dayOfWeek day to look for
	 * @param pShift shift configuration
	 * @return the corresponding shift line
	 */
	private MGH_ShiftsLine GetShiftLineByDayOfWeek(DayOfWeek dayOfWeek, MGH_Shifts pShift) {
		MGH_ShiftsLine sl = new Query(getCtx(), MGH_ShiftsLine.Table_Name, "GH_Shifts_ID=? AND WeekDay=?", get_TrxName()).setParameters(pShift.get_ID(),String.valueOf(dayOfWeek.getValue())).first();
		
		return sl;
	}
	
	/**
	 * Calculates the difference between expected shift hours and actual attendance hours.
	 * 
	 * @param attendanceline attendance line to evaluate
	 * @return the difference in hours, accounting for tolerance
	 */
	protected BigDecimal getDifference(MHR_AttendanceLine attendanceline) {
		String WeekDay = attendanceline.getWeekDay();
		if (WeekDay.equals(null))
			return BigDecimal.ZERO;
		MGH_ShiftsLine shiftline = new Query(getCtx(), MGH_ShiftsLine.Table_Name, "WeekDay=?", get_TrxName()).setParameters(WeekDay).first();
		BigDecimal totalshift = shiftline.getQtyOfHours1().add(shiftline.getQtyOfHours2());
		BigDecimal diference = totalshift.subtract(attendanceline.getTotalQtyOfHours());
		diference = diference.subtract(shiftline.getTolerance());
		return (diference.compareTo(BigDecimal.ZERO)>0)? diference: BigDecimal.ZERO;
	}

	/**
	 * Inserts a calculated extra hour amount into the payroll attributes (MHRAttribute).
	 * 
	 * @param C_BPartner_ID employee ID
	 * @param extraHour amount of hours
	 * @param concept_id payroll concept ID
	 * @param payrollProcess active payroll process
	 * @param Description description for the attribute
	 */
	protected void insertExtraHour(int C_BPartner_ID, BigDecimal extraHour,int concept_id,MHRProcess payrollProcess,String Description) {
		MBPartner employed = new MBPartner(getCtx(),C_BPartner_ID , get_TrxName());
		if(!employed.isActive() || extraHour.compareTo(BigDecimal.ZERO)<=0)
			return;
		
		MHRPeriod p = new MHRPeriod(getCtx(),payrollProcess.getPayrollPeriod(),get_TrxName());
		MHRAttribute attribute = new MHRAttribute(getCtx(), 0, get_TrxName());
		attribute.setValidFrom(p.getStartDate());
		attribute.setValidTo(p.getStartDate());
		attribute.setColumnType(MHRAttribute.COLUMNTYPE_Amount);
		attribute.setAmount(extraHour.setScale(2, BigDecimal.ROUND_HALF_EVEN));
		attribute.setHR_Concept_ID(concept_id);
		attribute.setC_BPartner_ID(employed.getC_BPartner_ID());
		attribute.setAD_Org_ID(payrollProcess.getAD_Org_ID()); 
		attribute.setDescription(Description);
		attribute.set_ValueOfColumn("HR_Attendance_ID", HR_Attendance_ID);
		attribute.saveEx();
	}
	
}
