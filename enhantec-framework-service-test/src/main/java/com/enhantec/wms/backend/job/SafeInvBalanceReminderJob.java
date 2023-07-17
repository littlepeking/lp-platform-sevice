package com.enhantec.wms.backend.job;

import com.enhantec.framework.common.utils.EHContextHelper;
import com.enhantec.wms.backend.framework.LegacyBaseService;
import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.utils.common.DBHelper;
import com.enhantec.wms.backend.utils.common.FulfillLogicException;
import com.enhantec.wms.backend.utils.common.UtilHelper;

import com.enhantec.framework.common.utils.EHContextHelper;
import java.util.HashMap;
import java.util.List;

public class SafeInvBalanceReminderJob extends LegacyBaseService {

	/**
	 --注册方法

	 DELETE FROM SCPRDMST.wmsadmin.sproceduremap WHERE THEPROCNAME = 'EHSafeInvBalanceReminderJob';
	 insert into SCPRDMST.wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION,  ADDWHO,  EDITWHO, PARAMETERS,LOAD,ISBATCH)
	 values ('EHSafeInvBalanceReminderJob', 'com.enhantec.sce.job', 'enhantec', 'SafeInvBalanceReminderJob', 'TRUE',  'JOHN',  'JOHN', '','0.10','0');

	 */


	private static final long serialVersionUID = 1L;

	public void execute(ServiceDataHolder serviceDataHolder)
	{
		String userid = EHContextHelper.getUser().getUsername();  //当用户
  //取数据库连接
		try
		{
			DBHelper.executeUpdate(
					"DELETE FROM REMINDER WHERE REMINDERTYPE = ? "
					, new Object[]{
							Const.SKU_SAFE_INV_BALANCE_TYPE
					});
			List<HashMap<String,String>> list =  DBHelper.executeQuery(
					"select s.AVAIL_QTY ,s2.TOTALAVAILABLE,s.SKU from SKU s ,( select sum((QTY-QTYPICKED-QTYALLOCATED-QTYONHOLD)) as TOTALAVAILABLE,l.SKU from LOT l ,V_LOTATTRIBUTE vl" +
							" where l.LOT=vl.LOT  GROUP BY l.SKU ) s2 " +
							" where s2.sku = s.SKU  and s.AVAIL_QTY >(s2.TOTALAVAILABLE - s.AVAIL_QTY)  ",
					new Object[]{});

			if(list.size()>0) {
				for (HashMap<String,String> temp : list) {

					DBHelper.executeUpdate(
							"INSERT INTO REMINDER (REMINDERTYPE,[DATE],MSG) VALUES (?,?,?)"
							, new Object[]{
									Const.SKU_SAFE_INV_BALANCE_TYPE,
									UtilHelper.getCurrentSqlDate(),

											" 物料代码:"+temp.get("SKU")+
													" 安全库存:"+ UtilHelper.getString(temp.get("AVAIL_QTY"))+
													" 可用库存:"+UtilHelper.getString(temp.get("TOTALAVAILABLE"))

							});

				}

			}

		}
		catch (Exception e)
		{
			if ( e instanceof FulfillLogicException)
				throw (FulfillLogicException)e;
			else
				throw new FulfillLogicException(e.getMessage());
		}finally {
		}



	}


}

