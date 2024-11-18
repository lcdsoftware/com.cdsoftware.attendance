package com.cdsoftware.lirion.attendance.process;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.compiere.model.MBPartner;
import org.compiere.model.MRefList;
import org.compiere.model.Query;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.util.DB;
import org.compiere.util.Env;

import com.cdsoftware.lirion.attendance.base.CustomProcess;
import com.cdsoftware.lirion.attendance.model.MGH_ShiftsLine;
import com.cdsoftware.lirion.attendance.model.MHR_Attendance;
import com.cdsoftware.lirion.attendance.model.MHR_AttendanceLine;
import com.cdsoftware.lirion.attendance.model.X_HR_AttendanceDevices;
import com.cdsoftware.lirion.attendance.model.X_I_Attendance;

/**
 * Proceso para importar marcaciones desde la tabla I_Attendance
 * 
 * @author Carlo
 */
@org.adempiere.base.annotation.Process
public class ImportAttendanceFromI_Attendance extends CustomProcess {

	private String pDevice_Name = "";

	@Override
	protected void prepare() {
		ProcessInfoParameter[] parameters = getParameter();
		for (ProcessInfoParameter para : parameters) {
			String name = para.getParameterName();
			if (para.getParameter() == null);
			else if (name.equals("Device_Name"))
				pDevice_Name = para.getParameterAsString();
		}
	}

	@Override
	protected String doIt() throws Exception {
		writeAttendance();
		return "Proceso Terminado";
	}

	public String writeAttendance() throws Exception {
		StringBuilder clientCheck = new StringBuilder(" AND AD_Client_ID=").append(getAD_Client_ID());

		MHR_Attendance attendance = new MHR_Attendance(getCtx(), 0, get_TrxName());

		// Obtener los registros de asistencia según pDevice_Name
		List<X_I_Attendance> attendanceList;
		if (pDevice_Name.isEmpty()) {
			attendanceList = new Query(getCtx(), X_I_Attendance.Table_Name, "Processed!='Y'", get_TrxName())
					.setClient_ID()
					.setOrderBy("Device_Name, HR_ClockCode, Date_Stamp ASC")
					.list();
		} else {
			attendanceList = new Query(getCtx(), X_I_Attendance.Table_Name, "Device_Name=? AND Processed!='Y'", get_TrxName())
					.setClient_ID()
					.setOrderBy("Device_Name, HR_ClockCode, Date_Stamp ASC")
					.setParameters(pDevice_Name)
					.list();
		}

		if (attendanceList.isEmpty()) {
			return "@Error@No hay registros en la tabla I_Attendance";
		} else {
			attendance.saveEx();
		}

		SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy h:mm a", Locale.getDefault());
		SimpleDateFormat dateFormat2 = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault());
		SimpleDateFormat dateFormat3 = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

		String day = "";
		String emp = "";
		int alID = 0;
		int i = 1;
		int j = 0;
		Timestamp time1 = null;
		Timestamp time2 = null;
		Timestamp time3 = null;
		Timestamp time4 = null;
		Timestamp startDate = new Timestamp(System.currentTimeMillis());
		Timestamp endDate = null;
		Timestamp parsedDate = null;
		String notprocessedwhere="Processed='N' ";
		if (!pDevice_Name.isEmpty()) {
			notprocessedwhere=notprocessedwhere+" AND Device_Name="+pDevice_Name;
		}
		int notprocessed=new Query(getCtx(),"I_Attendance",notprocessedwhere,get_TrxName()).count();
		int processed=1;

		MHR_AttendanceLine lastAttendanceLine=null;
		Map<String,String> attendanceDevices = new HashMap<>();

		for (X_I_Attendance record : attendanceList) {


			if(record.getHR_ClockCode()==null)
				continue;

			String hrClockCode = record.getHR_ClockCode().trim();
			String markingDateStr = dateFormat.format(record.getDate_Stamp());
			parsedDate = new Timestamp(record.getDate_Stamp().getTime());

			if (j == 0 || startDate.compareTo(parsedDate) > 0)
				startDate = parsedDate;

			// Obtener el empleado por HR_ClockCode
			int employedID = DB.getSQLValueEx(get_TrxName(), 
					"SELECT C_BPartner_ID FROM C_BPartner WHERE REPLACE(trim(COALESCE(HR_ClockCode,taxid,value)), '-', '')= REPLACE(trim(?), '-', '')", 
					hrClockCode);

			if (employedID <= 0) {
				log.severe("No se encuentra el empleado por HR_ClockCode ni por Tax_ID: " + hrClockCode);
				log.warning("@Error@ No se encuentra el empleado " + hrClockCode);
				continue; // Saltar este registro, evita marcar como procesado
			}

			// Verificar que Device_Name y Device_SN existan en HR_AttendanceDevices
			String deviceName = record.getDevice_Name();
			String deviceSN = record.getDevice_SN();

			//int deviceID = DB.getSQLValueEx(get_TrxName(),
			//		"SELECT HR_AttendanceDevices_ID FROM HR_AttendanceDevices WHERE Name=? AND Device_SN=?",
			//		deviceName, deviceSN);
			
			//X_HR_AttendanceDevices device = new Query(getCtx(), X_HR_AttendanceDevices.Table_Name, "Name=? AND Device_SN=?", get_TrxName()).setParameters(deviceName, deviceSN).first();
			
			if (!attendanceDevices.containsKey(deviceName)) {
				// Si el dispositivo no existe, crear uno nuevo
				log.warning("Dispositivo no encontrado. Insertando nuevo dispositivo: " + deviceName + " con SN: " + deviceSN);

				X_HR_AttendanceDevices newDevice = new X_HR_AttendanceDevices(getCtx(), 0, get_TrxName());
				newDevice.setName(deviceName);
				newDevice.setDevice_SN(deviceSN);
				newDevice.saveEx(); // Guardar el nuevo dispositivo
				attendanceDevices.put(deviceName, deviceSN);
				//deviceID = newDevice.getHR_AttendanceDevices_ID(); // Obtener el nuevo ID del dispositivo
				log.info("Nuevo dispositivo creado con ID: " + newDevice.get_ID());
				
			}

			MBPartner employed = new MBPartner(getCtx(), employedID, get_TrxName());

			

			if (!day.equals(dateFormat2.format(parsedDate)) || !emp.equals(hrClockCode)) {
				MHR_AttendanceLine existingattendance=new Query(getCtx(), MHR_AttendanceLine.Table_Name, "AttendanceDate=? AND C_BPartner_ID=?", get_TrxName())
						.setParameters(dateFormat3.format(parsedDate),employed.getC_BPartner_ID())
						.first();
				if(existingattendance==null) {
					i = 1;
					
					checkLastAttendanceTime(lastAttendanceLine);	
					MHR_AttendanceLine al = new MHR_AttendanceLine(getCtx(), 0, get_TrxName());
					al.setHR_Attendance_ID(attendance.get_ID());
					al.setC_BPartner_ID(employed.getC_BPartner_ID());
					al.setWeekDay(getWeekDayValue(parsedDate));
					al.setAttendanceDate(parsedDate);

					time1 = parsedDate;
					al.setTime1(time1);

					al.saveEx();
					this.statusUpdate("Procesando registro: "+String.valueOf(processed++)+"/"+String.valueOf(notprocessed) +" "+ employed.getValue() + " " + employed.getName() + " " + al.getAttendanceDate());
					day = dateFormat2.format(parsedDate);
					emp = hrClockCode;
					alID = al.get_ID();
				}

			} else {
				MHR_AttendanceLine al = new MHR_AttendanceLine(getCtx(), alID, get_TrxName());
				if (record.getDate_Stamp() != null) {
					parsedDate = new Timestamp(record.getDate_Stamp().getTime());

					if (i == 2) {
						time2 = parsedDate;
						al.setTime2(time2);
						BigDecimal QtyOfHours1 = BigDecimal.ZERO;
						if (time1 != null && time2 != null) {
							QtyOfHours1 = BigDecimal.valueOf((time2.getTime() - time1.getTime()) / (1000 * 60)).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_EVEN);
						}
						al.setQtyOfHours1(QtyOfHours1);
						al.setTotalQtyOfHours(al.getTotalQtyOfHours().add(QtyOfHours1));
					}

					if (i == 3) {
						time3 = parsedDate;
						al.setTime3(time3);
					}

					if (i >= 4) {
						time4 = parsedDate;
						al.setTime4(time4);
						BigDecimal QtyOfHours2 = BigDecimal.ZERO;
						if (time3 != null && time4 != null) {
							QtyOfHours2 = BigDecimal.valueOf((time4.getTime() - time3.getTime()) / (1000 * 60)).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_EVEN);
						}
						al.setQtyOfHours2(QtyOfHours2);
						al.setTotalQtyOfHours(al.getTotalQtyOfHours().add(QtyOfHours2));
					}
				}
				al.saveEx();
				lastAttendanceLine = al;

			}

			// Marcar como procesado solo si el empleado fue encontrado
			record.set_ValueNoCheck("Processed", true);
			record.saveEx();

			i++;
			j++;
		}





		endDate = parsedDate;
		attendance.setDateFrom(startDate);
		attendance.setDateTo(endDate);
		attendance.setName(startDate.toString().concat(" to ").concat(endDate.toString()));
		attendance.saveEx();
		return null;
	}

	protected BigDecimal getDifference(MHR_AttendanceLine attendance, int Shift_ID) {
		String WeekDay = getWeekDayValue(attendance.getWeekDay());
		if (WeekDay == null)
			return BigDecimal.ZERO;
		MGH_ShiftsLine shiftline = new Query(getCtx(), MGH_ShiftsLine.Table_Name, "WeekDay=? and " + MGH_ShiftsLine.COLUMNNAME_GH_Shifts_ID + "=?", get_TrxName()).setParameters(WeekDay, Shift_ID).first();
		BigDecimal diference = shiftline.getQtyOfHours1().subtract(attendance.getQtyOfHours1());
		diference = diference.subtract(shiftline.getTolerance());
		return (diference.compareTo(BigDecimal.ZERO) > 0) ? diference : BigDecimal.ZERO;
	}

	protected String getWeekDayValue(java.util.Date WeekDayStr) {
		Timestamp time = new Timestamp(WeekDayStr.getTime());
		LocalDateTime attendancedateaux = time.toLocalDateTime();

		List<MRefList> reflist = new Query(getCtx(), MRefList.Table_Name, "AD_Reference_ID=?", get_TrxName()).setParameters(167).list();

		for (MRefList ref : reflist) {
			if (ref.getName().trim().compareToIgnoreCase(attendancedateaux.getDayOfWeek().toString()) == 0)
				return ref.getValue();
		}
		return null;
	}

	protected String getWeekDayValue(String WeekDayStr) {
		List<MRefList> reflist = new Query(getCtx(), MRefList.Table_Name, "AD_Reference_ID=?", get_TrxName()).setParameters(167).list();

		for (MRefList ref : reflist) {
			if (ref.getName().trim().compareToIgnoreCase(WeekDayStr) == 0)
				return ref.getValue();
		}
		return null;
	}
	protected void checkLastAttendanceTime(MHR_AttendanceLine al) {
		
		//Check if the last hour has data
		if(al!=null && al.getTime4()==null && al.getTime3()!=null) {
			al.setTime4(al.getTime3());
			al.setTime3(null);						
			al.saveEx();
		}
		else if(al!=null && al.getTime4()==null && al.getTime3()==null && al.getTime2()!=null) {
			al.setTime4(al.getTime2());
			al.setTime2(null);
			al.setQtyOfHours1(Env.ZERO);
			al.saveEx();
		}			
	}
}
