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
 * Model class for Attendance Import (I_Attendance).
 * Used as a staging model for importing attendance records from external sources.
 * 
 * @author Casa del Software
 */
public class MIAttendance extends X_I_Attendance{

	private static final long serialVersionUID = 1L;
	
	/**
	 * Standard Constructor
	 * @param ctx context
	 * @param I_Attendance_ID id
	 * @param trxName transaction
	 */
	public MIAttendance(Properties ctx, int I_Attendance_ID, String trxName) {
		super(ctx, I_Attendance_ID, trxName);
	}

	/**
	 * Load Constructor
	 * @param ctx context
	 * @param rs result set
	 * @param trxName transaction
	 */
	public MIAttendance(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
	}

	/**
	 * Placeholder for Business Partner value setter
	 * @param string value
	 */
	public void setBPartnerValue(String string) {
	}

	/**
	 * Placeholder for Attendance Date setter
	 * @param time1 date
	 */
	public void setAttendanceDate(Timestamp time1) {
	}

}
