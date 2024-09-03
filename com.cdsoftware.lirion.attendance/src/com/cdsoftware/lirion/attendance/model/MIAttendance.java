package com.cdsoftware.lirion.attendance.model;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Properties;

public class MIAttendance extends X_I_Attendance{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public MIAttendance(Properties ctx, int I_Attendance_ID, String trxName) {
		super(ctx, I_Attendance_ID, trxName);
		// TODO Auto-generated constructor stub
	}

	public MIAttendance(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
		// TODO Auto-generated constructor stub
	}

	public void setBPartnerValue(String string) {
		// TODO Auto-generated method stub
		
	}

	public void setAttendanceDate(Timestamp time1) {
		// TODO Auto-generated method stub
		
	}

	

}
