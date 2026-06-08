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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.Properties;

import org.adempiere.base.annotation.Callout;
import org.compiere.model.GridField;
import org.compiere.model.GridTab;
import org.compiere.util.CLogger;
import org.compiere.util.Env;

import com.cdsoftware.lirion.attendance.base.CustomCallout;

/**
 * Callout used to calculate the duration between timestamps in attendance-related records.
 * It computes hours for two potential intervals (Time1/Time2 and Time3/Time4), 
 * typically representing shifts and breaks, and updates the respective quantity fields 
 * as well as the total quantity of hours.
 * 
 * Triggered by: I_Marking, GH_ShiftsLine, HR_AttendanceLine.Time1, Time2, Time3, Time4
 */
@Callout(tableName = {"I_Marking","GH_ShiftsLine","HR_AttendanceLine"}, columnName = {"Time1","Time2","Time3","Time4"})
public class SetQtyOfHours extends CustomCallout{
	protected transient CLogger	log = CLogger.getCLogger (getClass());
	
	/**
	 * Calculates QtyOfHours1, QtyOfHours2 and TotalQtyOfHours based on the input times.
	 * Handles midnight crossing by adding 24 hours if the end time is before the start time.
	 * 
	 * @param ctx context
	 * @param WindowNo window no
	 * @param mTab grid tab
	 * @param mField grid field
	 * @param value new value
	 * @param oldValue old value
	 * @return null
	 */
	@Override
	public String start(Properties ctx, int WindowNo, GridTab mTab, GridField mField, Object value, Object oldValue) {
		// TODO Auto-generated method stub
		
		Timestamp time1= (Timestamp)mTab.getValue("Time1");
		Timestamp time2= (Timestamp)mTab.getValue("Time2");
		Timestamp time3= (Timestamp)mTab.getValue("Time3");
		Timestamp time4= (Timestamp)mTab.getValue("Time4");
		BigDecimal QtyOfHours1 = BigDecimal.ZERO;
		BigDecimal QtyOfHours2 = BigDecimal.ZERO;
		
		if (time1!=null && time2!=null) {
			QtyOfHours1 = BigDecimal.valueOf((time2.getTime()-time1.getTime())/(1000*60)).divide(BigDecimal.valueOf(60),2, RoundingMode.HALF_EVEN);
			if(QtyOfHours1.compareTo(Env.ZERO)<0)
				QtyOfHours1 = QtyOfHours1.add(new BigDecimal(24));
			mTab.setValue("QtyOfHours1", QtyOfHours1);
		}

		if (time3!=null && time4!=null) {
			QtyOfHours2 = BigDecimal.valueOf((time4.getTime()-time3.getTime())/(1000*60)).divide(BigDecimal.valueOf(60),2, RoundingMode.HALF_EVEN);
			if(QtyOfHours2.compareTo(Env.ZERO)<0)
				QtyOfHours2 = QtyOfHours2.add(new BigDecimal(24));
			mTab.setValue("QtyOfHours2", QtyOfHours2);
		}
		BigDecimal TotalQtyOfHours = QtyOfHours1.add(QtyOfHours2);
		if(mTab.getField("TotalQtyOfHours") != null)
			mTab.setValue("TotalQtyOfHours", TotalQtyOfHours);
			
		return null;
	}

	@Override
	protected String start() {
		// TODO Auto-generated method stub
		return null;
	}

}
