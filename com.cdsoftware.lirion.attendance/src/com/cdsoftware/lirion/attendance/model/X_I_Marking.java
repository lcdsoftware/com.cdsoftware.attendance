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

/** Generated Model for I_Marking
 *  @author iDempiere (generated) 
 *  @version Release 7.1 - $Id$ */
public class X_I_Marking extends PO implements I_I_Marking, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20200122L;

    /** Standard Constructor */
    public X_I_Marking (Properties ctx, int I_Marking_ID, String trxName)
    {
      super (ctx, I_Marking_ID, trxName);
      /** if (I_Marking_ID == 0)
        {
			setI_IsImported (false);
			setI_Marking_ID (0);
        } */
    }

    /** Load Constructor */
    public X_I_Marking (Properties ctx, ResultSet rs, String trxName)
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
      StringBuffer sb = new StringBuffer ("X_I_Marking[")
        .append(get_ID()).append("]");
      return sb.toString();
    }

	public org.compiere.model.I_AD_User getAD_User() throws RuntimeException
    {
		return (org.compiere.model.I_AD_User)MTable.get(getCtx(), org.compiere.model.I_AD_User.Table_Name)
			.getPO(getAD_User_ID(), get_TrxName());	}

	/** Set User/Contact.
		@param AD_User_ID 
		User within the system - Internal or Business Partner Contact
	  */
	public void setAD_User_ID (int AD_User_ID)
	{
		if (AD_User_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_AD_User_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_AD_User_ID, Integer.valueOf(AD_User_ID));
	}

	/** Get User/Contact.
		@return User within the system - Internal or Business Partner Contact
	  */
	public int getAD_User_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_AD_User_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
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

	/** Set Import Error Message.
		@param I_ErrorMsg 
		Messages generated from import process
	  */
	public void setI_ErrorMsg (String I_ErrorMsg)
	{
		set_Value (COLUMNNAME_I_ErrorMsg, I_ErrorMsg);
	}

	/** Get Import Error Message.
		@return Messages generated from import process
	  */
	public String getI_ErrorMsg () 
	{
		return (String)get_Value(COLUMNNAME_I_ErrorMsg);
	}

	/** Set Imported.
		@param I_IsImported 
		Has this import been processed
	  */
	public void setI_IsImported (boolean I_IsImported)
	{
		set_Value (COLUMNNAME_I_IsImported, Boolean.valueOf(I_IsImported));
	}

	/** Get Imported.
		@return Has this import been processed
	  */
	public boolean isI_IsImported () 
	{
		Object oo = get_Value(COLUMNNAME_I_IsImported);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Import Marking.
		@param I_Marking_ID Import Marking	  */
	public void setI_Marking_ID (int I_Marking_ID)
	{
		if (I_Marking_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_I_Marking_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_I_Marking_ID, Integer.valueOf(I_Marking_ID));
	}

	/** Get Import Marking.
		@return Import Marking	  */
	public int getI_Marking_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_I_Marking_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set I_Marking_UU.
		@param I_Marking_UU I_Marking_UU	  */
	public void setI_Marking_UU (String I_Marking_UU)
	{
		set_ValueNoCheck (COLUMNNAME_I_Marking_UU, I_Marking_UU);
	}

	/** Get I_Marking_UU.
		@return I_Marking_UU	  */
	public String getI_Marking_UU () 
	{
		return (String)get_Value(COLUMNNAME_I_Marking_UU);
	}

	/** Set MarkingDate.
		@param MarkingDate MarkingDate	  */
	public void setMarkingDate (Timestamp MarkingDate)
	{
		set_Value (COLUMNNAME_MarkingDate, MarkingDate);
	}

	/** Get MarkingDate.
		@return MarkingDate	  */
	public Timestamp getMarkingDate () 
	{
		return (Timestamp)get_Value(COLUMNNAME_MarkingDate);
	}

	/** Set Processed.
		@param Processed 
		The document has been processed
	  */
	public void setProcessed (boolean Processed)
	{
		set_Value (COLUMNNAME_Processed, Boolean.valueOf(Processed));
	}

	/** Get Processed.
		@return The document has been processed
	  */
	public boolean isProcessed () 
	{
		Object oo = get_Value(COLUMNNAME_Processed);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Process Now.
		@param Processing Process Now	  */
	public void setProcessing (boolean Processing)
	{
		set_Value (COLUMNNAME_Processing, Boolean.valueOf(Processing));
	}

	/** Get Process Now.
		@return Process Now	  */
	public boolean isProcessing () 
	{
		Object oo = get_Value(COLUMNNAME_Processing);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
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

	/** Set Search Key.
		@param Value 
		Search key for the record in the format required - must be unique
	  */
	public void setValue (String Value)
	{
		set_Value (COLUMNNAME_Value, Value);
	}

	/** Get Search Key.
		@return Search key for the record in the format required - must be unique
	  */
	public String getValue () 
	{
		return (String)get_Value(COLUMNNAME_Value);
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

	/** Set Day of the Week String.
		@param WeekDayStr 
		Day of the Week String
	  */
	public void setWeekDayStr (String WeekDayStr)
	{
		set_Value (COLUMNNAME_WeekDayStr, WeekDayStr);
	}

	/** Get Day of the Week String.
		@return Day of the Week String
	  */
	public String getWeekDayStr () 
	{
		return (String)get_Value(COLUMNNAME_WeekDayStr);
	}

	@Override
	public void setTime3(Timestamp Time3) {
		set_Value (COLUMNNAME_Time3, Time3);
		
	}

	@Override
	public Timestamp getTime3() {
		return (Timestamp)get_Value(COLUMNNAME_Time3);
	}

	@Override
	public void setTime4(Timestamp Time4) {
		set_Value (COLUMNNAME_Time4, Time4);
		
	}

	@Override
	public Timestamp getTime4() {
		return (Timestamp)get_Value(COLUMNNAME_Time4);
	}
}