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
import org.compiere.process.ProcessInfoParameter;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.Msg;

import com.cdsoftware.lirion.attendance.base.CustomProcess;
import com.cdsoftware.lirion.attendance.model.MHR_Attendance;
import com.cdsoftware.lirion.attendance.model.MHR_AttendanceLine;

@org.adempiere.base.annotation.Process
public class AddMissingDates extends CustomProcess{


	private int p_HR_Attendance_ID=0;
	private Timestamp p_DateFrom;
	private Timestamp p_DateTo;
	MHR_Attendance attendance;
	@Override
	protected void prepare() {
		// TODO Auto-generated method stub
		ProcessInfoParameter[] parameters = getParameter();
		for (ProcessInfoParameter para: parameters)
		{
			String name = para.getParameterName();
			if (para.getParameter() == null)
				;
			else if (name.equals("HR_Attendance_ID"))
				p_HR_Attendance_ID = para.getParameterAsInt();
			else if(name.equals("DateFrom")) {
				p_DateFrom=para.getParameterAsTimestamp();
				p_DateTo=para.getParameter_ToAsTimestamp();
			}
		}

	}

	@Override
	protected String doIt() throws Exception {
		//MHR_Attendance at=null;
		//List<MHR_Attendance> atlist=new ArrayList<MHR_Attendance>();
		List<LocalDate> p_dateList=null;
		ZoneId defaultZoneId = ZoneId.systemDefault();
		int count=0;
		if(p_HR_Attendance_ID>0) {
			//atlist.add(new MHR_Attendance(getCtx(), p_HR_Attendance_ID, get_TrxName()));

		}
		else if(p_DateFrom!=null && p_DateTo!=null) {
			/*atlist = new Query(getCtx(), MHR_Attendance.Table_Name,"1=1 "+
					(p_DateFrom==null?"":" AND DateFrom <= '"+p_DateFrom+"'")+
					(p_DateTo==null?"":" AND DateTo >= '"+p_DateTo+"'"), 
					get_TrxName()).list();*/
			//El listado de fechas debe basarse en las lineas no puede buscar la información en la cabecera
			/*atlist = new Query(getCtx(), MHR_Attendance.Table_Name,"HR_Attendance_ID IN (SELECT HR_Attendance_ID FROM HR_AttendanceLine "
					+ "WHERE AttendanceDate >= '"+p_DateFrom+"' AND AttendanceDate <= '"+p_DateTo+
					"' GROUP BY HR_Attendance_ID,AttendanceDate)", 
					get_TrxName()).list();*/
		}
		else
			return("@Error@: "+Msg.translate(Env.getCtx(), "AddMissingDatesNoParam"));
		
		MHR_Attendance at = null;
		if(p_HR_Attendance_ID>0) {
			at = new MHR_Attendance(getCtx(), p_HR_Attendance_ID, get_TrxName());
			if(at.getDateFrom()==null)
				return "@Error@La Asistencia no tiene Fecha desde, por favor verififique el registro";
			if(at.getDateTo()==null)
				return "@Error@La Asistencia no tiene Fecha hasta, por favor verififique el registro";
			p_dateList = getDatesBetween(at.getDateFrom().toLocalDateTime().toLocalDate(),at.getDateTo().toLocalDateTime().toLocalDate());
		}				
		else
			p_dateList = getDatesBetween(p_DateFrom.toLocalDateTime().toLocalDate(),p_DateTo.toLocalDateTime().toLocalDate());
		
		for(LocalDate fecha:p_dateList) {
			//para cada fecha verifico si existe o no el registro
			if(p_HR_Attendance_ID==0) {
				/*at = new Query(getCtx(), MHR_Attendance.Table_Name,
						" DateFrom <= '"+fecha+"'"+
						" AND DateTo >= '"+fecha+"'", 
						get_TrxName()).first();	*/
				at = new Query(getCtx(), MHR_Attendance.Table_Name,"HR_Attendance_ID IN (SELECT HR_Attendance_ID FROM HR_AttendanceLine "
						+ "WHERE AttendanceDate = '"+fecha+
						"' GROUP BY HR_Attendance_ID,AttendanceDate)", 
						get_TrxName()).first();
				if(at==null) {
					at = new MHR_Attendance(getCtx(), 0, get_TrxName());
					at.setDateFrom(Timestamp.valueOf(fecha.atStartOfDay()));
					at.setDateTo(Timestamp.valueOf(fecha.atStartOfDay()));
					at.saveEx();
				}
			}
			PreparedStatement pstmt = null;
			ResultSet rs = null;
			//buscar todos los empleados que estaban en planilla para la fecha
			StringBuilder sql = new StringBuilder("SELECT Distinct ON (C_BPartner_ID) bp.C_BPartner_ID "
					+ " FROM C_BPartner bp "
					+ " LEFT JOIN AD_User u ON u.C_BPartner_ID=bp.C_BPartner_ID AND IsInPayroll='Y'"
					+ " JOIN (SELECT C_BPartner_ID FROM HR_Employee WHERE StartDate <= ? AND (EndDate is null OR EndDate >=?) AND isactive='Y' GROUP BY C_BPartner_ID) e ON e.C_BPartner_ID=bp.C_BPartner_ID "
					+ " WHERE bp.IsEmployee='Y' AND bp.IsActive='Y'");		
			try
			{				
				pstmt = DB.prepareStatement(sql.toString(), get_TrxName());
				pstmt.setTimestamp(1, Timestamp.valueOf(fecha.atStartOfDay()));
				pstmt.setTimestamp(2, Timestamp.valueOf(fecha.atStartOfDay()));
				rs = pstmt.executeQuery();
				while (rs.next())
				{				
					int c_bpartner_id = rs.getInt("C_BPartner_ID");			
					MHR_AttendanceLine atline = new Query(getCtx(), "HR_AttendanceLine",
							"C_BPartner_ID="+c_bpartner_id+
							" AND HR_Attendance_ID="+at.getHR_Attendance_ID()+
							" AND AttendanceDate='"+fecha+"'",
							get_TrxName()).first();
					if(atline==null) {
						count++;
						//log.warning("Tercero"+c_bpartner_id+" fecha "+fecha);
						MHR_AttendanceLine newline = new MHR_AttendanceLine(getCtx(),0,get_TrxName());
						newline.setC_BPartner_ID(c_bpartner_id);
						newline.setAttendanceDate(Timestamp.valueOf(fecha.atStartOfDay()));
						newline.setQtyOfHours1(Env.ZERO);
						newline.setQtyOfHours2(Env.ZERO);
						newline.setTotalQtyOfHours(Env.ZERO);
						newline.setHR_Attendance_ID(at.getHR_Attendance_ID());
						newline.setWeekDay(getWeekDayValue(Date.from(fecha.atStartOfDay(defaultZoneId).toInstant())));
						newline.saveEx();
					}
				}
			}
			catch (Exception e)
			{
				log.log(Level.SEVERE, sql.toString(), e);
			}
			finally
			{
				DB.close(rs, pstmt);
			}
		}
		//log.warning("total emp="+count);
		return "Proceso Finalizado. Creados "+count+" Registros.";
	}

	public static List<LocalDate> getDatesBetween(LocalDate startDate, LocalDate endDate) {
	    return startDate.datesUntil(endDate.plus(1,ChronoUnit.DAYS))
	    	      .collect(Collectors.toList());
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
	
}
