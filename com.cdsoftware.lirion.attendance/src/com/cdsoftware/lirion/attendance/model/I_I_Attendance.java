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
package com.cdsoftware.lirion.attendance.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import org.compiere.model.*;
import org.compiere.util.KeyNamePair;

/** Generated Interface for I_Attendance
 *  @author iDempiere (generated) 
 *  @version Release 10
 */
@SuppressWarnings("all")
public interface I_I_Attendance 
{

    /** TableName=I_Attendance */
    public static final String Table_Name = "I_Attendance";

    /** AD_Table_ID=1000023 */
    public static final int Table_ID = MTable.getTable_ID(Table_Name);

    KeyNamePair Model = new KeyNamePair(Table_ID, Table_Name);

    /** AccessLevel = 3 - Client - Org 
     */
    BigDecimal accessLevel = BigDecimal.valueOf(3);

    /** Load Meta Data */

    /** Column name AD_Client_ID */
    public static final String COLUMNNAME_AD_Client_ID = "AD_Client_ID";

	/** Get Tenant.
	  * Tenant for this installation.
	  */
	public int getAD_Client_ID();

    /** Column name AD_Org_ID */
    public static final String COLUMNNAME_AD_Org_ID = "AD_Org_ID";

	/** Set Organization.
	  * Organizational entity within tenant
	  */
	public void setAD_Org_ID (int AD_Org_ID);

	/** Get Organization.
	  * Organizational entity within tenant
	  */
	public int getAD_Org_ID();

    /** Column name Attendance_Status */
    public static final String COLUMNNAME_Attendance_Status = "Attendance_Status";

	/** Set Attendance Status	  */
	public void setAttendance_Status (String Attendance_Status);

	/** Get Attendance Status	  */
	public String getAttendance_Status();

    /** Column name Auth_Method */
    public static final String COLUMNNAME_Auth_Method = "Auth_Method";

	/** Set Authentication Method	  */
	public void setAuth_Method (String Auth_Method);

	/** Get Authentication Method	  */
	public String getAuth_Method();

    /** Column name Auth_Result */
    public static final String COLUMNNAME_Auth_Result = "Auth_Result";

	/** Set Authentication Result	  */
	public void setAuth_Result (String Auth_Result);

	/** Get Authentication Result	  */
	public String getAuth_Result();

    /** Column name Card_Reader */
    public static final String COLUMNNAME_Card_Reader = "Card_Reader";

	/** Set Card Reader	  */
	public void setCard_Reader (String Card_Reader);

	/** Get Card Reader	  */
	public String getCard_Reader();

    /** Column name C_BPartner_ID */
    public static final String COLUMNNAME_C_BPartner_ID = "C_BPartner_ID";

	/** Set Business Partner.
	  * Identifies a Business Partner
	  */
	public void setC_BPartner_ID (int C_BPartner_ID);

	/** Get Business Partner.
	  * Identifies a Business Partner
	  */
	public int getC_BPartner_ID();

	public org.compiere.model.I_C_BPartner getC_BPartner() throws RuntimeException;

    /** Column name Created */
    public static final String COLUMNNAME_Created = "Created";

	/** Get Created.
	  * Date this record was created
	  */
	public Timestamp getCreated();

    /** Column name CreatedBy */
    public static final String COLUMNNAME_CreatedBy = "CreatedBy";

	/** Get Created By.
	  * User who created this records
	  */
	public int getCreatedBy();

    /** Column name Date_Stamp */
    public static final String COLUMNNAME_Date_Stamp = "Date_Stamp";

	/** Set Date Stamp	  */
	public void setDate_Stamp (Timestamp Date_Stamp);

	/** Get Date Stamp	  */
	public Timestamp getDate_Stamp();

    /** Column name Department */
    public static final String COLUMNNAME_Department = "Department";

	/** Set Department	  */
	public void setDepartment (String Department);

	/** Get Department	  */
	public String getDepartment();

    /** Column name Device_Name */
    public static final String COLUMNNAME_Device_Name = "Device_Name";

	/** Set Device Name	  */
	public void setDevice_Name (String Device_Name);

	/** Get Device Name	  */
	public String getDevice_Name();

    /** Column name Device_SN */
    public static final String COLUMNNAME_Device_SN = "Device_SN";

	/** Set Device Serial Number	  */
	public void setDevice_SN (String Device_SN);

	/** Get Device Serial Number	  */
	public String getDevice_SN();

    /** Column name Direction */
    public static final String COLUMNNAME_Direction = "Direction";

	/** Set Direction	  */
	public void setDirection (String Direction);

	/** Get Direction	  */
	public String getDirection();

    /** Column name Face_Mask */
    public static final String COLUMNNAME_Face_Mask = "Face_Mask";

	/** Set Face Mask	  */
	public void setFace_Mask (String Face_Mask);

	/** Get Face Mask	  */
	public String getFace_Mask();

    /** Column name Full_Name */
    public static final String COLUMNNAME_Full_Name = "Full_Name";

	/** Set Full Name	  */
	public void setFull_Name (String Full_Name);

	/** Get Full Name	  */
	public String getFull_Name();

    /** Column name HR_ClockCode */
    public static final String COLUMNNAME_HR_ClockCode = "HR_ClockCode";

	/** Set  Code on Dial Clock	  */
	public void setHR_ClockCode (String HR_ClockCode);

	/** Get  Code on Dial Clock	  */
	public String getHR_ClockCode();

    /** Column name I_Attendance_ID */
    public static final String COLUMNNAME_I_Attendance_ID = "I_Attendance_ID";

	/** Set Import Attendance	  */
	public void setI_Attendance_ID (int I_Attendance_ID);

	/** Get Import Attendance	  */
	public int getI_Attendance_ID();

    /** Column name I_Attendance_UU */
    public static final String COLUMNNAME_I_Attendance_UU = "I_Attendance_UU";

	/** Set I_attendance_UU	  */
	public void setI_Attendance_UU (String I_Attendance_UU);

	/** Get I_attendance_UU	  */
	public String getI_Attendance_UU();

    /** Column name ID_Card */
    public static final String COLUMNNAME_ID_Card = "ID_Card";

	/** Set ID Card	  */
	public void setID_Card (String ID_Card);

	/** Get ID Card	  */
	public String getID_Card();

    /** Column name IsActive */
    public static final String COLUMNNAME_IsActive = "IsActive";

	/** Set Active.
	  * The record is active in the system
	  */
	public void setIsActive (boolean IsActive);

	/** Get Active.
	  * The record is active in the system
	  */
	public boolean isActive();

    /** Column name Skin_Temperature */
    public static final String COLUMNNAME_Skin_Temperature = "Skin_Temperature";

	/** Set Skin Temperature	  */
	public void setSkin_Temperature (String Skin_Temperature);

	/** Get Skin Temperature	  */
	public String getSkin_Temperature();

    /** Column name Temperature */
    public static final String COLUMNNAME_Temperature = "Temperature";

	/** Set Temperature	  */
	public void setTemperature (String Temperature);

	/** Get Temperature	  */
	public String getTemperature();

    /** Column name Time_Stamp */
    public static final String COLUMNNAME_Time_Stamp = "Time_Stamp";

	/** Set Time Stamp	  */
	public void setTime_Stamp (Timestamp Time_Stamp);

	/** Get Time Stamp	  */
	public Timestamp getTime_Stamp();

    /** Column name Updated */
    public static final String COLUMNNAME_Updated = "Updated";

	/** Get Updated.
	  * Date this record was updated
	  */
	public Timestamp getUpdated();

    /** Column name UpdatedBy */
    public static final String COLUMNNAME_UpdatedBy = "UpdatedBy";

	/** Get Updated By.
	  * User who updated this records
	  */
	public int getUpdatedBy();
}
