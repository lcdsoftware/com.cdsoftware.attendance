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

/** Generated Model for HR_Attendance
 *  @author iDempiere (generated) 
 *  @version Release 7.1 - $Id$ */
public class X_HR_Attendance extends PO implements I_HR_Attendance, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20200122L;

    /** Standard Constructor */
    public X_HR_Attendance (Properties ctx, int HR_Attendance_ID, String trxName)
    {
      super (ctx, HR_Attendance_ID, trxName);
      /** if (HR_Attendance_ID == 0)
        {
			setHR_Attendance_ID (0);
			setHR_Process_ID (0);
        } */
    }

    /** Load Constructor */
    public X_HR_Attendance (Properties ctx, ResultSet rs, String trxName)
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
      StringBuffer sb = new StringBuffer ("X_HR_Attendance[")
        .append(get_ID()).append("]");
      return sb.toString();
    }

	/** Set Complete Attendance With Default Shift.
		@param CompleteWithShift Complete Attendance With Default Shift	  */
	public void setCompleteWithShift (boolean CompleteWithShift)
	{
		set_Value (COLUMNNAME_CompleteWithShift, Boolean.valueOf(CompleteWithShift));
	}

	/** Get Complete Attendance With Default Shift.
		@return Complete Attendance With Default Shift	  */
	public boolean isCompleteWithShift () 
	{
		Object oo = get_Value(COLUMNNAME_CompleteWithShift);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Date From.
		@param DateFrom 
		Starting date for a range
	  */
	public void setDateFrom (Timestamp DateFrom)
	{
		set_Value (COLUMNNAME_DateFrom, DateFrom);
	}

	/** Get Date From.
		@return Starting date for a range
	  */
	public Timestamp getDateFrom () 
	{
		return (Timestamp)get_Value(COLUMNNAME_DateFrom);
	}

	/** Set Date To.
		@param DateTo 
		End date of a date range
	  */
	public void setDateTo (Timestamp DateTo)
	{
		set_Value (COLUMNNAME_DateTo, DateTo);
	}

	/** Get Date To.
		@return End date of a date range
	  */
	public Timestamp getDateTo () 
	{
		return (Timestamp)get_Value(COLUMNNAME_DateTo);
	}

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

	/** Set HR_Attendance_UU.
		@param HR_Attendance_UU HR_Attendance_UU	  */
	public void setHR_Attendance_UU (String HR_Attendance_UU)
	{
		set_ValueNoCheck (COLUMNNAME_HR_Attendance_UU, HR_Attendance_UU);
	}

	/** Get HR_Attendance_UU.
		@return HR_Attendance_UU	  */
	public String getHR_Attendance_UU () 
	{
		return (String)get_Value(COLUMNNAME_HR_Attendance_UU);
	}

	public org.eevolution.model.I_HR_Process getHR_Process() throws RuntimeException
    {
		return (org.eevolution.model.I_HR_Process)MTable.get(getCtx(), org.eevolution.model.I_HR_Process.Table_Name)
			.getPO(getHR_Process_ID(), get_TrxName());	}

	/** Set Payroll Process.
		@param HR_Process_ID Payroll Process	  */
	public void setHR_Process_ID (int HR_Process_ID)
	{
		if (HR_Process_ID < 1) 
			set_Value (COLUMNNAME_HR_Process_ID, null);
		else 
			set_Value (COLUMNNAME_HR_Process_ID, Integer.valueOf(HR_Process_ID));
	}

	/** Get Payroll Process.
		@return Payroll Process	  */
	public int getHR_Process_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_HR_Process_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Import Attendance.
		@param ImportAttendance Import Attendance	  */
	public void setImportAttendance (String ImportAttendance)
	{
		set_Value (COLUMNNAME_ImportAttendance, ImportAttendance);
	}

	/** Get Import Attendance.
		@return Import Attendance	  */
	public String getImportAttendance () 
	{
		return (String)get_Value(COLUMNNAME_ImportAttendance);
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

	/** Set Process Attendance.
		@param ProcessAttendance Process Attendance	  */
	public void setProcessAttendance (String ProcessAttendance)
	{
		set_Value (COLUMNNAME_ProcessAttendance, ProcessAttendance);
	}

	/** Get Process Attendance.
		@return Process Attendance	  */
	public String getProcessAttendance () 
	{
		return (String)get_Value(COLUMNNAME_ProcessAttendance);
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
}