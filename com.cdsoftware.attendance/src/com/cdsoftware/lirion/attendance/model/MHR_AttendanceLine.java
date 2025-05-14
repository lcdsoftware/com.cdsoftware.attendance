package com.cdsoftware.lirion.attendance.model;

import java.sql.ResultSet;
import java.util.Properties;

public class MHR_AttendanceLine extends X_HR_AttendanceLine{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7772046405597547502L;

	public MHR_AttendanceLine(Properties ctx, int HR_AttendanceLine_ID, String trxName) {
		super(ctx, HR_AttendanceLine_ID, trxName);
		// TODO Auto-generated constructor stub
	}
	public MHR_AttendanceLine(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
		// TODO Auto-generated constructor stub
	}

	

}
