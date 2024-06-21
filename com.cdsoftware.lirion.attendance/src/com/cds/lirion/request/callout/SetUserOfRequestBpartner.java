package com.cds.lirion.request.callout;

import java.util.Properties;

import org.adempiere.base.annotation.Callout;
import org.compiere.model.GridField;
import org.compiere.model.GridTab;
import org.compiere.model.MBPartner;
import org.compiere.model.MRequest;
import org.compiere.util.Env;

import com.cdsoftware.lirion.attendance.base.CustomCallout;

@Callout(tableName = MRequest.Table_Name, columnName = {MRequest.COLUMNNAME_C_BPartner_ID})
public class SetUserOfRequestBpartner extends CustomCallout{

	@Override
	public String start(Properties ctx, int WindowNo, GridTab mTab, GridField mField, Object value, Object oldValue) {
		MBPartner bp = null;
		if(mTab.getValue("C_BPartner_ID")!=null) {
			if((int)mTab.getValue("C_BPartner_ID")>0)
				bp = MBPartner.get(Env.getCtx(), (int)mTab.getValue("C_BPartner_ID"));		
			int  userID = bp.getPrimaryAD_User_ID();
			if(userID>0)
				mTab.setValue("AD_User_ID", userID);
		}
		return null;
	}

	@Override
	protected String start() {
		// TODO Auto-generated method stub
		return null;
	}

}
