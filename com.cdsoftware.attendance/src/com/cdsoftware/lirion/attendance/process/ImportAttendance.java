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

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

import org.compiere.model.MBPartner;
import org.compiere.model.Query;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.process.SvrProcess;

import com.cdsoftware.lirion.attendance.model.MGH_ShiftsLine;
import com.cdsoftware.lirion.attendance.model.MMarking;
import com.cdsoftware.lirion.payroll.model.MHRAttribute;
import com.cdsoftware.lirion.payroll.model.MHRConcept;
import com.cdsoftware.lirion.payroll.model.MHRProcess;

/**
 * Process to import attendance markings and calculate late hours as payroll attributes.
 * It compares actual worked hours against shift configurations to determine absences or lateness.
 * 
 * @author Casa del Software
 */
@org.adempiere.base.annotation.Process
public class ImportAttendance extends SvrProcess{
 
	private Timestamp MarkingDateFrom;
	private Timestamp MarkingDateTo;
	private int C_BPartner_ID;
    /**
     * Reads the process parameters required for attendance import.
     * 
     * Parameters:
     * - MarkingDate: Date range for the markings to process.
     * - C_BPartner_ID: Filter by a specific Business Partner.
     */
	@Override
	protected void prepare() {
		ProcessInfoParameter[] parameters = getParameter();
		for (ProcessInfoParameter para: parameters)
		{
			String name = para.getParameterName();
			if (para.getParameter() == null)
				;
			else if (name.equals("MarkingDate")) {
				MarkingDateFrom = para.getParameterAsTimestamp();
				MarkingDateTo = para.getParameter_ToAsTimestamp();
			}else if (name.equals("C_BPartner_ID")) {
				C_BPartner_ID = para.getParameterAsInt();
				
			}
		}
	}

    /**
     * Iterates over attendance markings, calculates late hours by comparing with shifts,
     * and inserts results as payroll attributes.
     * 
     * @return null or status message.
     * @throws Exception if processing fails.
     */
	@Override
	protected String doIt() throws Exception {
		//MBPartner bp = new MBPartner(getCtx(), C_BPartner_ID, get_TrxName());
				
		List<MMarking> listattendance = new Query(getCtx(),MMarking.Table_Name,"MarkingDate between ? and ? AND QtyOfHours1 is not null ",get_TrxName()).setParameters(MarkingDateFrom,MarkingDateTo).setOrderBy("Value").list();
		String value = listattendance.get(0).getValue();
		//boolean valueChange = false;
		BigDecimal lateHour = BigDecimal.ZERO;
		for(MMarking attendance : listattendance) {
			if(attendance.getValue().compareTo(value)!=0) {
				insertLateHour(value,lateHour);
				value = attendance.getValue();
				lateHour=BigDecimal.ZERO;
				
			}
			lateHour= lateHour.add(getDifference(attendance));
			attendance.setI_IsImported(true);
			attendance.saveEx();
			
		}
		insertLateHour(value,lateHour);
		return null;
	}

    /**
     * Calculates the difference between shift hours and actual worked hours,
     * considering a tolerance threshold.
     * 
     * @param attendance The marking record.
     * @return Positive difference representing lateness, or zero.
     */
	protected BigDecimal getDifference(MMarking attendance) {
		String WeekDay = attendance.getWeekDayValue();
		if (WeekDay.equals(null))
			return BigDecimal.ZERO;
		MGH_ShiftsLine shiftline = new Query(getCtx(), MGH_ShiftsLine.Table_Name, "WeekDay=?", get_TrxName()).setParameters(WeekDay).first();
		BigDecimal diference = shiftline.getQtyOfHours1().subtract(attendance.getQtyOfHours1());
		diference = diference.subtract(shiftline.getTolerance());
		return (diference.compareTo(BigDecimal.ZERO)>0)? diference: BigDecimal.ZERO;
	}

    /**
     * Inserts a calculated late hour amount as a payroll attribute (absence quantity).
     * 
     * @param value Business Partner search key (Value).
     * @param lateHour Calculated total late hours.
     */
	protected void insertLateHour(String value, BigDecimal lateHour) {
		MBPartner employed = new Query(getCtx(), MBPartner.Table_Name, "Value=?", get_TrxName()).setParameters(value).first();
		MHRProcess payrollprocess= new MHRProcess(getCtx(), 1000002, get_TrxName());
		MHRAttribute attribute = new MHRAttribute(getCtx(), 0, get_TrxName());
		attribute.setValidFrom(payrollprocess.getFirstDayOfPeriod(payrollprocess.getPayrollPeriod()));
		attribute.setValidTo(payrollprocess.getLastDayOfPeriod(payrollprocess.getPayrollPeriod()));
		attribute.setAmount(lateHour);
		attribute.setHR_Concept_ID(MHRConcept.getByValue(getCtx(), "PA_D_HORAS_AUSENCIA_CANTIDAD",get_TrxName()).getHR_Concept_ID());
		attribute.setC_BPartner_ID(employed.getC_BPartner_ID());
		attribute.setAD_Org_ID(1000000); 
		attribute.saveEx();
	}

}
