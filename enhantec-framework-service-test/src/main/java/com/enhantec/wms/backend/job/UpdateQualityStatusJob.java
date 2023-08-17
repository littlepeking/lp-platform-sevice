package com.enhantec.wms.backend.job;

import com.enhantec.framework.common.utils.EHContextHelper;
import com.enhantec.wms.backend.common.base.code.CDSysSet;
import com.enhantec.wms.backend.framework.WMSBaseService;
import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.utils.audit.Udtrn;
import com.enhantec.wms.backend.utils.common.DBHelper;
import com.enhantec.wms.backend.utils.common.FulfillLogicException;
import com.enhantec.wms.backend.utils.common.UtilHelper;

import java.util.Map;
import java.util.List;

public class UpdateQualityStatusJob extends WMSBaseService {

	/**
	 --注册方法

	 DELETE FROM SCPRDMST.wmsadmin.sproceduremap WHERE THEPROCNAME = 'EHQualityStatusJob';
	 insert into SCPRDMST.wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION,  ADDWHO,  EDITWHO, PARAMETERS,LOAD,ISBATCH)
	 values ('EHQualityStatusJob', 'com.enhantec.sce.job', 'enhantec', 'UpdateQualityStatusJob', 'TRUE',  'JOHN',  'JOHN', '','0.10','0');

	 */


	private static final long serialVersionUID = 1L;



	public void execute(ServiceDataHolder serviceDataHolder)
	{
		String userid = EHContextHelper.getUser().getUsername();  //当用户
  //取数据库连接
		try
		{
			//查询过复测期的库存批次质量状态
			//SELECT DATEADD(s, 1, DATEADD(DAY, DATEDIFF(DAY, 0, GETDATE()), 0))     --first second of today
			List<Map<String,String>> list =  DBHelper.executeQuery( "SELECT STORERKEY, SKU, ELOT, ELOTTABLE13 FROM enterprise.ELOTATTRIBUTE WHERE (ELOTTABLE03 = 'RELEASE' OR ELOTTABLE03 = 'CONDIREL') AND ELOTTABLE05 < DATEADD(s, 1, DATEADD(DAY, DATEDIFF(DAY, 0, GETDATE()), 0)) "
					, new Object[]{});

			//更新过期库存批次对应的质量状态为待检或停止发运
			if(list.size()>0) {
				for (Map<String,String> tempLotAttr : list) {

					String newQAStatus  = CDSysSet.getElot05ExpiredQualityStatus();

//					if(tempLotAttr.get("ELOTTABLE13").equals("0")||
//						tempLotAttr.get("ELOTTABLE13").equals("1")) newQAStatus = "QUARANTINE";
					DBHelper.executeUpdate( "UPDATE enterprise.ELOTATTRIBUTE SET EDITWHO=?, EDITDATE=?, ELOTTABLE03=? WHERE ELOT = ? "
							, new Object[]{userid, UtilHelper.getCurrentSqlDate(), newQAStatus, tempLotAttr.get("ELOT")});

//					//更新标签表对应的质量状态
//					DBHelper.executeUpdate( "UPDATE IDNOTES SET EDITWHO=?, EDITDATE=?, ELOTTABLE03=? WHERE ELOT=? "
//							, new Object[]{userid),UtilHelper.getCurrentSqlDate(),newQAStatus), tempLotAttr.get("LOTTABLE06"))});
//


					Udtrn UDTRN = new Udtrn();
					UDTRN.FROMTYPE = "质量状态过期";
					UDTRN.FROMTABLENAME = "ELOTATTRIBUTE";
					UDTRN.FROMKEY = tempLotAttr.get("SKU");
					UDTRN.FROMKEY1 = tempLotAttr.get("ELOT");
					UDTRN.FROMKEY2 = "";
					UDTRN.FROMKEY3 = "";
					UDTRN.TITLE01 = "物料代码";
					UDTRN.CONTENT01 = tempLotAttr.get("SKU");
					UDTRN.TITLE02 = "收货批次";
					UDTRN.CONTENT02 = tempLotAttr.get("ELOT");
					UDTRN.Insert( userid);
				}

			}


			//查询过有效期的库存批次质量状态
			//SELECT DATEADD(s, 1, DATEADD(DAY, DATEDIFF(DAY, 0, GETDATE()), 0))     --first second of today
			list =  DBHelper.executeQuery( "SELECT SKU, ELOT, ELOTTABLE13 FROM  enterprise.ELOTATTRIBUTE WHERE (ELOTTABLE03 = 'RELEASE' OR ELOTTABLE03 = 'CONDIREL' OR ELOTTABLE03 = 'QUARANTINE') AND  ELOTTABLE11 < DATEADD(s, 1, DATEADD(DAY, DATEDIFF(DAY, 7, GETDATE()), 0)) "
					, new Object[]{});

			//更新过期库存批次对应的质量状态为不合格
			if(list.size()>0) {
				for (Map<String,String> tempLotAttr : list) {
					//String newQAStatus  = "REJECT";
					String newQAStatus  = CDSysSet.getElot11ExpiredQualityStatus();

					DBHelper.executeUpdate( "UPDATE ENTERPRISE.ELOTATTRIBUTE SET EDITWHO=?, EDITDATE=?, ELOTTABLE03=? WHERE ELOT = ? "
							, new Object[]{userid, UtilHelper.getCurrentSqlDate(), newQAStatus, tempLotAttr.get("ELOT")});
//
//					//更新标签表对应的质量状态
//					DBHelper.executeUpdate( "UPDATE IDNOTES SET EDITWHO=?, EDITDATE=?, ELOTTABLE03=? WHERE ELOT=? "
//							, new Object[]{userid),UtilHelper.getCurrentSqlDate(),newQAStatus), tempLotAttr.get("LOTTABLE06"))});


					Udtrn UDTRN = new Udtrn();
					UDTRN.FROMTYPE = "质量状态过期";
					UDTRN.FROMTABLENAME = "ELOTATTRIBUTE";
					UDTRN.FROMKEY = tempLotAttr.get("SKU");
					UDTRN.FROMKEY1 = tempLotAttr.get("ELOT");
					UDTRN.FROMKEY2 = "";
					UDTRN.FROMKEY3 = "";
					UDTRN.TITLE01 = "物料代码";


					UDTRN.CONTENT01 = tempLotAttr.get("SKU");
					UDTRN.TITLE02 = "收货批次";
					UDTRN.CONTENT02 = tempLotAttr.get("ELOT");
					UDTRN.Insert( userid);

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

