package com.cdsoftware.lirion.attendance.process;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

import org.compiere.model.MAttachment;
import org.compiere.model.MAttachmentEntry;
import org.compiere.model.MBPartner;
import org.compiere.model.MRefList;
import org.compiere.model.Query;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;
import org.compiere.util.DB;

import com.cdsoftware.lirion.attendance.model.MGH_ShiftsLine;
import com.cdsoftware.lirion.attendance.model.MHR_Attendance;
import com.cdsoftware.lirion.attendance.model.MHR_AttendanceLine;
import com.cdsoftware.lirion.attendance.model.MMarking;

@org.adempiere.base.annotation.Process
public class ImportAttendanceFromAttachmentzk extends SvrProcess{


	private int RECORD_ID;
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
		writeAttendance();
		return null;
	}
	public String readAttendance() throws Exception {
		StringBuilder clientCheck = new StringBuilder(" AND AD_Client_ID=").append(getAD_Client_ID());
		//StringBuilder sql = new StringBuilder ("DELETE I_AttendanceLine ")
		//		.append("WHERE HR_Attendance_ID=").append(RECORD_ID).append (clientCheck);
		//int	no = DB.executeUpdate(sql.toString(), get_TrxName());
		//if (log.isLoggable(Level.FINE)) log.fine("Delete Attendance =" + no);		
		//List<MMarking> listattendance = new Query(getCtx(),MMarking.Table_Name,"MarkingDate between ? and ? AND QtyOfHours1 is not null ",get_TrxName()).setParameters(MarkingDateFrom,MarkingDateTo).setOrderBy("Value").list();
		MHR_Attendance attendance = new MHR_Attendance(getCtx(), RECORD_ID, get_TrxName());

		MAttachment attachment = attendance.getAttachment();
		if (attachment == null) {
			return "@Error@Please attach the attendance file before running the process";
		}

		MAttachmentEntry entry = attachment.getEntry(0);
		if (entry == null) {
			return "@Error@Please attach the attendance file before running the process";
		}
		File csvFile = entry.getFile();

		String line = "";
		String cvsSplitBy = String.valueOf((char)9);

		try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
			String day = "";
			String emp = "";
			int alID = 0;
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
			//SimpleDateFormat dateFormat2 = new SimpleDateFormat("dd/MM/yyyy");
			int i = 1;
			while ((line = br.readLine()) != null) {

				// use comma as separator
				String[] csvLine = line.split(cvsSplitBy);

				MIAttendance ia = new MIAttendance(getCtx(), 0, get_TrxName());
				ia.setBPartnerValue(csvLine[0]);
				Date parsedDate = dateFormat.parse(csvLine[1].replace("\"", ""));
				Timestamp time1 = new Timestamp(parsedDate.getTime()); 
				ia.setAttendanceDate(time1);
				ia.saveEx();


			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		commitEx();
		return null;
	}

	public String writeAttendance() throws Exception{
		// TODO Auto-generated method stub
		//MBPartner bp = new MBPartner(getCtx(), C_BPartner_ID, get_TrxName());
		//StringBuilder clientCheck = new StringBuilder(" AND AD_Client_ID=").append(getAD_Client_ID());
		//StringBuilder sql = new StringBuilder ("DELETE HR_AttendanceLine ")
		//		.append("WHERE HR_Attendance_ID=").append(RECORD_ID).append (clientCheck);
		//int	no = DB.executeUpdate(sql.toString(), get_TrxName());
		//if (log.isLoggable(Level.FINE)) log.fine("Delete Attendance =" + no);		
		//List<MMarking> listattendance = new Query(getCtx(),MMarking.Table_Name,"MarkingDate between ? and ? AND QtyOfHours1 is not null ",get_TrxName()).setParameters(MarkingDateFrom,MarkingDateTo).setOrderBy("Value").list();
		MHR_Attendance attendance = new MHR_Attendance(getCtx(), RECORD_ID, get_TrxName());

		MAttachment attachment = attendance.getAttachment();
		if (attachment == null) {
			return "@Error@Please attach the attendance file before running the process";
		}

		MAttachmentEntry entry = attachment.getEntry(0);
		if (entry == null) {
			return "@Error@Please attach the attendance file before running the process";
		}
		File csvFile = entry.getFile();

		String line = "";
		String cvsSplitBy = ",";

		try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
			String day = "";
			String emp = "";
			int alID = 0;
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
			SimpleDateFormat dateFormat2 = new SimpleDateFormat("dd/MM/yyyy");
			int i = 1;
			//line = br.readLine();
			Timestamp time1=null;

			Timestamp time2=null;
			Timestamp time3=null;
			Timestamp time4=null;
			while ((line = br.readLine()) != null) {

				// use comma as separator
				String[] csvLine = line.split(cvsSplitBy);
				//List<MHR_AttendanceLine> allist= new Query(getCtx(), MHR_AttendanceLine.Table_Name, "HR_Attendance_ID=?", get_TrxName()).list();

				if(day.compareTo(dateFormat2.parse(csvLine[1].replace("\"", "")).toString())!=0 || emp.compareTo(csvLine[0].trim())!=0) {
					i=1;
					MHR_AttendanceLine al= new MHR_AttendanceLine(getCtx(), 0, get_TrxName());
					al.setHR_Attendance_ID(attendance.get_ID());
					MBPartner employed = new Query(getCtx(), MBPartner.Table_Name, "Value=?", get_TrxName()).setParameters(csvLine[0]).first();
					if(employed==null) {
						log.severe("No se encuentra el empleado "+csvLine[0]);
						return "@Error@ No se encuentra el empleado "+csvLine[0];
					}
					
					al.setC_BPartner_ID(employed.getC_BPartner_ID());
					Date parsedDate = dateFormat2.parse(csvLine[1].replace("\"", ""));
					//Date parsedDateaux = dateFormat2.parse(csvLine[1].replace("\"", ""));
					al.setWeekDay(getWeekDayValue(parsedDate));

					//if(csvLine.length>=2) {

					al.setAttendanceDate(new Timestamp(parsedDate.getTime()));
					//if(i==1) {
					parsedDate = dateFormat.parse(csvLine[1].replace("\"", ""));
					time1 = new Timestamp(parsedDate.getTime()); 
					al.setTime1(time1);
					//}

					//}

					al.saveEx();
					this.statusUpdate("Procesando: "+employed.getValue()+" "+employed.getName()+" "+al.getAttendanceDate());
					day=dateFormat2.parse(csvLine[1].replace("\"", "")).toString();
					emp=csvLine[0];
					alID=al.get_ID();
				}else {
					MHR_AttendanceLine al= new MHR_AttendanceLine(getCtx(), alID, get_TrxName());
					if(csvLine.length>=2) {
						Date parsedDate = dateFormat2.parse(csvLine[1].replace("\"", ""));

						if(i==2) {
							parsedDate = dateFormat.parse(csvLine[1]);
							time2 = new Timestamp(parsedDate.getTime());
							al.setTime2(time2);
							BigDecimal QtyOfHours1=BigDecimal.ZERO;
							if(time1!=null && time2!=null) {
								QtyOfHours1 = BigDecimal.valueOf((time2.getTime()-time1.getTime())/(1000*60)).divide(BigDecimal.valueOf(60),RoundingMode.HALF_EVEN);

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
								QtyOfHours2 = BigDecimal.valueOf((time4.getTime()-time3.getTime())/(1000*60)).divide(BigDecimal.valueOf(60),RoundingMode.HALF_EVEN);

							}
							al.setQtyOfHours2(QtyOfHours2);
							al.setTotalQtyOfHours(al.getTotalQtyOfHours().add(QtyOfHours2));
						}
					}
					al.saveEx();


				}
				i++;
			}

		} catch (IOException e) {
			e.printStackTrace();
		}


		return null;
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

}
