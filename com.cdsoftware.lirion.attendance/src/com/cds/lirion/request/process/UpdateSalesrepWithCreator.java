package com.cds.lirion.request.process;

import org.compiere.model.MRequest;
import org.compiere.model.MUser;

import com.cdsoftware.lirion.attendance.base.CustomProcess;

@org.adempiere.base.annotation.Process
public class UpdateSalesrepWithCreator extends CustomProcess{

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
		//MUser u= new MUser(getCtx(), r.getCreatedBy(), get_TrxName());
		//MUser u= new MUser(getCtx(), r.getAD_User_ID(), get_TrxName());
		
		//r.setSalesRep_ID(r.getCreatedBy());
		r.setSalesRep_ID(r.getAD_User_ID());
		//r.setAD_Role_ID(1000002);

		r.saveEx();
		
		return null;
	}

}
