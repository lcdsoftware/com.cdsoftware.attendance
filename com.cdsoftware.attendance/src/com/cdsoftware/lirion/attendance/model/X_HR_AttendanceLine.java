/******************************************************************************
 * Product: iDempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 1999-2012 ComPiere, Inc. All Rights Reserved.                *
 * This program is free software, you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY, without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program, if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * ComPiere, Inc., 2620 Augustine Dr. #245, Santa Clara, CA 95054, USA        *
 * or via info@compiere.org or http://www.compiere.org/license.html           *
 *****************************************************************************/
/** Generated Model - DO NOT CHANGE */
package com.cdsoftware.lirion.attendance.model;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Properties;
import org.compiere.model.*;
import org.compiere.util.Env;

/** Generated Model for HR_AttendanceLine
 *  @author iDempiere (generated) 
 *  @version Release 7.1 - $Id$ */

@org.adempiere.base.Model(table="HR_AttendanceLine")
public class X_HR_AttendanceLine extends PO implements I_HR_AttendanceLine, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20200228L;

    /** Standard Constructor */
    public X_HR_AttendanceLine (Properties ctx, int HR_AttendanceLine_ID, String trxName)
    {
      super (ctx, HR_AttendanceLine_ID, trxName);
      /** if (HR_AttendanceLine_ID == 0)
        {
			setHR_AttendanceLine_ID (0);
        } */
    }

    /** Load Constructor */
    public X_HR_AttendanceLine (Properties ctx, ResultSet rs, String trxName)
    {
      super (ctx, rs, trxName);
    }

    /** AccessLevel
      * @return 3 - Client - Org 
      */
    protected int get_AccessLevel()
    {
      return accessLevel.intValue();
    }

    /** Load Meta Data */
    protected POInfo initPO (Properties ctx)
    {
      POInfo poi = POInfo.getPOInfo (ctx, Table_ID, get_TrxName());
      return poi;
    }

    public String toString()
    {
      StringBuffer sb = new StringBuffer ("X_HR_AttendanceLine[")
        .append(get_ID()).append("]");
      return sb.toString();
    }

	/** Set Attendance Date.
		@param AttendanceDate Attendance Date	  */
	public void setAttendanceDate (Timestamp AttendanceDate)
	{
		set_Value (COLUMNNAME_AttendanceDate, AttendanceDate);
	}

	/** Get Attendance Date.
		@return Attendance Date	  */
	public Timestamp getAttendanceDate () 
	{
		return (Timestamp)get_Value(COLUMNNAME_AttendanceDate);
	}

	/** Long Day = LD */
	public static final String AUTHORIZATIONTYPE_LongDay = "LD";
	/** Lunch Autrorized = LA */
	public static final String AUTHORIZATIONTYPE_LunchAutrorized = "LA";
	/** Set AuthorizationType.
		@param AuthorizationType AuthorizationType	  */
	public void setAuthorizationType (String AuthorizationType)
	{

		set_Value (COLUMNNAME_AuthorizationType, AuthorizationType);
	}

	/** Get AuthorizationType.
		@return AuthorizationType	  */
	public String getAuthorizationType () 
	{
		return (String)get_Value(COLUMNNAME_AuthorizationType);
	}

	public org.compiere.model.I_C_BPartner getC_BPartner() throws RuntimeException
    {
		return (org.compiere.model.I_C_BPartner)MTable.get(getCtx(), org.compiere.model.I_C_BPartner.Table_Name)
			.getPO(getC_BPartner_ID(), get_TrxName());	}

	/** Set Business Partner .
		@param C_BPartner_ID 
		Identifies a Business Partner
	  */
	public void setC_BPartner_ID (int C_BPartner_ID)
	{
		if (C_BPartner_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_C_BPartner_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_C_BPartner_ID, Integer.valueOf(C_BPartner_ID));
	}

	/** Get Business Partner .
		@return Identifies a Business Partner
	  */
	public int getC_BPartner_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_BPartner_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	public I_HR_Attendance getHR_Attendance() throws RuntimeException
    {
		return (I_HR_Attendance)MTable.get(getCtx(), I_HR_Attendance.Table_Name)
			.getPO(getHR_Attendance_ID(), get_TrxName());	}

	/** Set Attendance.
		@param HR_Attendance_ID Attendance	  */
	public void setHR_Attendance_ID (int HR_Attendance_ID)
	{
		if (HR_Attendance_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_HR_Attendance_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_HR_Attendance_ID, Integer.valueOf(HR_Attendance_ID));
	}

	/** Get Attendance.
		@return Attendance	  */
	public int getHR_Attendance_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_HR_Attendance_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Attendance Line.
		@param HR_AttendanceLine_ID Attendance Line	  */
	public void setHR_AttendanceLine_ID (int HR_AttendanceLine_ID)
	{
		if (HR_AttendanceLine_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_HR_AttendanceLine_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_HR_AttendanceLine_ID, Integer.valueOf(HR_AttendanceLine_ID));
	}

	/** Get Attendance Line.
		@return Attendance Line	  */
	public int getHR_AttendanceLine_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_HR_AttendanceLine_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set HR_AttendanceLine_UU.
		@param HR_AttendanceLine_UU HR_AttendanceLine_UU	  */
	public void setHR_AttendanceLine_UU (String HR_AttendanceLine_UU)
	{
		set_ValueNoCheck (COLUMNNAME_HR_AttendanceLine_UU, HR_AttendanceLine_UU);
	}

	/** Get HR_AttendanceLine_UU.
		@return HR_AttendanceLine_UU	  */
	public String getHR_AttendanceLine_UU () 
	{
		return (String)get_Value(COLUMNNAME_HR_AttendanceLine_UU);
	}

	/** Set Difference in Minutes Shift 1.
		@param QtyMinutesDifference1 
		Difference Quantity
	  */
	public void setQtyMinutesDifference1 (BigDecimal QtyMinutesDifference1)
	{
		set_Value (COLUMNNAME_QtyMinutesDifference1, QtyMinutesDifference1);
	}

	/** Get Difference in Minutes Shift 1.
		@return Difference Quantity
	  */
	public BigDecimal getQtyMinutesDifference1 () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_QtyMinutesDifference1);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	/** Set Difference in Minutes Shift 2.
		@param QtyMinutesDifference2 
		Difference Quantity
	  */
	public void setQtyMinutesDifference2 (BigDecimal QtyMinutesDifference2)
	{
		set_Value (COLUMNNAME_QtyMinutesDifference2, QtyMinutesDifference2);
	}

	/** Get Difference in Minutes Shift 2.
		@return Difference Quantity
	  */
	public BigDecimal getQtyMinutesDifference2 () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_QtyMinutesDifference2);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	/** Set Qty Of Hours 1.
		@param QtyOfHours1 Qty Of Hours 1	  */
	public void setQtyOfHours1 (BigDecimal QtyOfHours1)
	{
		set_Value (COLUMNNAME_QtyOfHours1, QtyOfHours1);
	}

	/** Get Qty Of Hours 1.
		@return Qty Of Hours 1	  */
	public BigDecimal getQtyOfHours1 () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_QtyOfHours1);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	/** Set Qty Of Hours 2.
		@param QtyOfHours2 Qty Of Hours 2	  */
	public void setQtyOfHours2 (BigDecimal QtyOfHours2)
	{
		set_Value (COLUMNNAME_QtyOfHours2, QtyOfHours2);
	}

	/** Get Qty Of Hours 2.
		@return Qty Of Hours 2	  */
	public BigDecimal getQtyOfHours2 () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_QtyOfHours2);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	/** Set Time1.
		@param Time1 Time1	  */
	public void setTime1 (Timestamp Time1)
	{
		set_Value (COLUMNNAME_Time1, Time1);
	}

	/** Get Time1.
		@return Time1	  */
	public Timestamp getTime1 () 
	{
		return (Timestamp)get_Value(COLUMNNAME_Time1);
	}

	/** Set Time2.
		@param Time2 Time2	  */
	public void setTime2 (Timestamp Time2)
	{
		set_Value (COLUMNNAME_Time2, Time2);
	}

	/** Get Time2.
		@return Time2	  */
	public Timestamp getTime2 () 
	{
		return (Timestamp)get_Value(COLUMNNAME_Time2);
	}

	/** Set Time3.
		@param Time3 Time3	  */
	public void setTime3 (Timestamp Time3)
	{
		set_Value (COLUMNNAME_Time3, Time3);
	}

	/** Get Time3.
		@return Time3	  */
	public Timestamp getTime3 () 
	{
		return (Timestamp)get_Value(COLUMNNAME_Time3);
	}

	/** Set Time4.
		@param Time4 Time4	  */
	public void setTime4 (Timestamp Time4)
	{
		set_Value (COLUMNNAME_Time4, Time4);
	}

	/** Get Time4.
		@return Time4	  */
	public Timestamp getTime4 () 
	{
		return (Timestamp)get_Value(COLUMNNAME_Time4);
	}

	/** Set Total Qty Of Hours.
		@param TotalQtyOfHours Total Qty Of Hours	  */
	public void setTotalQtyOfHours (BigDecimal TotalQtyOfHours)
	{
		set_Value (COLUMNNAME_TotalQtyOfHours, TotalQtyOfHours);
	}

	/** Get Total Qty Of Hours.
		@return Total Qty Of Hours	  */
	public BigDecimal getTotalQtyOfHours () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_TotalQtyOfHours);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	/** WeekDay AD_Reference_ID=167 */
	public static final int WEEKDAY_AD_Reference_ID=167;
	/** Sunday = 7 */
	public static final String WEEKDAY_Sunday = "7";
	/** Monday = 1 */
	public static final String WEEKDAY_Monday = "1";
	/** Tuesday = 2 */
	public static final String WEEKDAY_Tuesday = "2";
	/** Wednesday = 3 */
	public static final String WEEKDAY_Wednesday = "3";
	/** Thursday = 4 */
	public static final String WEEKDAY_Thursday = "4";
	/** Friday = 5 */
	public static final String WEEKDAY_Friday = "5";
	/** Saturday = 6 */
	public static final String WEEKDAY_Saturday = "6";
	/** Set Day of the Week.
		@param WeekDay 
		Day of the Week
	  */
	public void setWeekDay (String WeekDay)
	{

		set_Value (COLUMNNAME_WeekDay, WeekDay);
	}

	/** Get Day of the Week.
		@return Day of the Week
	  */
	public String getWeekDay () 
	{
		return (String)get_Value(COLUMNNAME_WeekDay);
	}
}