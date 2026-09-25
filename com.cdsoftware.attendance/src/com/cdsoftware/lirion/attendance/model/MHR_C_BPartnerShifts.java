/**********************************************************************
 * This file is part of iDempiere ERP Open Source                      *
 * http://www.idempiere.org                                            *
 *                                                                     *
 * Copyright (C) Contributors                                          *
 *                                                                     *
 * This program is free software; you can redistribute it and/or       *
 * modify it under the terms of the GNU General Public License         *
 * as published by the Free Software Foundation; either version 2      *
 * of the License, or (at your option) any later version.              *
 *                                                                     *
 * This program is distributed in the hope that it will be useful,     *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of      *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the        *
 * GNU General Public License for more details.                        *
 *                                                                     *
 * You should have received a copy of the GNU General Public License   *
 * along with this program; if not, write to the Free Software         *
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,          *
 * MA 02110-1301, USA.                                                 *
 *                                                                     *
 * Contributors:                                                       *
 * - Casa del Software                                                 *
 **********************************************************************/
package com.cdsoftware.lirion.attendance.model;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Properties;

/**
 * Model class for Business Partner Work Shifts (HR_C_BPartnerShifts).
 * Links employees to specific work shifts for a given date range.
 * 
 * @author Casa del Software
 */
public class MHR_C_BPartnerShifts extends X_HR_C_BPartnerShifts{

	private static final long serialVersionUID = 1L;

	/**
	 * Standard Constructor
	 * @param ctx context
	 * @param HR_C_BPartnerShifts_ID id
	 * @param trxName transaction
	 */
	public MHR_C_BPartnerShifts(Properties ctx, int HR_C_BPartnerShifts_ID, String trxName) {
		super(ctx, HR_C_BPartnerShifts_ID, trxName);
	}

	/**
	 * Constructor with Virtual Columns
	 * @param ctx context
	 * @param HR_C_BPartnerShifts_ID id
	 * @param trxName transaction
	 * @param virtualColumns virtual columns
	 */
	public MHR_C_BPartnerShifts(Properties ctx, int HR_C_BPartnerShifts_ID, String trxName, String[] virtualColumns) {
		super(ctx, HR_C_BPartnerShifts_ID, trxName, virtualColumns);
	}

	/**
	 * Load Constructor
	 * @param ctx context
	 * @param rs result set
	 * @param trxName transaction
	 */
	public MHR_C_BPartnerShifts(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
	}

	/**
	 * Placeholder for date from setter
	 * @param p_DateFrom start date
	 */
	public void setdatefrom(Timestamp p_DateFrom) {
	}

	/**
	 * Placeholder for date to setter
	 * @param p_DateTo end date
	 */
	public void setdateto(Timestamp p_DateTo) {
	}

	/**
	 * Gets the start date of the shift assignment.
	 * @return start date
	 */
	public Timestamp getDateFrom() {
	    return get_ValueAsTimestamp("DateFrom");
	}

	/**
	 * Internal helper to retrieve timestamp values.
	 * @param string column name
	 * @return timestamp or null
	 */
	private Timestamp get_ValueAsTimestamp(String string) {
		return (Timestamp)get_Value(string);
	}

	/**
	 * Gets the end date of the shift assignment.
	 * @return end date
	 */
	public Timestamp getDateTo() {
	    return get_ValueAsTimestamp("DateTo");
	}

}
