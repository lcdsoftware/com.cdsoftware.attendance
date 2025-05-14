package com.cdsoftware.lirion.attendance.process;

import java.util.List;
import org.compiere.model.MRefList;
import org.compiere.model.Query;
import org.compiere.process.ProcessInfoParameter;

import com.cdsoftware.lirion.attendance.base.CustomProcess;
import com.cdsoftware.lirion.attendance.model.MGH_Shifts;
import com.cdsoftware.lirion.attendance.model.MGH_ShiftsLine;
import com.cdsoftware.lirion.attendance.model.MMarking;

@org.adempiere.base.annotation.Process
public class CompleteAttendance extends CustomProcess{

	private int p_GH_Shifts_ID;
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

	@Override
	protected String doIt() throws Exception {
		// TODO Auto-generated method stub
		List<MMarking> listattendance = new Query(getCtx(), MMarking.Table_Name, "time2 is null", get_TrxName()).list();
		for(MMarking attendance : listattendance) {
			
			getLostTime(p_GH_Shifts_ID,attendance);			
		}
		
		return null;
	}

	protected void getLostTime(int p_GH_Shifts_ID2, MMarking attendance) {
		// TODO Auto-generated method stub
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

	protected String getWeekDayValue(String WeekDayStr) {
		
		List<MRefList> reflist = new Query(getCtx(), MRefList.Table_Name, "AD_Reference_ID=?",get_TrxName()).setParameters(167).list();
		
		for(MRefList ref : reflist) {
			if(ref.getName().compareToIgnoreCase(WeekDayStr)==0) 
				return ref.getValue();
			
		}
		return null;
		
	}

}
