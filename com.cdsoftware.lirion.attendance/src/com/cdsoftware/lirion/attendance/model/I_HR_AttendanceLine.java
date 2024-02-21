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

/** Generated Interface for HR_AttendanceLine
 *  @author iDempiere (generated) 
 *  @version Release 7.1
 */
@SuppressWarnings("all")
public interface I_HR_AttendanceLine 
{

    /** TableName=HR_AttendanceLine */
    public static final String Table_Name = "HR_AttendanceLine";

    /** AD_Table_ID=1000020 */
    public static final int Table_ID = MTable.getTable_ID(Table_Name);

    KeyNamePair Model = new KeyNamePair(Table_ID, Table_Name);

    /** AccessLevel = 3 - Client - Org 
     */
    BigDecimal accessLevel = BigDecimal.valueOf(3);

    /** Load Meta Data */

    /** Column name AD_Client_ID */
    public static final String COLUMNNAME_AD_Client_ID = "AD_Client_ID";

	/** Get Client.
	  * Client/Tenant for this installation.
	  */
	public int getAD_Client_ID();

    /** Column name AD_Org_ID */
    public static final String COLUMNNAME_AD_Org_ID = "AD_Org_ID";

	/** Set Organization.
	  * Organizational entity within client
	  */
	public void setAD_Org_ID (int AD_Org_ID);

	/** Get Organization.
	  * Organizational entity within client
	  */
	public int getAD_Org_ID();

    /** Column name AttendanceDate */
    public static final String COLUMNNAME_AttendanceDate = "AttendanceDate";

	/** Set Attendance Date	  */
	public void setAttendanceDate (Timestamp AttendanceDate);

	/** Get Attendance Date	  */
	public Timestamp getAttendanceDate();

    /** Column name AuthorizationType */
    public static final String COLUMNNAME_AuthorizationType = "AuthorizationType";

	/** Set AuthorizationType	  */
	public void setAuthorizationType (String AuthorizationType);

	/** Get AuthorizationType	  */
	public String getAuthorizationType();

    /** Column name C_BPartner_ID */
    public static final String COLUMNNAME_C_BPartner_ID = "C_BPartner_ID";

	/** Set Business Partner .
	  * Identifies a Business Partner
	  */
	public void setC_BPartner_ID (int C_BPartner_ID);

	/** Get Business Partner .
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

    /** Column name HR_Attendance_ID */
    public static final String COLUMNNAME_HR_Attendance_ID = "HR_Attendance_ID";

	/** Set Attendance	  */
	public void setHR_Attendance_ID (int HR_Attendance_ID);

	/** Get Attendance	  */
	public int getHR_Attendance_ID();

	public I_HR_Attendance getHR_Attendance() throws RuntimeException;

    /** Column name HR_AttendanceLine_ID */
    public static final String COLUMNNAME_HR_AttendanceLine_ID = "HR_AttendanceLine_ID";

	/** Set Attendance Line	  */
	public void setHR_AttendanceLine_ID (int HR_AttendanceLine_ID);

	/** Get Attendance Line	  */
	public int getHR_AttendanceLine_ID();

    /** Column name HR_AttendanceLine_UU */
    public static final String COLUMNNAME_HR_AttendanceLine_UU = "HR_AttendanceLine_UU";

	/** Set HR_AttendanceLine_UU	  */
	public void setHR_AttendanceLine_UU (String HR_AttendanceLine_UU);

	/** Get HR_AttendanceLine_UU	  */
	public String getHR_AttendanceLine_UU();

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

    /** Column name QtyMinutesDifference1 */
    public static final String COLUMNNAME_QtyMinutesDifference1 = "QtyMinutesDifference1";

	/** Set Difference in Minutes Shift 1.
	  * Difference Quantity
	  */
	public void setQtyMinutesDifference1 (BigDecimal QtyMinutesDifference1);

	/** Get Difference in Minutes Shift 1.
	  * Difference Quantity
	  */
	public BigDecimal getQtyMinutesDifference1();

    /** Column name QtyMinutesDifference2 */
    public static final String COLUMNNAME_QtyMinutesDifference2 = "QtyMinutesDifference2";

	/** Set Difference in Minutes Shift 2.
	  * Difference Quantity
	  */
	public void setQtyMinutesDifference2 (BigDecimal QtyMinutesDifference2);

	/** Get Difference in Minutes Shift 2.
	  * Difference Quantity
	  */
	public BigDecimal getQtyMinutesDifference2();

    /** Column name QtyOfHours1 */
    public static final String COLUMNNAME_QtyOfHours1 = "QtyOfHours1";

	/** Set Qty Of Hours 1	  */
	public void setQtyOfHours1 (BigDecimal QtyOfHours1);

	/** Get Qty Of Hours 1	  */
	public BigDecimal getQtyOfHours1();

    /** Column name QtyOfHours2 */
    public static final String COLUMNNAME_QtyOfHours2 = "QtyOfHours2";

	/** Set Qty Of Hours 2	  */
	public void setQtyOfHours2 (BigDecimal QtyOfHours2);

	/** Get Qty Of Hours 2	  */
	public BigDecimal getQtyOfHours2();

    /** Column name Time1 */
    public static final String COLUMNNAME_Time1 = "Time1";

	/** Set Time1	  */
	public void setTime1 (Timestamp Time1);

	/** Get Time1	  */
	public Timestamp getTime1();

    /** Column name Time2 */
    public static final String COLUMNNAME_Time2 = "Time2";

	/** Set Time2	  */
	public void setTime2 (Timestamp Time2);

	/** Get Time2	  */
	public Timestamp getTime2();

    /** Column name Time3 */
    public static final String COLUMNNAME_Time3 = "Time3";

	/** Set Time3	  */
	public void setTime3 (Timestamp Time3);

	/** Get Time3	  */
	public Timestamp getTime3();

    /** Column name Time4 */
    public static final String COLUMNNAME_Time4 = "Time4";

	/** Set Time4	  */
	public void setTime4 (Timestamp Time4);

	/** Get Time4	  */
	public Timestamp getTime4();

    /** Column name TotalQtyOfHours */
    public static final String COLUMNNAME_TotalQtyOfHours = "TotalQtyOfHours";

	/** Set Total Qty Of Hours	  */
	public void setTotalQtyOfHours (BigDecimal TotalQtyOfHours);

	/** Get Total Qty Of Hours	  */
	public BigDecimal getTotalQtyOfHours();

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

    /** Column name WeekDay */
    public static final String COLUMNNAME_WeekDay = "WeekDay";

	/** Set Day of the Week.
	  * Day of the Week
	  */
	public void setWeekDay (String WeekDay);

	/** Get Day of the Week.
	  * Day of the Week
	  */
	public String getWeekDay();
}
