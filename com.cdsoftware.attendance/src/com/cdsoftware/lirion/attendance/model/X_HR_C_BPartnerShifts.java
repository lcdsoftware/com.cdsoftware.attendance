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

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Properties;
import org.compiere.model.*;

/** Generated Model for HR_C_BPartnerShifts
 *  @author iDempiere (generated) 
 *  @version Release 10 - $Id$ */
@org.adempiere.base.Model(table="HR_C_BPartnerShifts")
public class X_HR_C_BPartnerShifts extends PO implements I_HR_C_BPartnerShifts, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20231124L;

    /** Standard Constructor */
    public X_HR_C_BPartnerShifts (Properties ctx, int HR_C_BPartnerShifts_ID, String trxName)
    {
      super (ctx, HR_C_BPartnerShifts_ID, trxName);
      /** if (HR_C_BPartnerShifts_ID == 0)
        {
			setC_BPartner_ID (0);
			setDateFrom (new Timestamp( System.currentTimeMillis() ));
			setDateTo (new Timestamp( System.currentTimeMillis() ));
			setGH_Shifts_ID (0);
			setHR_C_BPartnerShifts_ID (0);
			setValue (null);
        } */
    }

    /** Standard Constructor */
    public X_HR_C_BPartnerShifts (Properties ctx, int HR_C_BPartnerShifts_ID, String trxName, String ... virtualColumns)
    {
      super (ctx, HR_C_BPartnerShifts_ID, trxName, virtualColumns);
      /** if (HR_C_BPartnerShifts_ID == 0)
        {
			setC_BPartner_ID (0);
			setDateFrom (new Timestamp( System.currentTimeMillis() ));
			setDateTo (new Timestamp( System.currentTimeMillis() ));
			setGH_Shifts_ID (0);
			setHR_C_BPartnerShifts_ID (0);
			setValue (null);
        } */
    }

    /** Load Constructor */
    public X_HR_C_BPartnerShifts (Properties ctx, ResultSet rs, String trxName)
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
      StringBuilder sb = new StringBuilder ("X_HR_C_BPartnerShifts[")
        .append(get_ID()).append("]");
      return sb.toString();
    }

	public org.compiere.model.I_C_BPartner getC_BPartner() throws RuntimeException
	{
		return (org.compiere.model.I_C_BPartner)MTable.get(getCtx(), org.compiere.model.I_C_BPartner.Table_ID)
			.getPO(getC_BPartner_ID(), get_TrxName());
	}

	/** Set Business Partner .
		@param C_BPartner_ID Identifies a Business Partner
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
	public int getC_BPartner_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_BPartner_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Date From.
		@param DateFrom Starting date for a range
	*/
	public void setDateFrom (Timestamp DateFrom)
	{
		set_Value (COLUMNNAME_DateFrom, DateFrom);
	}

	/** Get Date From.
		@return Starting date for a range
	  */
	public Timestamp getDateFrom()
	{
		return (Timestamp)get_Value(COLUMNNAME_DateFrom);
	}

	/** Set Date To.
		@param DateTo End date of a date range
	*/
	public void setDateTo (Timestamp DateTo)
	{
		set_Value (COLUMNNAME_DateTo, DateTo);
	}

	/** Get Date To.
		@return End date of a date range
	  */
	public Timestamp getDateTo()
	{
		return (Timestamp)get_Value(COLUMNNAME_DateTo);
	}

	/** Set Description.
		@param Description Optional short description of the record
	*/
	public void setDescription (String Description)
	{
		set_Value (COLUMNNAME_Description, Description);
	}

	/** Get Description.
		@return Optional short description of the record
	  */
	public String getDescription()
	{
		return (String)get_Value(COLUMNNAME_Description);
	}

	public I_GH_Shifts getGH_Shifts() throws RuntimeException
	{
		return (I_GH_Shifts)MTable.get(getCtx(), I_GH_Shifts.Table_ID)
			.getPO(getGH_Shifts_ID(), get_TrxName());
	}

	/** Set Shifts.
		@param GH_Shifts_ID Shifts
	*/
	public void setGH_Shifts_ID (int GH_Shifts_ID)
	{
		if (GH_Shifts_ID < 1)
			set_ValueNoCheck (COLUMNNAME_GH_Shifts_ID, null);
		else
			set_ValueNoCheck (COLUMNNAME_GH_Shifts_ID, Integer.valueOf(GH_Shifts_ID));
	}

	/** Get Shifts.
		@return Shifts	  */
	public int getGH_Shifts_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_GH_Shifts_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set C_BPartner Shifts.
		@param HR_C_BPartnerShifts_ID C_BPartner Shifts
	*/
	public void setHR_C_BPartnerShifts_ID (int HR_C_BPartnerShifts_ID)
	{
		if (HR_C_BPartnerShifts_ID < 1)
			set_ValueNoCheck (COLUMNNAME_HR_C_BPartnerShifts_ID, null);
		else
			set_ValueNoCheck (COLUMNNAME_HR_C_BPartnerShifts_ID, Integer.valueOf(HR_C_BPartnerShifts_ID));
	}

	/** Get C_BPartner Shifts.
		@return C_BPartner Shifts	  */
	public int getHR_C_BPartnerShifts_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_HR_C_BPartnerShifts_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set HR_C_BPartnerShifts_UU.
		@param HR_C_BPartnerShifts_UU HR_C_BPartnerShifts_UU
	*/
	public void setHR_C_BPartnerShifts_UU (String HR_C_BPartnerShifts_UU)
	{
		set_Value (COLUMNNAME_HR_C_BPartnerShifts_UU, HR_C_BPartnerShifts_UU);
	}

	/** Get HR_C_BPartnerShifts_UU.
		@return HR_C_BPartnerShifts_UU	  */
	public String getHR_C_BPartnerShifts_UU()
	{
		return (String)get_Value(COLUMNNAME_HR_C_BPartnerShifts_UU);
	}

	/** Set Search Key.
		@param Value Search key for the record in the format required - must be unique
	*/
	public void setValue (String Value)
	{
		set_Value (COLUMNNAME_Value, Value);
	}

	/** Get Search Key.
		@return Search key for the record in the format required - must be unique
	  */
	public String getValue()
	{
		return (String)get_Value(COLUMNNAME_Value);
	}
}