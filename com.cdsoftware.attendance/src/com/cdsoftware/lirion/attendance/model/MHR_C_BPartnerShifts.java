package com.cdsoftware.lirion.attendance.model;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Properties;

public class MHR_C_BPartnerShifts extends X_HR_C_BPartnerShifts{

	private static final long serialVersionUID = 1L;

	public MHR_C_BPartnerShifts(Properties ctx, int HR_C_BPartnerShifts_ID, String trxName) {
		super(ctx, HR_C_BPartnerShifts_ID, trxName);
		// TODO Auto-generated constructor stub
	}

	public MHR_C_BPartnerShifts(Properties ctx, int HR_C_BPartnerShifts_ID, String trxName, String[] virtualColumns) {
		super(ctx, HR_C_BPartnerShifts_ID, trxName, virtualColumns);
		// TODO Auto-generated constructor stub
	}

	public MHR_C_BPartnerShifts(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
		// TODO Auto-generated constructor stub
	}

	public void setdatefrom(Timestamp p_DateFrom) {
		// TODO Auto-generated method stub
		
	}

	public void setdateto(Timestamp p_DateTo) {
		// TODO Auto-generated method stub
		
	}
	public Timestamp getDateFrom() {
	    return get_ValueAsTimestamp("DateFrom");
	}

	private Timestamp get_ValueAsTimestamp(String string) {
		// TODO Auto-generated method stub
		return null;
	}

	public Timestamp getDateTo() {
	    return get_ValueAsTimestamp("DateTo");
	}

	

	

}
