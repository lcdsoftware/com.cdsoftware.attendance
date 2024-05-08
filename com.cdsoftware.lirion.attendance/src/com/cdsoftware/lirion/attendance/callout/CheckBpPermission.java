package com.cdsoftware.lirion.attendance.callout;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Properties;

import org.adempiere.base.annotation.Callout;
import org.compiere.model.GridField;
import org.compiere.model.GridTab;
import org.compiere.model.Query;
import org.compiere.util.CLogger;
import org.compiere.util.Env;
import org.compiere.util.Msg;

import com.cdsoftware.lirion.attendance.base.CustomCallout;

@Callout(tableName = "M_Request", columnName = {"CDS_R_RequestTypeDetails_ID","R_RequestType_ID","C_BPartner_ID","CDS_StartDate"})
public class CheckBpPermission extends CustomCallout{
	protected CLogger			log = CLogger.getCLogger (getClass());
	@Override
	public String start(Properties ctx, int WindowNo, GridTab mTab, GridField mField, Object value, Object oldValue) {
		log.warning("TEST");
		int bpartnerid = 0;
		int subtypeid = 0;
		int typeid = 0;
		int qty = 0;
		Timestamp datefilter = null;
		Calendar cal = Calendar.getInstance();
		
		//para controlar el maximo de permisos y tardanzas por mes
		
		if(mTab.getValue("C_BPartner_ID") == null)
			return "";
		if(mTab.getValue("R_RequestType_ID") == null)
			return "";
		if(mTab.getValue("CDS_R_RequestTypeDetails_ID") == null)
			return "";
		if(mTab.getValue("CDS_StartDate") == null)
			return "";
		
		
		bpartnerid = (int)mTab.getValue("C_BPartner_ID");
		subtypeid = (int)mTab.getValue("CDS_R_RequestTypeDetails_ID");
		typeid = (int)mTab.getValue("R_RequestType_ID");
		datefilter=(Timestamp)mTab.getValue("CDS_StartDate");
		
		cal.setTime(datefilter);
		int month = cal.get(Calendar.MONTH)+1;
		
		qty = new Query(Env.getCtx(), "R_Request", "C_BPartner_ID = "+bpartnerid
				+ " AND R_RequestType_ID =  "+typeid
				+ " AND EXTRACT(MONTH FROM CDS_StartDate)::numeric=  "+month
				+" AND R_Request.R_RequestType_ID IN (SELECT R_RequestType_ID FROM R_RequestType WHERE R_RequestType_UU='98cbeb41-3602-4559-aa17-2a05fc905602')",	//permisos	
				null).count();
			log.warning("cantidad de solicitudes: "+qty);	
		if(qty>=3) {
			return Msg.getMsg(Env.getCtx(), "SubtypeOver3PerMonth");
		}	
		
		qty = new Query(Env.getCtx(), "R_Request", "C_BPartner_ID = "+bpartnerid
				+ " AND CDS_R_RequestTypeDetails_ID =  "+subtypeid
				+ " AND EXTRACT(MONTH FROM CDS_StartDate)::numeric=  "+month
				+ " AND R_Request.cds_r_requesttypedetails_ID IN (select cds_r_requesttypedetails_ID FROM cds_r_requesttypedetails where cds_r_requesttypedetails_UU='8d28f1d7-e637-4d91-a9a6-de4fecb539cb')",	//tardanzas	
				null).count();
				
		if(qty>=3) {
			return Msg.getMsg(Env.getCtx(), "SubtypeOver3PerMonth");
		}	
		
		return null;
	}

	@Override
	protected String start() {
		// TODO Auto-generated method stub
		return null;
	}

}
