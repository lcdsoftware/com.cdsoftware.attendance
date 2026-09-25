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
import java.util.Properties;

/**
 * Model class for Work Shifts (GH_Shifts).
 * Represents a work shift configuration, including diurnal/nocturnal boundaries
 * and specific work rules.
 * 
 * @author Casa del Software
 */
public class MGH_Shifts extends X_GH_Shifts{

	private static final long serialVersionUID = 1040299876871577919L;

	/**
	 * Standard Constructor
	 * @param ctx context
	 * @param GH_Shifts_ID id
	 * @param trxName transaction
	 */
	public MGH_Shifts(Properties ctx, int GH_Shifts_ID, String trxName) {
		super(ctx, GH_Shifts_ID, trxName);
	}
	
	/**
	 * Load Constructor
	 * @param ctx context
	 * @param rs result set
	 * @param trxName transaction
	 */
	public MGH_Shifts(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
	}

}
