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


@org.adempiere.base.annotation.Process
public class ImportAttendance extends SvrProcess{
 
	private Timestamp MarkingDateFrom;
	private Timestamp MarkingDateTo;
	private int C_BPartner_ID;
	@Override
	protected void prepare() {
		// TODO Auto-generated method stub
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

	@Override
	protected String doIt() throws Exception {
		// TODO Auto-generated method stub
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

	protected BigDecimal getDifference(MMarking attendance) {
		// TODO Auto-generated method stub
		String WeekDay = attendance.getWeekDayValue();
		if (WeekDay.equals(null))
			return BigDecimal.ZERO;
		MGH_ShiftsLine shiftline = new Query(getCtx(), MGH_ShiftsLine.Table_Name, "WeekDay=?", get_TrxName()).setParameters(WeekDay).first();
		BigDecimal diference = shiftline.getQtyOfHours1().subtract(attendance.getQtyOfHours1());
		diference = diference.subtract(shiftline.getTolerance());
		return (diference.compareTo(BigDecimal.ZERO)>0)? diference: BigDecimal.ZERO;
	}

	protected void insertLateHour(String value, BigDecimal lateHour) {
		// TODO Auto-generated method stub
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
