package com.cdsoftware.lirion.attendance.model;

import java.sql.ResultSet;
import java.util.List;
import java.util.Properties;

import org.compiere.model.MRefList;
import org.compiere.model.Query;

public class MMarking extends X_I_Marking{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8579890139738804447L;
	
	public MMarking(Properties ctx, int I_Marking_ID, String trxName) {
		super(ctx, I_Marking_ID, trxName);
		// TODO Auto-generated constructor stub
	}

	public MMarking(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
		// TODO Auto-generated constructor stub
	}

	public String getWeekDayValue() {
		String WeekDayStr = this.getWeekDayStr();
		List<MRefList> reflist = new Query(getCtx(), MRefList.Table_Name, "AD_Reference_ID=?",get_TrxName()).setParameters(167).list();
		
		for(MRefList ref : reflist) {
			if(ref.getName().compareToIgnoreCase(WeekDayStr)==0) 
				return ref.getValue();
			
		}
		return null;
		
	}

}
