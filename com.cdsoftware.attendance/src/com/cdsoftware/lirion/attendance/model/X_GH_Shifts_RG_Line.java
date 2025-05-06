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

/** Generated Model for GH_Shifts_RG_Line
 *  @author iDempiere (generated) 
 *  @version Release 10 - $Id$ */
@org.adempiere.base.Model(table="GH_Shifts_RG_Line")
public class X_GH_Shifts_RG_Line extends PO implements I_GH_Shifts_RG_Line, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20231124L;

    /** Standard Constructor */
    public X_GH_Shifts_RG_Line (Properties ctx, int GH_Shifts_RG_Line_ID, String trxName)
    {
      super (ctx, GH_Shifts_RG_Line_ID, trxName);
      /** if (GH_Shifts_RG_Line_ID == 0)
        {
			setGH_Shifts_RG_Line_ID (0);
        } */
    }

    /** Standard Constructor */
    public X_GH_Shifts_RG_Line (Properties ctx, int GH_Shifts_RG_Line_ID, String trxName, String ... virtualColumns)
    {
      super (ctx, GH_Shifts_RG_Line_ID, trxName, virtualColumns);
      /** if (GH_Shifts_RG_Line_ID == 0)
        {
			setGH_Shifts_RG_Line_ID (0);
        } */
    }

    /** Load Constructor */
    public X_GH_Shifts_RG_Line (Properties ctx, ResultSet rs, String trxName)
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
      StringBuilder sb = new StringBuilder ("X_GH_Shifts_RG_Line[")
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

	public I_GH_Shifts_RG getGH_Shifts_RG() throws RuntimeException
	{
		return (I_GH_Shifts_RG)MTable.get(getCtx(), I_GH_Shifts_RG.Table_ID)
			.getPO(getGH_Shifts_RG_ID(), get_TrxName());
	}

	/** Set Rotation Group.
		@param GH_Shifts_RG_ID Rotation Group
	*/
	public void setGH_Shifts_RG_ID (int GH_Shifts_RG_ID)
	{
		if (GH_Shifts_RG_ID < 1)
			set_ValueNoCheck (COLUMNNAME_GH_Shifts_RG_ID, null);
		else
			set_ValueNoCheck (COLUMNNAME_GH_Shifts_RG_ID, Integer.valueOf(GH_Shifts_RG_ID));
	}

	/** Get Rotation Group.
		@return Rotation Group	  */
	public int getGH_Shifts_RG_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_GH_Shifts_RG_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Line Rotation Group.
		@param GH_Shifts_RG_Line_ID Line Rotation Group
	*/
	public void setGH_Shifts_RG_Line_ID (int GH_Shifts_RG_Line_ID)
	{
		if (GH_Shifts_RG_Line_ID < 1)
			set_ValueNoCheck (COLUMNNAME_GH_Shifts_RG_Line_ID, null);
		else
			set_ValueNoCheck (COLUMNNAME_GH_Shifts_RG_Line_ID, Integer.valueOf(GH_Shifts_RG_Line_ID));
	}

	/** Get Line Rotation Group.
		@return Line Rotation Group	  */
	public int getGH_Shifts_RG_Line_ID()
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_GH_Shifts_RG_Line_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set GH_Shifts_RG_Line_UU.
		@param GH_Shifts_RG_Line_UU GH_Shifts_RG_Line_UU
	*/
	public void setGH_Shifts_RG_Line_UU (String GH_Shifts_RG_Line_UU)
	{
		set_Value (COLUMNNAME_GH_Shifts_RG_Line_UU, GH_Shifts_RG_Line_UU);
	}

	/** Get GH_Shifts_RG_Line_UU.
		@return GH_Shifts_RG_Line_UU	  */
	public String getGH_Shifts_RG_Line_UU()
	{
		return (String)get_Value(COLUMNNAME_GH_Shifts_RG_Line_UU);
	}
}