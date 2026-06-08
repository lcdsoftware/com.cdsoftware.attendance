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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.compiere.model.MRefList;
import org.compiere.model.Query;
import org.compiere.model.X_C_NonBusinessDay;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.Msg;

import com.cdsoftware.lirion.attendance.base.CustomProcess;
import com.cdsoftware.lirion.attendance.model.MGH_ShiftsLine;
import com.cdsoftware.lirion.attendance.model.MHR_Attendance;
import com.cdsoftware.lirion.attendance.model.MHR_AttendanceLine;
import com.cdsoftware.lirion.attendance.model.MHR_C_BPartnerShifts;

/**
 * Server process to automatically generate attendance lines for missing dates.
 * It identifies active employees in payroll for a specific date range,
 * checks their assigned shifts (MHR_C_BPartnerShifts), and creates attendance 
 * records (MHR_AttendanceLine) if they are missing and the day is not a rest 
 * day or holiday (C_NonBusinessDay).
 * 
 * This process can also be used to purge empty attendance records within a range.
 *
 * @author Ángel Lara
 */
@org.adempiere.base.annotation.Process
public class AddMissingDates extends CustomProcess{

	private int p_HR_Attendance_ID=0;
	private Timestamp p_DateFrom;
	private Timestamp p_DateTo;
	MHR_Attendance attendance;
	private boolean delete = false;

	/**
	 * Reads the process parameters required for missing date generation or purging.
	 * 
	 * Parameters:
	 * - HR_Attendance_ID: Specific attendance header record ID. If provided, the date range 
	 *                     is taken from this record's DateFrom and DateTo.
	 * - DateFrom: Start date of the range (and end date if no 'To' parameter is provided).
	 * - delete: If true, the process deletes empty attendance lines (TotalQtyOfHours = 0) 
	 *           and headers without lines instead of generating new ones.
	 */
	@Override
	protected void prepare() {
		ProcessInfoParameter[] parameters = getParameter();
		for (ProcessInfoParameter para: parameters)
		{
			String name = para.getParameterName();
			if (para.getParameter() == null) continue;
			else if (name.equals("HR_Attendance_ID"))
				p_HR_Attendance_ID = para.getParameterAsInt();
			else if(name.equals("DateFrom")) {
				p_DateFrom=para.getParameterAsTimestamp();
				p_DateTo=para.getParameter_ToAsTimestamp();
			}
			else if (name.equals("delete"))
				delete = para.getParameterAsBoolean();
		}

	}

	/**
	 * Executes the attendance record generation or deletion logic.
	 * 
	 * Logic flow:
	 * 1. If 'delete' is true:
	 *    - Removes HR_AttendanceLine records with zero hours in the date range.
	 *    - Removes HR_Attendance headers that no longer have lines.
	 * 2. If generating:
	 *    - Determines the date range (from parameters or HR_Attendance header).
	 *    - Filters out holidays (C_NonBusinessDay).
	 *    - For each remaining date, identifies active employees with assigned shifts.
	 *    - Creates an MHR_AttendanceLine for each missing date/employee combination 
	 *      if the day is not a rest day in their shift configuration.
	 * 
	 * @return A summary message of the operations performed.
	 * @throws Exception if an error occurs during database operations.
	 */
	@Override
	protected String doIt() throws Exception {
		if(delete) {
			Object[] params = new Object[]{p_DateFrom, p_DateTo, getAD_Client_ID()};
			int lines = DB.executeUpdate("DELETE FROM HR_AttendanceLine WHERE AttendanceDate BETWEEN ? AND ? AND TotalQtyOfHours = 0 AND AD_Client_ID = ?"
					,params
					,false
					,get_TrxName());
			
			int header = DB.executeUpdate("DELETE FROM HR_Attendance WHERE HR_Attendance_ID NOT IN (SELECT HR_Attendance_ID FROM HR_AttendanceLine WHERE AD_Client_ID = ?) AND AD_Client_ID = ?"
					,new Object[] {getAD_Client_ID(),getAD_Client_ID()}
					,false
					,get_TrxName());
			return "Borradas "+lines+" Lineas y "+header+" registros de asistencia.";
		}
		
		List<LocalDate> p_dateList=null;
		ZoneId defaultZoneId = ZoneId.systemDefault();
		int count=0;
		if(p_HR_Attendance_ID==0 && p_DateFrom == null && p_DateTo == null)
			return("@Error@: "+Msg.translate(Env.getCtx(), "AddMissingDatesNoParam"));
		
		MHR_Attendance attendanceHeader = null;
		if(p_HR_Attendance_ID>0) {
			attendanceHeader = new MHR_Attendance(getCtx(), p_HR_Attendance_ID, get_TrxName());
			if(attendanceHeader.getDateFrom()==null)
				return "@Error@La Asistencia no tiene Fecha desde, por favor verififique el registro";
			if(attendanceHeader.getDateTo()==null)
				return "@Error@La Asistencia no tiene Fecha hasta, por favor verififique el registro";
			p_dateList = getDatesBetween(attendanceHeader.getDateFrom().toLocalDateTime().toLocalDate(),attendanceHeader.getDateTo().toLocalDateTime().toLocalDate());
		}				
		else
			p_dateList = getDatesBetween(p_DateFrom.toLocalDateTime().toLocalDate(),p_DateTo.toLocalDateTime().toLocalDate());
		
		
		List<X_C_NonBusinessDay> dateListNonBussiness = new Query(getCtx(), "C_NonBusinessDay", "date1 BETWEEN ? AND ?",get_TrxName())
				.setClient_ID()
				.setOnlyActiveRecords(true)
				.setParameters(p_DateFrom,p_DateTo)
				.list();
		
		List<LocalDate> listNonBusiness = new ArrayList<>();
		for(X_C_NonBusinessDay NonBussiness:dateListNonBussiness) {
			listNonBusiness.add(NonBussiness.getDate1().toLocalDateTime().toLocalDate());
		}
		
        // Remove from the list of days the elements that are in the holiday list
		p_dateList.removeAll(listNonBusiness);
        
		for(LocalDate fecha:p_dateList) {
			this.statusUpdate("Analizando Dia "+fecha);
			Timestamp attendanceDate = Timestamp.valueOf(fecha.atStartOfDay());
			String weekDay = getWeekDayValue(Date.from(fecha.atStartOfDay(defaultZoneId).toInstant()));
			// For each date, check if the record exists or not
			if(p_HR_Attendance_ID==0) {
				attendanceHeader = new Query(getCtx(), MHR_Attendance.Table_Name,"HR_Attendance_ID IN (SELECT HR_Attendance_ID FROM HR_AttendanceLine "
						+ "WHERE AttendanceDate = '"+fecha+
						"' GROUP BY HR_Attendance_ID,AttendanceDate)", 
						get_TrxName()).first();
				if(attendanceHeader==null) {
					attendanceHeader = new MHR_Attendance(getCtx(), 0, get_TrxName());
					attendanceHeader.setDateFrom(Timestamp.valueOf(fecha.atStartOfDay()));
					attendanceHeader.setDateTo(Timestamp.valueOf(fecha.atStartOfDay()));
					attendanceHeader.saveEx();
				}
			}
			PreparedStatement pstmt = null;
			ResultSet rs = null;
			// Search for all employees that were in payroll for the date
			StringBuilder sql = new StringBuilder("SELECT Distinct ON (C_BPartner_ID) bp.C_BPartner_ID,bps.HR_C_BPartnerShifts_ID "
					+ " FROM C_BPartner bp "
					+ " LEFT JOIN AD_User u ON u.C_BPartner_ID=bp.C_BPartner_ID AND IsInPayroll='Y'"
					+ " JOIN (SELECT C_BPartner_ID FROM HR_Employee WHERE StartDate <= ? AND (EndDate is null OR EndDate >=?) AND isactive='Y' GROUP BY C_BPartner_ID) e ON e.C_BPartner_ID=bp.C_BPartner_ID "
					+ " JOIN HR_C_BPartnerShifts bps ON bps.C_BPartner_ID = bp.C_BPartner_ID AND (bps.DateFrom is null OR bps.DateFrom <=  ?) AND (bps.DateTo is null OR bps.DateTo >= ?)"
					+ " WHERE bp.IsEmployee='Y' AND bp.IsActive='Y'");		
			try
			{				
				pstmt = DB.prepareStatement(sql.toString(), get_TrxName());
				pstmt.setTimestamp(1, Timestamp.valueOf(fecha.atStartOfDay()));
				pstmt.setTimestamp(2, Timestamp.valueOf(fecha.atStartOfDay()));
				pstmt.setTimestamp(3, attendanceDate);
				pstmt.setTimestamp(4, attendanceDate);
				rs = pstmt.executeQuery();
				while (rs.next())
				{
					int c_bpartner_id = rs.getInt("C_BPartner_ID");		
					MHR_C_BPartnerShifts bpShift = new MHR_C_BPartnerShifts(getCtx(),  rs.getInt("HR_C_BPartnerShifts_ID"), this.get_TrxName());					
					String whereClauseBpShiftLine = "GH_Shifts_ID = "+bpShift.getGH_Shifts_ID()
							+ " AND RestDay !='Y' AND WeekDay = '"+weekDay+"'";
					
					
					
					MGH_ShiftsLine bpShiftLine = new Query(getCtx(), MGH_ShiftsLine.Table_Name, whereClauseBpShiftLine, this.get_TrxName()).first();
					if(bpShiftLine==null)
						continue;
					MHR_AttendanceLine attendanceLine = new Query(getCtx(), "HR_AttendanceLine",
							"C_BPartner_ID="+c_bpartner_id+
							" AND HR_Attendance_ID="+attendanceHeader.getHR_Attendance_ID()+
							" AND AttendanceDate='"+fecha+"'",
							get_TrxName()).first();
					if(attendanceLine==null) {
						count++;
						//log.warning("Tercero"+c_bpartner_id+" fecha "+fecha);
						MHR_AttendanceLine newline = new MHR_AttendanceLine(getCtx(),0,get_TrxName());
						newline.setC_BPartner_ID(c_bpartner_id);
						newline.setAttendanceDate(attendanceDate);
						newline.setQtyOfHours1(Env.ZERO);
						newline.setQtyOfHours2(Env.ZERO);
						newline.setTotalQtyOfHours(Env.ZERO);
						newline.setHR_Attendance_ID(attendanceHeader.getHR_Attendance_ID());
						newline.setWeekDay(weekDay);
						newline.saveEx();
					}
				}
			}
			catch (Exception e)
			{
				log.log(Level.SEVERE, sql.toString(), e);
				log.warning(e.getLocalizedMessage());
				DB.close(rs, pstmt);
				return e.getLocalizedMessage();
			}
			finally
			{
				DB.close(rs, pstmt);
			}
		}
		//log.warning("total emp="+count);
		return "Proceso Finalizado. Creados "+count+" Registros.";
	}

	/**
	 * Generates a list of all dates between the specified start and end dates.
	 * 
	 * @param startDate the beginning of the range
	 * @param endDate the end of the range
	 * @return list of dates inclusive of both bounds
	 */
	public static List<LocalDate> getDatesBetween(LocalDate startDate, LocalDate endDate) {
	    return startDate.datesUntil(endDate.plus(1,ChronoUnit.DAYS))
	    	      .collect(Collectors.toList());
	}

	/**
	 * Resolves the iDempiere reference value for a given date's day of the week.
	 * It maps Java's DayOfWeek to the values defined in AD Reference 167.
	 * 
	 * @param WeekDayStr the date to evaluate
	 * @return the value string from the reference list, or null if not found
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
	
}
