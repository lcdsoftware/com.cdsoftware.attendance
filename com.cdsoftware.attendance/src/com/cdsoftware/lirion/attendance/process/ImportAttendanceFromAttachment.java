package com.cdsoftware.lirion.attendance.process;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.compiere.model.MAttachment;
import org.compiere.model.MAttachmentEntry;
import org.compiere.model.MBPartner;
import org.compiere.model.MRefList;
import org.compiere.model.Query;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;

import com.cdsoftware.lirion.attendance.model.MGH_ShiftsLine;
import com.cdsoftware.lirion.attendance.model.MHR_Attendance;
import com.cdsoftware.lirion.attendance.model.MHR_AttendanceLine;
import com.cdsoftware.lirion.attendance.model.MMarking;
/**
 * estructura del csv
 * [0]	dump
 * [1]	codigo tercero
 * [2]	fecha marcacion
 * [3]
 * [4]
 * [5]
 * [6]
 * @author alara
 *
 */

@org.adempiere.base.annotation.Process
public class ImportAttendanceFromAttachment extends SvrProcess{


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
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			SimpleDateFormat dateFormat2 = new SimpleDateFormat("dd/MM/yyyy");
			while ((line = br.readLine()) != null) {

				// use comma as separator
				String[] csvLine = line.split(cvsSplitBy);


				if(day.compareTo(csvLine[2])!=0 || emp.compareTo(csvLine[1])!=0) {
					MHR_AttendanceLine al= new MHR_AttendanceLine(getCtx(), 0, get_TrxName());
					al.setHR_Attendance_ID(attendance.get_ID());
					MBPartner employed = new Query(getCtx(), MBPartner.Table_Name, "Value=?", get_TrxName()).setParameters(csvLine[1]).first();
					if(employed==null) {
						log.severe("No se encuentra el empleado "+csvLine[1]);
						return "@Error@ No se encuentra el empleado "+csvLine[1];
					}

					al.setC_BPartner_ID(employed.getC_BPartner_ID());
					al.setWeekDay(getWeekDayValue(csvLine[2].replace("\"", "")));
					if(csvLine.length>=5) {
						Date parsedDate = dateFormat2.parse(csvLine[3].replace("\"", ""));
						al.setAttendanceDate(new Timestamp(parsedDate.getTime()));
						Timestamp time1=null;
						if(csvLine[4]!=null) {
							parsedDate = dateFormat.parse(csvLine[3].replace("\"", "") +" "+ csvLine[4].replace("\"", ""));
							time1 = new Timestamp(parsedDate.getTime()); 
							al.setTime1(time1);
						}
						Timestamp time2=null;
						if(csvLine.length>=7) {
							if(csvLine[6]!=null) {
								parsedDate = dateFormat.parse(csvLine[3].replace("\"", "") +" "+ csvLine[6].replace("\"", ""));
								time2 = new Timestamp(parsedDate.getTime()); 
								al.setTime2(time2);
							}
						}
						BigDecimal QtyOfHours1=BigDecimal.ZERO;
						if(time1!=null && time2!=null) {
							QtyOfHours1 = BigDecimal.valueOf((time2.getTime()-time1.getTime())/(1000*60)).divide(BigDecimal.valueOf(60),2, BigDecimal.ROUND_HALF_EVEN);
							
						}
						al.setQtyOfHours1(QtyOfHours1);
						al.setTotalQtyOfHours(al.getTotalQtyOfHours().add(QtyOfHours1));
					}
					else if(attendance.isCompleteWithShift()){
						MGH_ShiftsLine sl = new Query(getCtx(),MGH_ShiftsLine.Table_Name, "WeekDay=?",get_TrxName())
								.setParameters(getWeekDayValue(csvLine[2].replace("\"", "")))
								.first();
						al.setTime1(sl.getTime1());
						al.setTime2(sl.getTime2());
						BigDecimal QtyOfHours1 = BigDecimal.valueOf((sl.getTime2().getTime()-sl.getTime1().getTime())/(1000*60)).divide(BigDecimal.valueOf(60),2, BigDecimal.ROUND_HALF_EVEN);
						al.setQtyOfHours1(QtyOfHours1);
						al.setTotalQtyOfHours(al.getTotalQtyOfHours().add(QtyOfHours1));

					}

					al.saveEx();

					day=csvLine[2];
					emp=csvLine[1];
					alID=al.get_ID();
				}else {
					MHR_AttendanceLine al= new MHR_AttendanceLine(getCtx(), alID, get_TrxName());
					if(csvLine.length>=5) {
						Date parsedDate = dateFormat2.parse(csvLine[3].replace("\"", ""));
						Timestamp time3=null;
						if(csvLine[4]!=null) {
							parsedDate = dateFormat.parse(csvLine[3] +" "+ csvLine[4].replace("\"", ""));
							time3 = new Timestamp(parsedDate.getTime());
							al.setTime3(time3);
						}
						Timestamp time4=null;
						if(csvLine.length>=7) {
							if(csvLine[6]!=null) {
								parsedDate = dateFormat.parse(csvLine[3].replace("\"", "") +" "+ csvLine[6].replace("\"", ""));
								time4 = new Timestamp(parsedDate.getTime());
								al.setTime4(time4);
							}
						}
						BigDecimal QtyOfHours2=BigDecimal.ZERO;
						if(time3!=null && time4!=null) {
							QtyOfHours2 = BigDecimal.valueOf((time4.getTime()-time3.getTime())/(1000*60)).divide(BigDecimal.valueOf(60),2, BigDecimal.ROUND_HALF_EVEN);
							
						}
						al.setQtyOfHours2(QtyOfHours2);
						al.setTotalQtyOfHours(al.getTotalQtyOfHours().add(QtyOfHours2));
					}else if(attendance.isCompleteWithShift()){

						MGH_ShiftsLine sl = new Query(getCtx(),MGH_ShiftsLine.Table_Name,"WeekDay=?",get_TrxName())
								.setParameters(getWeekDayValue(csvLine[2].replace("\"", "")))
								.first();
						al.setTime3(sl.getTime3());
						al.setTime4(sl.getTime4());
						BigDecimal QtyOfHours2 = BigDecimal.valueOf((sl.getTime4().getTime()-sl.getTime3().getTime())/(1000*60)).divide(BigDecimal.valueOf(60),2, BigDecimal.ROUND_HALF_EVEN);
						al.setQtyOfHours2(QtyOfHours2);
						al.setTotalQtyOfHours(al.getTotalQtyOfHours().add(QtyOfHours2));

					}
					al.saveEx();
					day="";
					emp="";
					alID=0;
				}



			}

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


	protected String getWeekDayValue(String WeekDayStr) {

		List<MRefList> reflist = new Query(getCtx(), MRefList.Table_Name, "AD_Reference_ID=?",get_TrxName()).setParameters(167).list();

		for(MRefList ref : reflist) {
			if(ref.getName().trim().compareToIgnoreCase(WeekDayStr)==0) 
				return ref.getValue();

		}
		return null;

	}

}
