package com.cdsoftware.lirion.attendance.model;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.compiere.model.MRefList;
import org.compiere.model.Query;

public class MHR_Attendance extends X_HR_Attendance{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1292782221616426105L;

	public MHR_Attendance(Properties ctx, int HR_Attendance_ID, String trxName) {
		super(ctx, HR_Attendance_ID, trxName);
		// TODO Auto-generated constructor stub
	}
	public MHR_Attendance(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
		// TODO Auto-generated constructor stub
	}
	

	public  String getWeekDayValue(String WeekDayStr) {
		
		List<MRefList> reflist = new Query(getCtx(), MRefList.Table_Name, "AD_Reference_ID=?",get_TrxName()).setParameters(167).list();
		
		for(MRefList ref : reflist) {
			if(ref.getName().compareToIgnoreCase(WeekDayStr)==0) 
				return ref.getValue();
			
		}
		return null;
		
	}

	public String getWeekDayValue(Date WeekDayStr) {
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
