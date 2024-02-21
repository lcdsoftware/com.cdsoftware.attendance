package com.cdsoftware.lirion.attendance.model;

import java.sql.ResultSet;
import java.util.Properties;

public class MGH_ShiftsLine extends X_GH_ShiftsLine{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1260030491420710831L;

	public MGH_ShiftsLine(Properties ctx, int GH_ShiftsLine_ID, String trxName) {
		super(ctx, GH_ShiftsLine_ID, trxName);
		// TODO Auto-generated constructor stub
	}
	
	public MGH_ShiftsLine(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
		// TODO Auto-generated constructor stub
	}

	

}
