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
package com.cdsoftware.lirion.attendance.process;

import java.util.List;
import org.compiere.model.MRefList;
import org.compiere.model.Query;
import org.compiere.process.ProcessInfoParameter;

import com.cdsoftware.lirion.attendance.base.CustomProcess;
import com.cdsoftware.lirion.attendance.model.MGH_Shifts;
import com.cdsoftware.lirion.attendance.model.MGH_ShiftsLine;
import com.cdsoftware.lirion.attendance.model.MMarking;

/**
 * Server process to complete attendance records that have missing clock-out times.
 * It identifies markings (MMarking) with null time2 and attempts to reconcile 
 * them with shift configurations (MGH_ShiftsLine) to estimate lost time or 
 * validate the marking period.
 * 
 * @author Casa del Software
 */
@org.adempiere.base.annotation.Process
public class CompleteAttendance extends CustomProcess{

	private int p_GH_Shifts_ID;

	/**
	 * Reads the process parameters required for attendance completion.
	 * 
	 * Parameters:
	 * - Description: Used to pass the GH_Shifts_ID representing 
	 *   the shift configuration to use for clock-out estimation.
	 */
	@Override
	protected void prepare() {
		ProcessInfoParameter[] parameters = getParameter();
		for (ProcessInfoParameter para: parameters)
		{
			String name = para.getParameterName();
			if (para.getParameter() == null)
				;
			else if (name.equals("Description"))
				p_GH_Shifts_ID = para.getParameterAsInt();
			
		}
		
	}

	/**
	 * Executes the completion logic for markings with null clock-out times.
	 * It iterates through all incomplete markings (where time2 is null) and 
	 * attempts to calculate the missing time based on the provided shift.
	 * 
	 * @return null.
	 * @throws Exception if an error occurs during record processing.
	 */
	@Override
	protected String doIt() throws Exception {
		List<MMarking> listattendance = new Query(getCtx(), MMarking.Table_Name, "time2 is null", get_TrxName()).list();
		for(MMarking attendance : listattendance) {
			
			getLostTime(p_GH_Shifts_ID,attendance);			
		}
		
		return null;
	}

	/**
	 * Attempts to find lost time by comparing marking times with shift line times.
	 * 
	 * @param p_GH_Shifts_ID2 shift ID
	 * @param attendance marking record
	 */
	protected void getLostTime(int p_GH_Shifts_ID2, MMarking attendance) {
		MGH_Shifts shift = new MGH_Shifts(getCtx(), p_GH_Shifts_ID2, get_TrxName());
		String WeekDay = getWeekDayValue(attendance.getWeekDayStr());
		if (WeekDay.equals(null))
			return;
		
		MGH_ShiftsLine shiftsLine = new Query(getCtx(), MGH_ShiftsLine.Table_Name, "GH_Shift_ID=? AND WeekDay=?", get_TrxName()).setParameters(p_GH_Shifts_ID2,WeekDay).first();
		long DifTime1 = attendance.getTime1().getTime() - shiftsLine.getTime1().getTime();
		long DifTime2 = attendance.getTime1().getTime() - shiftsLine.getTime2().getTime();
		long DifTime3 = attendance.getTime1().getTime() - shiftsLine.getTime3().getTime();
		long DifTime4 = attendance.getTime1().getTime() - shiftsLine.getTime4().getTime();
		int min = 0;
		if(DifTime1<DifTime2)
			min = 1;
		else 
			min =2;
		
		if(DifTime2<DifTime3)
			min = 1;
		else
			min =2;
		
	}

	/**
	 * Resolves the week day value from a string name using the AD reference 167.
	 * 
	 * @param WeekDayStr day name (e.g. "Monday")
	 * @return corresponding reference value
	 */
	protected String getWeekDayValue(String WeekDayStr) {
		
		List<MRefList> reflist = new Query(getCtx(), MRefList.Table_Name, "AD_Reference_ID=?",get_TrxName()).setParameters(167).list();
		
		for(MRefList ref : reflist) {
			if(ref.getName().compareToIgnoreCase(WeekDayStr)==0) 
				return ref.getValue();
			
		}
		return null;
		
	}

}
