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
import java.util.Properties;
import org.compiere.model.*;

/** Generated Model for HR_AttendanceDevices
 *  @author iDempiere (generated) 
 *  @version Release 10 - $Id$ */
@org.adempiere.base.Model(table="HR_AttendanceDevices")
public class X_HR_AttendanceDevices extends PO implements I_HR_AttendanceDevices, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20240808L;

    /** Standard Constructor */
    public X_HR_AttendanceDevices (Properties ctx, int HR_AttendanceDevices_ID, String trxName)
    {
      super (ctx, HR_AttendanceDevices_ID, trxName);
      /** if (HR_AttendanceDevices_ID == 0)
        {
			setHR_AttendanceDevices_ID (0);
			setName (null);
        } */
    }

    /** Standard Constructor */
    public X_HR_AttendanceDevices (Properties ctx, int HR_AttendanceDevices_ID, String trxName, String ... virtualColumns)
    {
      super (ctx, HR_AttendanceDevices_ID, trxName, virtualColumns);
      /** if (HR_AttendanceDevices_ID == 0)
        {
			setHR_AttendanceDevices_ID (0);
			setName (null);
        } */
    }

    /** Load Constructor */
    public X_HR_AttendanceDevices (Properties ctx, ResultSet rs, String trxName)
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
      StringBuilder sb = new StringBuilder ("X_HR_AttendanceDevices[")
        .append(get_ID()).append(",Name=").append(getName()).append("]");
      return sb.toString();
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

	/** Set Device Serial Number.
		@param Device_SN Device Serial Number
	*/
	public void setDevice_SN (String Device_SN)
	{
		set_Value (COLUMNNAME_Device_SN, Device_SN);
	}

	/** Get Device Serial Number.
		@return Device Serial Number	  */
	public String getDevice_SN()
	{
		return (String)get_Value(COLUMNNAME_Device_SN);
	}

	/** Set Comment/Help.
		@param Help Comment or Hint
	*/
	public void setHelp (String Help)
	{
		set_Value (COLUMNNAME_Help, Help);
	}

	/** Get Comment/Help.
		@return Comment or Hint
	  */
	public String getHelp()
	{
		return (String)get_Value(COLUMNNAME_Help);
	}

	/** Set Attendance Devices.
		@param HR_AttendanceDevices_ID Attendance Devices
	*/
	public void setHR_AttendanceDevices_ID (int HR_AttendanceDevices_ID)
	{
		if (HR_AttendanceDevices_ID < 1)
			set_ValueNoCheck (COLUMNNAME_HR_AttendanceDevices_ID, null);
		else
			set_ValueNoCheck (COLUMNNAME_HR_AttendanceDevices_ID, Integer.valueOf(HR_AttendanceDevices_ID));
	}

	/** Get Attendance Devices.
		@return Attendance Devices	  */
	public int getHR_AttendanceDevices_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_HR_AttendanceDevices_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set HR_AttendanceDevices_UU.
		@param HR_AttendanceDevices_UU HR_AttendanceDevices_UU
	*/
	public void setHR_AttendanceDevices_UU (String HR_AttendanceDevices_UU)
	{
		set_Value (COLUMNNAME_HR_AttendanceDevices_UU, HR_AttendanceDevices_UU);
	}

	/** Get HR_AttendanceDevices_UU.
		@return HR_AttendanceDevices_UU	  */
	public String getHR_AttendanceDevices_UU()
	{
		return (String)get_Value(COLUMNNAME_HR_AttendanceDevices_UU);
	}

	/** Set Name.
		@param Name Alphanumeric identifier of the entity
	*/
	public void setName (String Name)
	{
		set_Value (COLUMNNAME_Name, Name);
	}

	/** Get Name.
		@return Alphanumeric identifier of the entity
	  */
	public String getName()
	{
		return (String)get_Value(COLUMNNAME_Name);
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