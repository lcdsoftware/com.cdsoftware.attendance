package com.cdsoftware.lirion.attendance.process;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Level;

import org.compiere.model.MBPartner;
import org.compiere.model.MPInstance;
import org.compiere.model.MProcess;
import org.compiere.model.MRefList;
import org.compiere.model.Query;
import org.compiere.model.X_C_NonBusinessDay;
import org.compiere.process.ProcessInfo;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.DB;

import com.cdsoftware.lirion.attendance.model.MGH_Shifts;
import com.cdsoftware.lirion.attendance.model.MGH_ShiftsLine;
import com.cdsoftware.lirion.attendance.model.MHR_Attendance;
import com.cdsoftware.lirion.attendance.model.MHR_AttendanceLine;
import com.cdsoftware.lirion.payroll.model.MHRAttribute;
import com.cdsoftware.lirion.payroll.model.MHRConcept;

@org.adempiere.base.annotation.Process
public class ProcessAttendance extends SvrProcess{


	private int RECORD_ID;
	private int p_C_BPartner_ID=0;
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
			else if (name.equals("C_BPartner_ID"))
				p_C_BPartner_ID = para.getParameterAsInt();
		}
		RECORD_ID = getRecord_ID();

	}

	@Override
	protected String doIt() throws Exception {
		
		StringBuilder clientCheck = new StringBuilder(" AND AD_Client_ID=").append(getAD_Client_ID());
		StringBuilder sql = new StringBuilder ("DELETE FROM HR_Attribute ")
				.append("WHERE HR_Attendance_ID=").append(RECORD_ID).append (clientCheck);
		if(p_C_BPartner_ID>0) 
			sql.append(" AND C_BPartner_ID=").append(p_C_BPartner_ID);
		
		int	no = DB.executeUpdate(sql.toString(), get_TrxName());
		if (log.isLoggable(Level.FINE)) log.fine("Delete Attendance =" + no);
		attendance = new MHR_Attendance(getCtx(), RECORD_ID, get_TrxName());
		
		//Rango de Fecha al procesar es obligatorio en cabecera del documento
		if(attendance.getDateTo()==null || attendance.getDateFrom()==null)
			return "@Error@No se ha colocado un rango de fecha a procesar";
		
		String wherealines="HR_Attendance_ID=? AND TotalQtyOfHours is not null AND AttendanceDate between ? and ?";
		if(p_C_BPartner_ID>0) 
			wherealines = wherealines+" AND C_BPartner_ID="+p_C_BPartner_ID;
		
		List<MHR_AttendanceLine> list_attendanceline = 
				new Query(getCtx(),MHR_AttendanceLine.Table_Name,wherealines,get_TrxName())
				.setParameters(attendance.get_ID(),attendance.getDateFrom(),attendance.getDateTo()).setOrderBy("C_BPartner_ID")
				.setOrderBy(MHR_AttendanceLine.COLUMNNAME_C_BPartner_ID+","+MHR_AttendanceLine.COLUMNNAME_AttendanceDate)
				.list();
		if(list_attendanceline.size()==0) 
			return "@Error@No hay marcaciones en la fecha especificada";
		
		int C_BPartner_ID = list_attendanceline.get(0).getC_BPartner_ID();
		BigDecimal lateHour = BigDecimal.ZERO;
		long absenceDays = 0;
		//obtenemos cantidad de dias a evaluar
		LocalDateTime from = attendance.getDateFrom().toLocalDateTime();
		LocalDateTime to = attendance.getDateTo().toLocalDateTime();
		Duration duration = Duration.between(from,to.plusDays(1));

		//quitamos los dias nacionales del periodo
		long nonBusinessDays = new Query(getCtx(),X_C_NonBusinessDay.Table_Name, X_C_NonBusinessDay.COLUMNNAME_Date1 + " between '"+from.toLocalDate().toString()+"' and '"+to.toLocalDate().toString()+"'", get_TrxName())
				.count();
		log.warning("Cantidad Feriados:"+nonBusinessDays);
		
		long i=0;
		long diff=0;
		long restDays=0;
		MHR_AttendanceLine lastattendanceline=null;
		diff = Math.abs(duration.toDays());
		for(MHR_AttendanceLine attendanceline : list_attendanceline) {
			MBPartner bpartner = new MBPartner(getCtx(), attendanceline.getC_BPartner_ID(), get_TrxName());
			this.statusUpdate("Actualizando: "+bpartner.getName());
			int p_GH_Shifts_ID = bpartner.get_ValueAsInt("GH_Shifts_ID");
			if(p_GH_Shifts_ID == 0) {
				p_GH_Shifts_ID = 1000001; //crear el check de predeterminado en el turno para que no este fijo
			}
			
			//buscamos los dias de descanso del turno
			//quitamos los dias de descanso en el periodo
			restDays=0;
			LocalDateTime fromRD =from;
			log.severe("Antes del ciclo diferencia "+String.valueOf(diff)+" restDays "+String.valueOf(restDays)+" Nonbusinessdays "+String.valueOf(nonBusinessDays)+" i "+String.valueOf(i));

			for(long j =1; j<=diff; j++) {
				if(isRestDay(fromRD.getDayOfWeek(),new MGH_Shifts(getCtx(),p_GH_Shifts_ID,get_TrxName())))
					restDays++;

				fromRD=fromRD.plusDays(1);
			}
			if(attendanceline.getC_BPartner_ID()!=C_BPartner_ID) {
				insertLateHour(C_BPartner_ID,lateHour,attendance);
				absenceDays = diff - i - restDays - nonBusinessDays;
				insertAbscenceDays(C_BPartner_ID,absenceDays,attendance);
				C_BPartner_ID = attendanceline.getC_BPartner_ID();
				lateHour=BigDecimal.ZERO;
				absenceDays=0;
				i=0;
				diff = Math.abs(duration.toDays());
			}

			log.warning("C_Bpartner"+C_BPartner_ID);
			
			if(!isRestDay(attendanceline.getAttendanceDate().toLocalDateTime().getDayOfWeek(),new MGH_Shifts(getCtx(),p_GH_Shifts_ID,get_TrxName()))) {
				BigDecimal diference=getDifference(attendanceline,new MGH_Shifts(getCtx(),p_GH_Shifts_ID,get_TrxName()));
				
			
				if(diference.compareTo(BigDecimal.valueOf(6))>=0)
					diff++;
				else
					lateHour= lateHour.add(diference);
				
			

			}
			BigDecimal aux= getDifferenceHE(attendanceline,new MGH_Shifts(getCtx(),p_GH_Shifts_ID,get_TrxName()));
			i++;
			lastattendanceline=attendanceline;
		}

		absenceDays = diff - i - restDays - nonBusinessDays;
		insertAbscenceDays(C_BPartner_ID,absenceDays,attendance);
		MBPartner bpartner = new MBPartner(getCtx(), lastattendanceline.getC_BPartner_ID(), get_TrxName());
		int p_GH_Shifts_ID = bpartner.get_ValueAsInt("GH_Shifts_ID");
		if(p_GH_Shifts_ID == 0) {
			p_GH_Shifts_ID =  1000001; //crear el check de predeterminado en el turno para que no este fijo

		}
		
		if(!isRestDay(lastattendanceline.getAttendanceDate().toLocalDateTime().getDayOfWeek(),new MGH_Shifts(getCtx(),p_GH_Shifts_ID,get_TrxName())))
			lateHour= lateHour.add(getDifference(lastattendanceline,new MGH_Shifts(getCtx(),p_GH_Shifts_ID,get_TrxName())));

		insertLateHour(C_BPartner_ID,lateHour,attendance);
		return null;
	}

	private boolean isRestDay(DayOfWeek dayOfWeek,MGH_Shifts shift) {
		// TODO Auto-generated method stub
		List<MGH_ShiftsLine> sl = new Query(getCtx(), MGH_ShiftsLine.Table_Name, "RestDay='Y'", get_TrxName()).list();

		for(MGH_ShiftsLine sline : sl){
			String wd =sline.getWeekDay();
			if(dayOfWeek.getValue()==Integer.valueOf(sline.getWeekDay()))
				return true;
		}
		return false;
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
			/*duration = Duration.between(attendanceTime2,shiftTime2 );


			diffTime2 = duration.toMinutes();
			diffTime2 = diffTime2 < 0.0 ?0.0:diffTime2;
			if(attendanceTime3!=null) {
				duration = Duration.between(shiftTime3,attendanceTime3);
				diffTime3 = duration.toMinutes();
				diffTime3 = diffTime3 < 0.0 ?0.0:diffTime3;
			}else if(attendanceTime2.compareTo(attendanceTime3)>0){
				attendanceTimeAux=attendanceTime2;
			}
			if(attendanceTime4!=null) {
				duration = Duration.between(attendanceTime4,shiftTime4);
				diffTime4 = duration.toMinutes();
				diffTime4 = diffTime4 < 0.0 ?0.0:diffTime4;
			}
			if(attendanceTime3==null && attendanceTime4==null && attendanceTimeAux.compareTo(shiftTime4)<0) {
				duration= Duration.between(attendanceTimeAux,shiftTime4WithoutTolerance);
				diffTime4 =duration.toMinutes();
			}
		
		}else if("LD".compareTo(attendanceline.getAuthorizationType())!=0) {
			LocalDateTime attendanceTimeAux=shiftTime3WithoutTolerance;
			duration = Duration.between(attendanceTime2,shiftTime2 );


			diffTime2 = duration.toMinutes();
			diffTime2 = diffTime2 < 0.0 ?0.0:diffTime2;
			if(attendanceTime3!=null) {
				duration = Duration.between(shiftTime3,attendanceTime3);
				diffTime3 = duration.toMinutes();
				diffTime3 = diffTime3 < 0.0 ?0.0:diffTime3;
			}else if(attendanceTime2.compareTo(attendanceTime3)>0){
				attendanceTimeAux=attendanceTime2;
			}
			if(attendanceTime4!=null) {
				duration = Duration.between(attendanceTime4,shiftTime4);
				diffTime4 = duration.toMinutes();
				diffTime4 = diffTime4 < 0.0 ?0.0:diffTime4;
			}
			if(attendanceTime3==null && attendanceTime4==null && attendanceTimeAux.compareTo(shiftTime4)<0) {
				duration= Duration.between(attendanceTimeAux,shiftTime4WithoutTolerance);
				diffTime4 =duration.toMinutes();
			}
		
		}else {
			if(attendanceTime2.compareTo(shiftTime3)>0 && attendanceTime3 == null && attendanceTime1.compareTo(shiftTime2)<0) {
				attendanceTime4=attendanceTime2;
				attendanceTime2=shiftTime2;
				attendanceTime3=shiftTime3;

			}*/
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
				/*if(diffTime4 <= shiftline.getTolerance().doubleValue())
					duration = Duration.between(attendanceTime4,shiftTime4);*/
				
				
				diffTime4 = diffTime4 < 0.0 ? 0.0:diffTime4;
			}
			
			if(attendanceTime3==null && attendanceTime4==null && attendanceTimeAux.compareTo(shiftTime4)<0) {
				duration= Duration.between(attendanceTimeAux,shiftTime4WithoutTolerance);
				diffTime4 =duration.toMinutes();
			}
			
		//}


		attendanceline.setQtyMinutesDifference1(BigDecimal.valueOf(diffTime1+diffTime2));
		attendanceline.setQtyMinutesDifference2(BigDecimal.valueOf(diffTime3+diffTime4));
		attendanceline.saveEx();


		BigDecimal diference = BigDecimal.valueOf((diffTime1+diffTime2+diffTime3+diffTime4)/60.0).setScale(2, RoundingMode.HALF_UP);
		log.severe("total"+String.valueOf(diffTime1+diffTime2+diffTime3+diffTime4));



		return (diference.compareTo(BigDecimal.ZERO)>0)? diference: BigDecimal.ZERO;
	}
	protected BigDecimal getDifferenceHE(MHR_AttendanceLine attendanceline, MGH_Shifts pShift) {
		// TODO Auto-generated method stub
		String WeekDay = attendanceline.getWeekDay();

		if (WeekDay.equals(null))
			return BigDecimal.ZERO;

		MGH_ShiftsLine shiftline = new Query(getCtx(), MGH_ShiftsLine.Table_Name, "GH_Shifts_ID=? AND WeekDay=?", get_TrxName()).setParameters(pShift.get_ID(),WeekDay).first();

		LocalDateTime shiftTime1 = prepareTime(attendanceline.getAttendanceDate(),shiftline.getTime1());
		LocalDateTime shiftTime2 = prepareTime(attendanceline.getAttendanceDate(),shiftline.getTime2());
		LocalDateTime shiftTime3 = prepareTime(attendanceline.getAttendanceDate(),shiftline.getTime3());
		LocalDateTime shiftTime4 = prepareTime(attendanceline.getAttendanceDate(),shiftline.getTime4());

		LocalDateTime attendanceTime1 = prepareTime(attendanceline.getAttendanceDate(),attendanceline.getTime1());
		attendanceTime1 = (attendanceTime1==null? shiftTime1 : attendanceTime1);
		LocalDateTime attendanceTime2 = prepareTime(attendanceline.getAttendanceDate(),attendanceline.getTime2());
		//attendanceTime2 = (attendanceTime2==null? shiftTime2 : attendanceTime2);
		if(attendanceTime2==null)
			return BigDecimal.ZERO;

		LocalDateTime attendanceTime3 = prepareTime(attendanceline.getAttendanceDate(),attendanceline.getTime3());
		LocalDateTime attendanceTime4 = prepareTime(attendanceline.getAttendanceDate(),attendanceline.getTime4());
		if(shiftTime3!=null && shiftTime4!=null ) {

			if( attendanceTime2.compareTo(shiftTime3)>0 && attendanceTime3 == null && attendanceTime1.compareTo(shiftTime2)<0) {
				attendanceTime4=attendanceTime2;
				attendanceTime2=shiftTime3;
				attendanceTime3=shiftTime3;

			}else {
				if(attendanceTime3 == null || (attendanceTime3.compareTo(shiftTime3)>0 && attendanceTime4 == null))
					return BigDecimal.ZERO;

				attendanceTime3 = (attendanceTime3 == null? shiftTime3 : attendanceTime3);

				attendanceTime4 = (attendanceTime4 == null? shiftTime4 : attendanceTime4);
			}
		}
		if(attendanceline.getAuthorizationType()==null || "LA".compareTo(attendanceline.getAuthorizationType())!=0)
			attendanceTime2=shiftTime2;


		if(shiftTime1==null || shiftTime2==null ||shiftTime3==null ||shiftTime4==null) return BigDecimal.ZERO;

		//check rest out and time2
		Duration rest = Duration.between(shiftTime2,shiftTime3);
		Duration aux = Duration.between(shiftTime2,attendanceTime2);
		/*if(aux.toMinutes()>=rest.toMinutes()) {
			attendanceTime4 = attendanceTime2;
			attendanceTime2 = shiftTime3;
			attendanceTime3 = shiftTime3;

		}*/

		Duration duration = Duration.between(shiftTime1, attendanceTime1);
		double diffTime1 = duration.toMinutes();

		duration = Duration.between(attendanceTime2,shiftTime2 );
		double diffTime2 = duration.toMinutes();

		duration = Duration.between(shiftTime3,attendanceTime3);
		double diffTime3 = duration.toMinutes();

		duration = Duration.between(attendanceTime4,shiftTime4);
		double diffTime4 = duration.toMinutes();






		BigDecimal diference = BigDecimal.valueOf((diffTime1+diffTime2+diffTime3+diffTime4)/60.0).setScale(2, RoundingMode.UP);


		if(diffTime2< (-29.0) || isRestDay(attendanceTime1.getDayOfWeek(),pShift)) {
			//process extra hours
			ProcessInfoParameter p1 = new ProcessInfoParameter("HR_Process_ID", attendance.get_Value("HR_Process_ID"),"", "", "");
			ProcessInfoParameter p2 = new ProcessInfoParameter("C_BPartner_ID", attendanceline.getC_BPartner_ID(),"", "", "");
			ProcessInfoParameter p3 = new ProcessInfoParameter("AttendanceDate", attendanceline.getAttendanceDate(),"", "", "");
			ProcessInfoParameter p4 = new ProcessInfoParameter("Time1", Timestamp.valueOf(shiftTime2),"", "", "");
			if(isRestDay(attendanceTime1.getDayOfWeek(),pShift))
				p4 = new ProcessInfoParameter("Time1", Timestamp.valueOf(attendanceTime1),"", "", "");
			ProcessInfoParameter p5 = new ProcessInfoParameter("AttendanceDate2", attendanceline.getAttendanceDate(),"", "", "");
			ProcessInfoParameter p6 = new ProcessInfoParameter("Time2", Timestamp.valueOf(attendanceTime2),"", "", "");
			ProcessInfoParameter p7 = new ProcessInfoParameter("HR_Attendance_ID", attendance.get_ID(),"", "", "");


			ProcessInfo pi = new ProcessInfo("HR_Calulate_Extra_Hour", 0, 0, 0);
			pi.setAD_Client_ID(getAD_Client_ID());
			pi.setParameter(new ProcessInfoParameter[]{p1,p2,p3,p4,p5,p6,p7});

			MProcess process = new Query(getCtx(),MProcess.Table_Name,  "Value = ?", get_TrxName()).setParameters("HR_Calulate_Extra_Hour").first();

			MPInstance instance = new MPInstance(getCtx(),0,null);
			instance.setAD_Process_ID(process.get_ID() );
			instance.setRecord_ID(0);
			instance.saveEx();

			pi.setAD_PInstance_ID(instance.getAD_PInstance_ID());



			log.info("Starting process  HR_Calulate_Extra_Hour");

			CalculateExtraHour ceh = new CalculateExtraHour();
			boolean result = ceh.startProcess(getCtx(), pi, null);

		}
		if(diffTime3<(-29.0) && (attendanceline.getAuthorizationType()==null || "LA".compareTo(attendanceline.getAuthorizationType())!=0 )) {
			//process extra hours
			ProcessInfoParameter p1 = new ProcessInfoParameter("HR_Process_ID", attendance.get_Value("HR_Process_ID"),"", "", "");
			ProcessInfoParameter p2 = new ProcessInfoParameter("C_BPartner_ID", attendanceline.getC_BPartner_ID(),"", "", "");
			ProcessInfoParameter p3 = new ProcessInfoParameter("AttendanceDate", attendanceline.getAttendanceDate(),"", "", "");
			ProcessInfoParameter p4 = new ProcessInfoParameter("Time1", Timestamp.valueOf(attendanceTime3),"", "", "");

			ProcessInfoParameter p5 = new ProcessInfoParameter("AttendanceDate2", attendanceline.getAttendanceDate(),"", "", "");
			ProcessInfoParameter p6 = new ProcessInfoParameter("Time2", Timestamp.valueOf(shiftTime3),"", "", "");
			ProcessInfoParameter p7 = new ProcessInfoParameter("HR_Attendance_ID", attendance.get_ID(),"", "", "");
			ProcessInfo pi = new ProcessInfo("HR_Calulate_Extra_Hour", 0, 0, 0);
			pi.setAD_Client_ID(getAD_Client_ID());
			pi.setParameter(new ProcessInfoParameter[]{p1,p2,p3,p4,p5,p6,p7});

			MProcess process = new Query(getCtx(),MProcess.Table_Name,  "Value = ?", get_TrxName()).setParameters("HR_Calulate_Extra_Hour").first();

			MPInstance instance = new MPInstance(getCtx(),0,null);
			instance.setAD_Process_ID(process.get_ID() );
			instance.setRecord_ID(0);
			instance.saveEx();

			pi.setAD_PInstance_ID(instance.getAD_PInstance_ID());



			log.info("Starting process  HR_Calulate_Extra_Hour");

			CalculateExtraHour ceh = new CalculateExtraHour();
			boolean result = ceh.startProcess(getCtx(), pi, null);

		}
		if(diffTime4<(-29.0) || isRestDay(attendanceTime3.getDayOfWeek(),pShift)) {
			//process extra hours
			ProcessInfoParameter p1 = new ProcessInfoParameter("HR_Process_ID", attendance.get_Value("HR_Process_ID"),"", "", "");
			ProcessInfoParameter p2 = new ProcessInfoParameter("C_BPartner_ID", attendanceline.getC_BPartner_ID(),"", "", "");
			ProcessInfoParameter p3 = new ProcessInfoParameter("AttendanceDate", attendanceline.getAttendanceDate(),"", "", "");
			ProcessInfoParameter p4 = new ProcessInfoParameter("Time1", Timestamp.valueOf(shiftTime4),"", "", "");
			if(isRestDay(attendanceTime3.getDayOfWeek(),pShift))
				p4 = new ProcessInfoParameter("Time1", Timestamp.valueOf(attendanceTime3),"", "", "");
			ProcessInfoParameter p5 = new ProcessInfoParameter("AttendanceDate2", attendanceline.getAttendanceDate(),"", "", "");
			ProcessInfoParameter p6 = new ProcessInfoParameter("Time2", Timestamp.valueOf(attendanceTime4),"", "", "");
			ProcessInfoParameter p7 = new ProcessInfoParameter("HR_Attendance_ID", attendance.get_ID(),"", "", "");
			ProcessInfo pi = new ProcessInfo("HR_Calulate_Extra_Hour", 0, 0, 0);
			pi.setAD_Client_ID(getAD_Client_ID());
			pi.setParameter(new ProcessInfoParameter[]{p1,p2,p3,p4,p5,p6,p7});

			MProcess process = new Query(getCtx(),MProcess.Table_Name,  "Value = ?", get_TrxName()).setParameters("HR_Calulate_Extra_Hour").first();

			MPInstance instance = new MPInstance(getCtx(),0,null);
			instance.setAD_Process_ID(process.get_ID() );
			instance.setRecord_ID(0);
			instance.saveEx();

			pi.setAD_PInstance_ID(instance.getAD_PInstance_ID());



			log.info("Starting process  HR_Calulate_Extra_Hour");

			CalculateExtraHour ceh = new CalculateExtraHour();
			boolean result = ceh.startProcess(getCtx(), pi, null);

		}

		return (diference.compareTo(BigDecimal.ZERO)>0)? diference: BigDecimal.ZERO;
	}

	private LocalDateTime prepareTime(Timestamp attendanceDate, Timestamp time1) {
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

	protected void insertLateHour(int C_BPartner_ID, BigDecimal lateHour,MHR_Attendance attendance) {
		// TODO Auto-generated method stub
		MBPartner employed = new MBPartner(getCtx(),C_BPartner_ID , get_TrxName());
		if(!employed.isActive() || lateHour.equals(BigDecimal.ZERO) || lateHour.compareTo(BigDecimal.valueOf(0.3))<0)
			return;


		MHRAttribute attribute = new MHRAttribute(getCtx(), 0, get_TrxName());
		attribute.setValidFrom(attendance.getDateTo());
		attribute.setValidTo(attendance.getDateTo());
		attribute.setColumnType(MHRAttribute.COLUMNTYPE_Amount);
		attribute.setAmount(lateHour);
		attribute.setHR_Concept_ID(MHRConcept.getByValue(getCtx(), "PA_D_HORAS_AUSENCIA_CANTIDAD",get_TrxName()).getHR_Concept_ID());
		attribute.setC_BPartner_ID(employed.getC_BPartner_ID());
		attribute.setAD_Org_ID(attendance.getAD_Org_ID()); 
		attribute.set_ValueOfColumn("HR_Attendance_ID", attendance.get_ID());
		attribute.saveEx();
	}
	private void insertAbscenceDays(int C_BPartner_ID, long absenceDays, MHR_Attendance attendance) {
		// TODO Auto-generated method stub
		MBPartner employed = new MBPartner(getCtx(),C_BPartner_ID , get_TrxName());
		if(!employed.isActive() || absenceDays<=0)
			return;


		MHRAttribute attribute = new MHRAttribute(getCtx(), 0, get_TrxName());
		attribute.setValidFrom(attendance.getDateTo());
		attribute.setValidTo(attendance.getDateTo());
		attribute.setColumnType(MHRAttribute.COLUMNTYPE_Amount);
		attribute.setAmount(BigDecimal.valueOf(absenceDays));
		attribute.setHR_Concept_ID(MHRConcept.getByValue(getCtx(), "PA_D_DIAS_AUSENCIA",get_TrxName()).getHR_Concept_ID());
		attribute.setC_BPartner_ID(employed.getC_BPartner_ID());
		attribute.setAD_Org_ID(attendance.getAD_Org_ID()); 
		attribute.set_ValueOfColumn("HR_Attendance_ID", attendance.get_ID());
		attribute.saveEx();
	}
	protected String getWeekDayValue(String WeekDayStr) {

		List<MRefList> reflist = new Query(getCtx(), MRefList.Table_Name, "AD_Reference_ID=?",get_TrxName()).setParameters(167).list();

		for(MRefList ref : reflist) {
			if(ref.getName().compareToIgnoreCase(WeekDayStr)==0) 
				return ref.getValue();

		}
		return null;

	}

}
