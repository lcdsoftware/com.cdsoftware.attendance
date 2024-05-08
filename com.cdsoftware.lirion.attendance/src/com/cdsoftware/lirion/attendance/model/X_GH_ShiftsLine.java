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

/** Generated Model for GH_ShiftsLine
 *  @author iDempiere (generated) 
 *  @version Release 7.1 - $Id$ */
@org.adempiere.base.Model(table="GH_ShiftsLine")
public class X_GH_ShiftsLine extends PO implements I_GH_ShiftsLine, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20200122L;

    /** Standard Constructor */
    public X_GH_ShiftsLine (Properties ctx, int GH_ShiftsLine_ID, String trxName)
    {
      super (ctx, GH_ShiftsLine_ID, trxName);
      /** if (GH_ShiftsLine_ID == 0)
        {
			setGH_ShiftsLine_ID (0);
        } */
    }

    /** Load Constructor */
    public X_GH_ShiftsLine (Properties ctx, ResultSet rs, String trxName)
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
      StringBuffer sb = new StringBuffer ("X_GH_ShiftsLine[")
        .append(get_ID()).append("]");
      return sb.toString();
    }

	public I_GH_Shifts getGH_Shifts() throws RuntimeException
    {
		return (I_GH_Shifts)MTable.get(getCtx(), I_GH_Shifts.Table_Name)
			.getPO(getGH_Shifts_ID(), get_TrxName());	}

	/** Set Shifts.
		@param GH_Shifts_ID Shifts	  */
	public void setGH_Shifts_ID (int GH_Shifts_ID)
	{
		if (GH_Shifts_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_GH_Shifts_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_GH_Shifts_ID, Integer.valueOf(GH_Shifts_ID));
	}

	/** Get Shifts.
		@return Shifts	  */
	public int getGH_Shifts_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_GH_Shifts_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Shifts Lines.
		@param GH_ShiftsLine_ID Shifts Lines	  */
	public void setGH_ShiftsLine_ID (int GH_ShiftsLine_ID)
	{
		if (GH_ShiftsLine_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_GH_ShiftsLine_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_GH_ShiftsLine_ID, Integer.valueOf(GH_ShiftsLine_ID));
	}

	/** Get Shifts Lines.
		@return Shifts Lines	  */
	public int getGH_ShiftsLine_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_GH_ShiftsLine_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set GH_ShiftsLine_UU.
		@param GH_ShiftsLine_UU GH_ShiftsLine_UU	  */
	public void setGH_ShiftsLine_UU (String GH_ShiftsLine_UU)
	{
		set_ValueNoCheck (COLUMNNAME_GH_ShiftsLine_UU, GH_ShiftsLine_UU);
	}

	/** Get GH_ShiftsLine_UU.
		@return GH_ShiftsLine_UU	  */
	public String getGH_ShiftsLine_UU () 
	{
		return (String)get_Value(COLUMNNAME_GH_ShiftsLine_UU);
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

	/** Set Qty Of Hours 2.
		@param QtyOfHours2 Qty Of Hours 2	  */
	public void setQtyOfHours2 (BigDecimal QtyOfHours2)
	{
		set_Value (COLUMNNAME_QtyOfHours2, QtyOfHours2);
	}

	/** Get Qty Of Hours 2.
		@return Qty Of Hours 2	  */
	public BigDecimal getQtyOfHours2 () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_QtyOfHours2);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	/** Set RestDay.
		@param RestDay RestDay	  */
	public void setRestDay (boolean RestDay)
	{
		set_Value (COLUMNNAME_RestDay, Boolean.valueOf(RestDay));
	}

	/** Get RestDay.
		@return RestDay	  */
	public boolean isRestDay () 
	{
		Object oo = get_Value(COLUMNNAME_RestDay);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set RestTolerance.
		@param RestTolerance RestTolerance	  */
	public void setRestTolerance (BigDecimal RestTolerance)
	{
		set_Value (COLUMNNAME_RestTolerance, RestTolerance);
	}

	/** Get RestTolerance.
		@return RestTolerance	  */
	public BigDecimal getRestTolerance () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_RestTolerance);
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

	/** Set Time3.
		@param Time3 Time3	  */
	public void setTime3 (Timestamp Time3)
	{
		set_Value (COLUMNNAME_Time3, Time3);
	}

	/** Get Time3.
		@return Time3	  */
	public Timestamp getTime3 () 
	{
		return (Timestamp)get_Value(COLUMNNAME_Time3);
	}

	/** Set Time4.
		@param Time4 Time4	  */
	public void setTime4 (Timestamp Time4)
	{
		set_Value (COLUMNNAME_Time4, Time4);
	}

	/** Get Time4.
		@return Time4	  */
	public Timestamp getTime4 () 
	{
		return (Timestamp)get_Value(COLUMNNAME_Time4);
	}

	/** Set Tolerance.
		@param Tolerance Tolerance	  */
	public void setTolerance (BigDecimal Tolerance)
	{
		set_Value (COLUMNNAME_Tolerance, Tolerance);
	}

	/** Get Tolerance.
		@return Tolerance	  */
	public BigDecimal getTolerance () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_Tolerance);
		if (bd == null)
			 return Env.ZERO;
		return bd;
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
}