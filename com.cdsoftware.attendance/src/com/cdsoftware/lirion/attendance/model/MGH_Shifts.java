package com.cdsoftware.lirion.attendance.model;

import java.sql.ResultSet;
import java.util.Properties;

public class MGH_Shifts extends X_GH_Shifts{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1040299876871577919L;

	public MGH_Shifts(Properties ctx, int GH_Shifts_ID, String trxName) {
		super(ctx, GH_Shifts_ID, trxName);
		// TODO Auto-generated constructor stub
	}
	
	public MGH_Shifts(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
		// TODO Auto-generated constructor stub
	}

	

}
