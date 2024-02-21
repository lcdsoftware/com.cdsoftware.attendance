package com.cdsoftware.lirion.attendance.callout;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.Properties;

import org.compiere.model.GridField;
import org.compiere.model.GridTab;
import org.compiere.util.CLogger;
import org.compiere.util.Env;

import com.cdsoftware.lirion.attendance.base.CustomCallout;


public class SetQtyOfHours extends CustomCallout{
	protected transient CLogger	log = CLogger.getCLogger (getClass());
	
	@Override
	public String start(Properties ctx, int WindowNo, GridTab mTab, GridField mField, Object value, Object oldValue) {
		// TODO Auto-generated method stub
		
		Timestamp time1= (Timestamp)mTab.getValue("Time1");
		Timestamp time2= (Timestamp)mTab.getValue("Time2");
		Timestamp time3= (Timestamp)mTab.getValue("Time3");
		Timestamp time4= (Timestamp)mTab.getValue("Time4");
		BigDecimal QtyOfHours1 = BigDecimal.ZERO;
		BigDecimal QtyOfHours2 = BigDecimal.ZERO;
		
		if (time1!=null && time2!=null) {
			QtyOfHours1 = BigDecimal.valueOf((time2.getTime()-time1.getTime())/(1000*60)).divide(BigDecimal.valueOf(60),2, RoundingMode.HALF_EVEN);
			if(QtyOfHours1.compareTo(Env.ZERO)<0)
				QtyOfHours1 = QtyOfHours1.add(new BigDecimal(24));
			mTab.setValue("QtyOfHours1", QtyOfHours1);
		}

		if (time3!=null && time4!=null) {
			QtyOfHours2 = BigDecimal.valueOf((time4.getTime()-time3.getTime())/(1000*60)).divide(BigDecimal.valueOf(60),2, RoundingMode.HALF_EVEN);
			if(QtyOfHours2.compareTo(Env.ZERO)<0)
				QtyOfHours2 = QtyOfHours2.add(new BigDecimal(24));
			mTab.setValue("QtyOfHours2", QtyOfHours2);
		}
		BigDecimal TotalQtyOfHours = QtyOfHours1.add(QtyOfHours2);
		if(mTab.getField("TotalQtyOfHours") != null)
			mTab.setValue("TotalQtyOfHours", TotalQtyOfHours);
			
		return null;
	}

	@Override
	protected String start() {
		// TODO Auto-generated method stub
		return null;
	}

}
