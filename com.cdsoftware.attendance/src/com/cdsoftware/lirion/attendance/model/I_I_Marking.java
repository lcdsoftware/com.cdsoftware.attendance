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

/** Generated Interface for I_Marking
 *  @author iDempiere (generated) 
 *  @version Release 7.1
 */
@SuppressWarnings("all")
public interface I_I_Marking 
{

    /** TableName=I_Marking */
    public static final String Table_Name = "I_Marking";

    /** AD_Table_ID=1000011 */
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

    /** Column name AD_User_ID */
    public static final String COLUMNNAME_AD_User_ID = "AD_User_ID";

	/** Set User/Contact.
	  * User within the system - Internal or Business Partner Contact
	  */
	public void setAD_User_ID (int AD_User_ID);

	/** Get User/Contact.
	  * User within the system - Internal or Business Partner Contact
	  */
	public int getAD_User_ID();

	public org.compiere.model.I_AD_User getAD_User() throws RuntimeException;

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

    /** Column name I_ErrorMsg */
    public static final String COLUMNNAME_I_ErrorMsg = "I_ErrorMsg";

	/** Set Import Error Message.
	  * Messages generated from import process
	  */
	public void setI_ErrorMsg (String I_ErrorMsg);

	/** Get Import Error Message.
	  * Messages generated from import process
	  */
	public String getI_ErrorMsg();

    /** Column name I_IsImported */
    public static final String COLUMNNAME_I_IsImported = "I_IsImported";

	/** Set Imported.
	  * Has this import been processed
	  */
	public void setI_IsImported (boolean I_IsImported);

	/** Get Imported.
	  * Has this import been processed
	  */
	public boolean isI_IsImported();

    /** Column name I_Marking_ID */
    public static final String COLUMNNAME_I_Marking_ID = "I_Marking_ID";

	/** Set Import Marking	  */
	public void setI_Marking_ID (int I_Marking_ID);

	/** Get Import Marking	  */
	public int getI_Marking_ID();

    /** Column name I_Marking_UU */
    public static final String COLUMNNAME_I_Marking_UU = "I_Marking_UU";

	/** Set I_Marking_UU	  */
	public void setI_Marking_UU (String I_Marking_UU);

	/** Get I_Marking_UU	  */
	public String getI_Marking_UU();

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

    /** Column name MarkingDate */
    public static final String COLUMNNAME_MarkingDate = "MarkingDate";

	/** Set MarkingDate	  */
	public void setMarkingDate (Timestamp MarkingDate);

	/** Get MarkingDate	  */
	public Timestamp getMarkingDate();

    /** Column name Processed */
    public static final String COLUMNNAME_Processed = "Processed";

	/** Set Processed.
	  * The document has been processed
	  */
	public void setProcessed (boolean Processed);

	/** Get Processed.
	  * The document has been processed
	  */
	public boolean isProcessed();

    /** Column name Processing */
    public static final String COLUMNNAME_Processing = "Processing";

	/** Set Process Now	  */
	public void setProcessing (boolean Processing);

	/** Get Process Now	  */
	public boolean isProcessing();

    /** Column name QtyOfHours1 */
    public static final String COLUMNNAME_QtyOfHours1 = "QtyOfHours1";

	/** Set Qty Of Hours 1	  */
	public void setQtyOfHours1 (BigDecimal QtyOfHours1);

	/** Get Qty Of Hours 1	  */
	public BigDecimal getQtyOfHours1();

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

    /** Column name Value */
    public static final String COLUMNNAME_Value = "Value";

	/** Set Search Key.
	  * Search key for the record in the format required - must be unique
	  */
	public void setValue (String Value);

	/** Get Search Key.
	  * Search key for the record in the format required - must be unique
	  */
	public String getValue();

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

    /** Column name WeekDayStr */
    public static final String COLUMNNAME_WeekDayStr = "WeekDayStr";

	/** Set Day of the Week String.
	  * Day of the Week String
	  */
	public void setWeekDayStr (String WeekDayStr);

	/** Get Day of the Week String.
	  * Day of the Week String
	  */
	public String getWeekDayStr();
}
