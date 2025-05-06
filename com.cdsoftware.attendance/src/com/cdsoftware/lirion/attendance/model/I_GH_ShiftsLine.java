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

/** Generated Interface for GH_ShiftsLine
 *  @author iDempiere (generated) 
 *  @version Release 7.1
 */
@SuppressWarnings("all")
public interface I_GH_ShiftsLine 
{

    /** TableName=GH_ShiftsLine */
    public static final String Table_Name = "GH_ShiftsLine";

    /** AD_Table_ID=1000013 */
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

    /** Column name GH_Shifts_ID */
    public static final String COLUMNNAME_GH_Shifts_ID = "GH_Shifts_ID";

	/** Set Shifts	  */
	public void setGH_Shifts_ID (int GH_Shifts_ID);

	/** Get Shifts	  */
	public int getGH_Shifts_ID();

	public I_GH_Shifts getGH_Shifts() throws RuntimeException;

    /** Column name GH_ShiftsLine_ID */
    public static final String COLUMNNAME_GH_ShiftsLine_ID = "GH_ShiftsLine_ID";

	/** Set Shifts Lines	  */
	public void setGH_ShiftsLine_ID (int GH_ShiftsLine_ID);

	/** Get Shifts Lines	  */
	public int getGH_ShiftsLine_ID();

    /** Column name GH_ShiftsLine_UU */
    public static final String COLUMNNAME_GH_ShiftsLine_UU = "GH_ShiftsLine_UU";

	/** Set GH_ShiftsLine_UU	  */
	public void setGH_ShiftsLine_UU (String GH_ShiftsLine_UU);

	/** Get GH_ShiftsLine_UU	  */
	public String getGH_ShiftsLine_UU();

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

    /** Column name RestDay */
    public static final String COLUMNNAME_RestDay = "RestDay";

	/** Set RestDay	  */
	public void setRestDay (boolean RestDay);

	/** Get RestDay	  */
	public boolean isRestDay();

    /** Column name RestTolerance */
    public static final String COLUMNNAME_RestTolerance = "RestTolerance";

	/** Set RestTolerance	  */
	public void setRestTolerance (BigDecimal RestTolerance);

	/** Get RestTolerance	  */
	public BigDecimal getRestTolerance();

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

    /** Column name Tolerance */
    public static final String COLUMNNAME_Tolerance = "Tolerance";

	/** Set Tolerance	  */
	public void setTolerance (BigDecimal Tolerance);

	/** Get Tolerance	  */
	public BigDecimal getTolerance();

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
