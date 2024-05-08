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

/** Generated Model for I_Attendance
 *  @author iDempiere (generated) 
 *  @version Release 7.1 - $Id$ */
@org.adempiere.base.Model(table="I_Attendance")
public class X_I_Attendance extends PO implements I_I_Attendance, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20200122L;

    /** Standard Constructor */
    public X_I_Attendance (Properties ctx, int I_Attendance_ID, String trxName)
    {
      super (ctx, I_Attendance_ID, trxName);
      /** if (I_Attendance_ID == 0)
        {
			setI_Attendance_ID (0);
			setName (null);
        } */
    }

    /** Load Constructor */
    public X_I_Attendance (Properties ctx, ResultSet rs, String trxName)
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
      StringBuffer sb = new StringBuffer ("X_I_Attendance[")
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

	/** Set Business Partner Key.
		@param BPartnerValue 
		Key of the Business Partner
	  */
	public void setBPartnerValue (String BPartnerValue)
	{
		set_Value (COLUMNNAME_BPartnerValue, BPartnerValue);
	}

	/** Get Business Partner Key.
		@return Key of the Business Partner
	  */
	public String getBPartnerValue () 
	{
		return (String)get_Value(COLUMNNAME_BPartnerValue);
	}

	/** Set Description.
		@param Description 
		Optional short description of the record
	  */
	public void setDescription (String Description)
	{
		set_Value (COLUMNNAME_Description, Description);
	}

	/** Get Description.
		@return Optional short description of the record
	  */
	public String getDescription () 
	{
		return (String)get_Value(COLUMNNAME_Description);
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

	/** Set Import Attendance.
		@param I_Attendance_ID Import Attendance	  */
	public void setI_Attendance_ID (int I_Attendance_ID)
	{
		if (I_Attendance_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_I_Attendance_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_I_Attendance_ID, Integer.valueOf(I_Attendance_ID));
	}

	/** Get Import Attendance.
		@return Import Attendance	  */
	public int getI_Attendance_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_I_Attendance_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set I_Attendance_UU.
		@param I_Attendance_UU I_Attendance_UU	  */
	public void setI_Attendance_UU (String I_Attendance_UU)
	{
		set_Value (COLUMNNAME_I_Attendance_UU, I_Attendance_UU);
	}

	/** Get I_Attendance_UU.
		@return I_Attendance_UU	  */
	public String getI_Attendance_UU () 
	{
		return (String)get_Value(COLUMNNAME_I_Attendance_UU);
	}

	/** Set Name.
		@param Name 
		Alphanumeric identifier of the entity
	  */
	public void setName (String Name)
	{
		set_Value (COLUMNNAME_Name, Name);
	}

	/** Get Name.
		@return Alphanumeric identifier of the entity
	  */
	public String getName () 
	{
		return (String)get_Value(COLUMNNAME_Name);
	}
}