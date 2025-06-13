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
import com.cdsoftware.lirion.attendance.model.MGH_ShiftsLine;
import com.cdsoftware.lirion.attendance.model.MHR_Attendance;
import com.cdsoftware.lirion.attendance.model.MHR_AttendanceLine;
import com.cdsoftware.lirion.attendance.model.MHR_C_BPartnerShifts;
/**
 * Proceso: AddMissingDates
 * ---------------------------------------
 * Descripción:
 * Este proceso genera automáticamente líneas de asistencia ({@code HR_AttendanceLine}) 
 * para fechas faltantes dentro de un rango dado, utilizando una cabecera de asistencia 
 * existente ({@code HR_Attendance}) o creando una nueva si no existe y se trabaja por rango.
 *
 * Para cada día en el rango indicado, el proceso:
 * - Obtiene la lista de empleados activos en nómina para esa fecha.
 * - Verifica si tienen turnos asignados y que no sea día de descanso según el turno.
 * - Crea una línea de asistencia si no existe para ese empleado en esa fecha.
 *
 * Parámetros de entrada:
 * - {@code HR_Attendance_ID} (opcional): ID de cabecera de asistencia. Si se proporciona, 
 *   se usa como base para el rango de fechas y la inserción de líneas.
 * - {@code DateFrom}, {@code DateTo} (opcional): Rango de fechas. Obligatorio si no se proporciona la cabecera.
 *
 * Comportamiento:
 * - Si se proporciona un {@code HR_Attendance_ID}:
 *   - Se valida que tenga fechas definidas ({@code DateFrom}, {@code DateTo}).
 *   - Se usa ese rango para procesar.
 * - Si no se proporciona la cabecera pero sí un rango de fechas:
 *   - Para cada día, se busca una cabecera de asistencia existente.
 *   - Si no existe una cabecera para esa fecha, se crea automáticamente.
 * - Para cada fecha:
 *   - Se obtienen empleados activos y en nómina.
 *   - Se valida si tienen un turno asignado para ese día (no es día de descanso).
 *   - Se verifica si ya existe una línea de asistencia.
 *   - Si no existe, se crea con 0 horas trabajadas y se asigna el día de la semana.
 *
 * Notas técnicas:
 * - Usa consultas SQL directas con {@code DISTINCT ON} para optimizar la selección de empleados.
 * - El día de la semana se determina a partir de la referencia {@code AD_Reference_ID=167} (valores estándar de días).
 * - El sistema usa la zona horaria local definida en {@code ZoneId.systemDefault()}.
 *
 * Validaciones:
 * - Si no se especifica ni {@code HR_Attendance_ID} ni un rango de fechas, el proceso lanza error.
 * - Si {@code HR_Attendance_ID} se proporciona pero la cabecera no tiene fechas válidas, lanza error.
 *
 * Resultado:
 * - Devuelve un mensaje indicando cuántas líneas de asistencia fueron creadas en total.
 *
 * Autor: Ángel Lara  
 * Proyecto: Módulo de Asistencia
 */

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
		List<LocalDate> p_dateList=null;
		ZoneId defaultZoneId = ZoneId.systemDefault();
		int count=0;
		if(p_HR_Attendance_ID==0 && p_DateFrom == null && p_DateTo == null)
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
			this.statusUpdate("Analizando Dia "+fecha);
			Timestamp attendanceDate = Timestamp.valueOf(fecha.atStartOfDay());
			String weekDay = getWeekDayValue(Date.from(fecha.atStartOfDay(defaultZoneId).toInstant()));
			//para cada fecha verifico si existe o no el registro
			if(p_HR_Attendance_ID==0) {
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
						newline.setAttendanceDate(attendanceDate);
						newline.setQtyOfHours1(Env.ZERO);
						newline.setQtyOfHours2(Env.ZERO);
						newline.setTotalQtyOfHours(Env.ZERO);
						newline.setHR_Attendance_ID(at.getHR_Attendance_ID());
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
