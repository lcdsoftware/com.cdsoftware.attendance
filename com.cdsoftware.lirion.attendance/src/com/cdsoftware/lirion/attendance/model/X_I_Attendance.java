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
 *  @version Release 10 - $Id$ */
@org.adempiere.base.Model(table="I_Attendance")
public class X_I_Attendance extends PO implements I_I_Attendance, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20240808L;

    /** Standard Constructor */
    public X_I_Attendance (Properties ctx, int I_Attendance_ID, String trxName)
    {
      super (ctx, I_Attendance_ID, trxName);
      /** if (I_Attendance_ID == 0)
        {
			setI_Attendance_ID (0);
        } */
    }

    /** Standard Constructor */
    public X_I_Attendance (Properties ctx, int I_Attendance_ID, String trxName, String ... virtualColumns)
    {
      super (ctx, I_Attendance_ID, trxName, virtualColumns);
      /** if (I_Attendance_ID == 0)
        {
			setI_Attendance_ID (0);
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
      StringBuilder sb = new StringBuilder ("X_I_Attendance[")
        .append(get_ID()).append("]");
      return sb.toString();
    }

	/** Set Attendance Status.
		@param Attendance_Status Attendance Status
	*/
	public void setAttendance_Status (String Attendance_Status)
	{
		set_Value (COLUMNNAME_Attendance_Status, Attendance_Status);
	}

	/** Get Attendance Status.
		@return Attendance Status	  */
	public String getAttendance_Status()
	{
		return (String)get_Value(COLUMNNAME_Attendance_Status);
	}

	/** Set Authentication Method.
		@param Auth_Method Authentication Method
	*/
	public void setAuth_Method (String Auth_Method)
	{
		set_Value (COLUMNNAME_Auth_Method, Auth_Method);
	}

	/** Get Authentication Method.
		@return Authentication Method	  */
	public String getAuth_Method()
	{
		return (String)get_Value(COLUMNNAME_Auth_Method);
	}

	/** Set Authentication Result.
		@param Auth_Result Authentication Result
	*/
	public void setAuth_Result (String Auth_Result)
	{
		set_Value (COLUMNNAME_Auth_Result, Auth_Result);
	}

	/** Get Authentication Result.
		@return Authentication Result	  */
	public String getAuth_Result()
	{
		return (String)get_Value(COLUMNNAME_Auth_Result);
	}

	/** Set Card Reader.
		@param Card_Reader Card Reader
	*/
	public void setCard_Reader (String Card_Reader)
	{
		set_Value (COLUMNNAME_Card_Reader, Card_Reader);
	}

	/** Get Card Reader.
		@return Card Reader	  */
	public String getCard_Reader()
	{
		return (String)get_Value(COLUMNNAME_Card_Reader);
	}

	public org.compiere.model.I_C_BPartner getC_BPartner() throws RuntimeException
	{
		return (org.compiere.model.I_C_BPartner)MTable.get(getCtx(), org.compiere.model.I_C_BPartner.Table_ID)
			.getPO(getC_BPartner_ID(), get_TrxName());
	}

	/** Set Business Partner.
		@param C_BPartner_ID Identifies a Business Partner
	*/
	public void setC_BPartner_ID (int C_BPartner_ID)
	{
		if (C_BPartner_ID < 1)
			set_ValueNoCheck (COLUMNNAME_C_BPartner_ID, null);
		else
			set_ValueNoCheck (COLUMNNAME_C_BPartner_ID, Integer.valueOf(C_BPartner_ID));
	}

	/** Get Business Partner.
		@return Identifies a Business Partner
	  */
	public int getC_BPartner_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_BPartner_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Date Stamp.
		@param Date_Stamp Date Stamp
	*/
	public void setDate_Stamp (Timestamp Date_Stamp)
	{
		set_Value (COLUMNNAME_Date_Stamp, Date_Stamp);
	}

	/** Get Date Stamp.
		@return Date Stamp	  */
	public Timestamp getDate_Stamp()
	{
		return (Timestamp)get_Value(COLUMNNAME_Date_Stamp);
	}

	/** Set Department.
		@param Department Department
	*/
	public void setDepartment (String Department)
	{
		set_Value (COLUMNNAME_Department, Department);
	}

	/** Get Department.
		@return Department	  */
	public String getDepartment()
	{
		return (String)get_Value(COLUMNNAME_Department);
	}

	/** Set Device Name.
		@param Device_Name Device Name
	*/
	public void setDevice_Name (String Device_Name)
	{
		set_Value (COLUMNNAME_Device_Name, Device_Name);
	}

	/** Get Device Name.
		@return Device Name	  */
	public String getDevice_Name()
	{
		return (String)get_Value(COLUMNNAME_Device_Name);
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

	/** Set Direction.
		@param Direction Direction
	*/
	public void setDirection (String Direction)
	{
		set_Value (COLUMNNAME_Direction, Direction);
	}

	/** Get Direction.
		@return Direction	  */
	public String getDirection()
	{
		return (String)get_Value(COLUMNNAME_Direction);
	}

	/** Set Face Mask.
		@param Face_Mask Face Mask
	*/
	public void setFace_Mask (String Face_Mask)
	{
		set_Value (COLUMNNAME_Face_Mask, Face_Mask);
	}

	/** Get Face Mask.
		@return Face Mask	  */
	public String getFace_Mask()
	{
		return (String)get_Value(COLUMNNAME_Face_Mask);
	}

	/** Set Full Name.
		@param Full_Name Full Name
	*/
	public void setFull_Name (String Full_Name)
	{
		set_Value (COLUMNNAME_Full_Name, Full_Name);
	}

	/** Get Full Name.
		@return Full Name	  */
	public String getFull_Name()
	{
		return (String)get_Value(COLUMNNAME_Full_Name);
	}

	/** Set  Code on Dial Clock.
		@param HR_ClockCode  Code on Dial Clock
	*/
	public void setHR_ClockCode (String HR_ClockCode)
	{
		set_Value (COLUMNNAME_HR_ClockCode, HR_ClockCode);
	}

	/** Get  Code on Dial Clock.
		@return  Code on Dial Clock	  */
	public String getHR_ClockCode()
	{
		return (String)get_Value(COLUMNNAME_HR_ClockCode);
	}

	/** Set Import Attendance.
		@param I_Attendance_ID Import Attendance
	*/
	public void setI_Attendance_ID (int I_Attendance_ID)
	{
		if (I_Attendance_ID < 1)
			set_ValueNoCheck (COLUMNNAME_I_Attendance_ID, null);
		else
			set_ValueNoCheck (COLUMNNAME_I_Attendance_ID, Integer.valueOf(I_Attendance_ID));
	}

	/** Get Import Attendance.
		@return Import Attendance	  */
	public int getI_Attendance_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_I_Attendance_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set I_attendance_UU.
		@param I_Attendance_UU I_attendance_UU
	*/
	public void setI_Attendance_UU (String I_Attendance_UU)
	{
		set_ValueNoCheck (COLUMNNAME_I_Attendance_UU, I_Attendance_UU);
	}

	/** Get I_attendance_UU.
		@return I_attendance_UU	  */
	public String getI_Attendance_UU()
	{
		return (String)get_Value(COLUMNNAME_I_Attendance_UU);
	}

	/** Set ID Card.
		@param ID_Card ID Card
	*/
	public void setID_Card (String ID_Card)
	{
		set_Value (COLUMNNAME_ID_Card, ID_Card);
	}

	/** Get ID Card.
		@return ID Card	  */
	public String getID_Card()
	{
		return (String)get_Value(COLUMNNAME_ID_Card);
	}

	/** Set Skin Temperature.
		@param Skin_Temperature Skin Temperature
	*/
	public void setSkin_Temperature (String Skin_Temperature)
	{
		set_Value (COLUMNNAME_Skin_Temperature, Skin_Temperature);
	}

	/** Get Skin Temperature.
		@return Skin Temperature	  */
	public String getSkin_Temperature()
	{
		return (String)get_Value(COLUMNNAME_Skin_Temperature);
	}

	/** Set Temperature.
		@param Temperature Temperature
	*/
	public void setTemperature (String Temperature)
	{
		set_Value (COLUMNNAME_Temperature, Temperature);
	}

	/** Get Temperature.
		@return Temperature	  */
	public String getTemperature()
	{
		return (String)get_Value(COLUMNNAME_Temperature);
	}

	/** Set Time Stamp.
		@param Time_Stamp Time Stamp
	*/
	public void setTime_Stamp (Timestamp Time_Stamp)
	{
		set_Value (COLUMNNAME_Time_Stamp, Time_Stamp);
	}

	/** Get Time Stamp.
		@return Time Stamp	  */
	public Timestamp getTime_Stamp()
	{
		return (Timestamp)get_Value(COLUMNNAME_Time_Stamp);
	}
}