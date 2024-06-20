package com.cds.lirion.request.process;

import org.compiere.model.MRequest;
import org.compiere.model.MSysConfig;
import org.compiere.util.Msg;

import com.cdsoftware.lirion.attendance.base.CustomProcess;

@org.adempiere.base.annotation.Process
public class UpdateSalesrepWithRRHH extends CustomProcess{

	private int RECORD_ID;
	@Override
	protected void prepare() {
		// TODO Auto-generated method stub
		RECORD_ID = this.getRecord_ID();
	}

	@Override
	protected String doIt() throws Exception {
		// TODO Auto-generated method stub
		
		MRequest r = new MRequest(getCtx(), RECORD_ID, get_TrxName());
		int RRHHUser = MSysConfig.getIntValue("RRHHUserIDForRequest", 0, getAD_Client_ID());
		int RRHHRole = MSysConfig.getIntValue("RRHHRoleIDForRequest", 0, getAD_Client_ID());
		if(RRHHRole>0 && RRHHUser>0) {			
			r.setSalesRep_ID(RRHHUser);
			r.setAD_Role_ID(RRHHRole);
			log.warning("El rol asignado es: "+RRHHRole);
		}
		else
			return Msg.getMsg(getCtx(), "NoRRHHUserIDForRequest");
		
		r.saveEx();		
		//r.setSalesRep_ID(RRHHUser);
		//r.setAD_Role_ID(1000002);
		
		
		return null;
	}

}
