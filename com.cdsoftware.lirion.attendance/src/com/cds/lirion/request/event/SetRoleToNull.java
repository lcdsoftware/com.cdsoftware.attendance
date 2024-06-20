package com.cds.lirion.request.event;

import org.compiere.model.MRequest;

import com.cdsoftware.lirion.attendance.base.CustomEvent;


public class SetRoleToNull extends CustomEvent {
	//REVISAR
	@Override
	protected void doHandleEvent() {
		// TODO Auto-generated method stub
		
	}

	/**
	@Override
	protected void doHandleEvent() {
		// TODO Auto-generated method stub
		MRequest req = (MRequest)  getPO();
		if(req.getR_Status_ID()!=1000001)
			req.set_ValueOfColumn("AD_Role_ID", null);
		
	}
**/
}
