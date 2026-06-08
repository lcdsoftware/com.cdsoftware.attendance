/**********************************************************************
 * This file is part of iDempiere ERP Open Source                      *
 * http://www.idempiere.org                                            *
 *                                                                     *
 * Copyright (C) Contributors                                          *
 *                                                                     *
 * This program is free software; you can redistribute it and/or       *
 * modify it under the terms of the GNU General Public License         *
 * as published by the Free Software Foundation; either version 2      *
 * of the License, or (at your option) any later version.              *
 *                                                                     *
 * This program is distributed in the hope that it will be useful,     *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of      *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the        *
 * GNU General Public License for more details.                        *
 *                                                                     *
 * You should have received a copy of the GNU General Public License   *
 * along with this program; if not, write to the Free Software         *
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,          *
 * MA 02110-1301, USA.                                                 *
 *                                                                     *
 * Contributors:                                                       *
 * - Casa del Software                                                 *
 **********************************************************************/
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

/**
 * Callout used to validate Business Partner permissions and tardiness limits.
 * It ensures that a Business Partner does not exceed the maximum allowed requests (3) 
 * per month for specific request types, specifically 'Permisos' (Permissions) and 'Tardanzas' (Tardiness).
 * 
 * Triggered by: M_Request.CDS_R_RequestTypeDetails_ID, R_RequestType_ID, C_BPartner_ID, CDS_StartDate
 */
@Callout(tableName = "M_Request", columnName = {"CDS_R_RequestTypeDetails_ID","R_RequestType_ID","C_BPartner_ID","CDS_StartDate"})
public class CheckBpPermission extends CustomCallout{
	protected CLogger			log = CLogger.getCLogger (getClass());

	/**
	 * Validates the number of requests for the Business Partner in the selected month.
	 * 
	 * @param ctx context
	 * @param WindowNo window no
	 * @param mTab grid tab
	 * @param mField grid field
	 * @param value new value
	 * @param oldValue old value
	 * @return error message if limit exceeded, otherwise null
	 */
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
