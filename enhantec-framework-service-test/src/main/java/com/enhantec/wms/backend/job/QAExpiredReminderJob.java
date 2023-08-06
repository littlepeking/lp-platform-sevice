package com.enhantec.wms.backend.job;

import com.enhantec.framework.common.utils.EHContextHelper;
import com.enhantec.wms.backend.common.base.code.CDSysSet;
import com.enhantec.wms.backend.framework.LegacyBaseService;
import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.utils.common.DBHelper;
import com.enhantec.wms.backend.utils.common.FulfillLogicException;
import com.enhantec.wms.backend.utils.common.UtilHelper;

import java.util.Map;
import java.util.List;

public class QAExpiredReminderJob extends LegacyBaseService {

	/**
	 --注册方法

	 DELETE FROM SCPRDMST.wmsadmin.sproceduremap WHERE THEPROCNAME = 'EHQAExpiredReminderJob';
	 insert into SCPRDMST.wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION,  ADDWHO,  EDITWHO, PARAMETERS,LOAD,ISBATCH)
	 values ('EHQAExpiredReminderJob', 'com.enhantec.sce.job', 'enhantec', 'QAExpiredReminderJob', 'TRUE',  'JOHN',  'JOHN', '','0.10','0');

	 */


	private static final long serialVersionUID = 1L;




	public void execute(ServiceDataHolder serviceDataHolder)
	{

		String userid = EHContextHelper.getUser().getUsername();  //当用户
  //取数据库连接
		try
		{

			DBHelper.executeUpdate(
					"DELETE FROM REMINDER WHERE REMINDERTYPE in (?,?,?)"
					, new Object[]{
							Const.QA_STATUS_AT_EXPIRED_DATE_TYPE,
							Const.QA_STATUS_NEAR_EXPIRED_DATE_TYPE,
							Const.QA_STATUS_BEYOND_EXPIRED_DATE_TYPE
					});

			int remindDays = CDSysSet.getElot05MaxRemindDays();
			String elot05DisplayName = CDSysSet.getElot05DisplayName();
			///////////////////////////////////////

			//查找满足复测期提醒要求的库存批次
			//DATEADD(s, 1, DATEADD(DAY, DATEDIFF(DAY, 0, GETDATE()), 0))     --first second of today
			//DATEADD(s, -1, DATEADD(DAY, DATEDIFF(DAY, 0, GETDATE()) + 1, 0)) --last second of today
			List<Map<String,String>> list =  DBHelper.executeQuery(
					"SELECT DISTINCT la.SKU, la.LOT, la.LOTTABLE06, FORMAT(DATEADD(HH,8,ELOTTABLE05), 'yyyy-MM-dd') ELOTTABLE05,la.ELOTTABLE13 RETESTTIMES," +
							" DATEDIFF(DAY,DATEADD(s, -1, DATEADD(DAY, DATEDIFF(DAY, 0, GETDATE()) + 1, 0)), DATEADD(s, -1, DATEADD(DAY, DATEDIFF(DAY, 0, ELOTTABLE05) + 1, 0))) AS LEFTDAYS " +
							" FROM V_LOTATTRIBUTE la " +
							" WHERE DATEDIFF(DAY,DATEADD(s, -1, DATEADD(DAY, DATEDIFF(DAY, 0, GETDATE()) + 1, 0)),DATEADD(s, -1, DATEADD(DAY, DATEDIFF(DAY, 0, ELOTTABLE05) + 1, 0))) <= ? " +
							" AND EXISTS (SELECT 1 FROM LOTXLOCXID l WHERE l.LOT = la.LOT AND l.STATUS <> 'HOLD' AND l.QTY>0  )",
					new Object[]{remindDays});

			if(list.size()>0) {
				for (Map<String,String> tempLotAttr : list) {

					int retestTimes = Integer.parseInt(tempLotAttr.get("RETESTTIMES"));
					int leftDays = Integer.parseInt(tempLotAttr.get("LEFTDAYS"));

					if(leftDays>0){
						DBHelper.executeUpdate(
								"INSERT INTO REMINDER (REMINDERTYPE,[DATE],MSG) VALUES (?,?,?)"
								, new Object[]{
										Const.QA_STATUS_NEAR_EXPIRED_DATE_TYPE,
								UtilHelper.getCurrentSqlDate(),
										//物料、批号、复验期

												" 物料代码:"+tempLotAttr.get("SKU")+
														" 批号:"+tempLotAttr.get("LOTTABLE06")+
														" 已"+elot05DisplayName+"次数:"+retestTimes+" "
														+elot05DisplayName+"期:"+tempLotAttr.get("ELOTTABLE05")+
														" 距"+elot05DisplayName+"期还剩"+leftDays+"天"
								});

					}else if(leftDays==0){
						DBHelper.executeUpdate(
								"INSERT INTO REMINDER (REMINDERTYPE,[DATE],MSG) VALUES (?,?,?)"
								, new Object[]{
										Const.QA_STATUS_AT_EXPIRED_DATE_TYPE,
										UtilHelper.getCurrentSqlDate(),
										//物料、批号、复验期

												" 物料代码:"+tempLotAttr.get("SKU")+
														" 批号:"+tempLotAttr.get("LOTTABLE06")+
														" 已"+elot05DisplayName+"次数:"+retestTimes+
														" "+elot05DisplayName+"期:"+tempLotAttr.get("ELOTTABLE05")+
														" 今日已到"+elot05DisplayName+"期"
								});

					}else{
						if(retestTimes< CDSysSet.getElot05MaxRemindTimes()) {
							DBHelper.executeUpdate(
									"INSERT INTO REMINDER (REMINDERTYPE,[DATE],MSG) VALUES (?,?,?)"
									, new Object[]{
											Const.QA_STATUS_BEYOND_EXPIRED_DATE_TYPE,
											UtilHelper.getCurrentSqlDate(),
											//物料、批号、复验期

													" 物料代码:" + tempLotAttr.get("SKU") +
															" 批号:" + tempLotAttr.get("LOTTABLE06") +
															" 已"+elot05DisplayName+"次数:" + retestTimes +
															" "+elot05DisplayName+"期:"+tempLotAttr.get("ELOTTABLE05") +
															" 已超"+elot05DisplayName+"期" + (-leftDays) + "天,需检验"
									});
						}else{
							DBHelper.executeUpdate(
									"INSERT INTO REMINDER (REMINDERTYPE,[DATE],MSG) VALUES (?,?,?)"
									, new Object[]{
											Const.QA_STATUS_BEYOND_EXPIRED_DATE_TYPE,
											UtilHelper.getCurrentSqlDate(),
											//物料、批号、复验期

													" 物料代码:" + tempLotAttr.get("SKU") +
															" 批号:" + tempLotAttr.get("LOTTABLE06") +
															" 已"+elot05DisplayName+"次数:" + retestTimes +
															" "+elot05DisplayName+"期:"+tempLotAttr.get("ELOTTABLE05")+
															" 已超"+elot05DisplayName+"期" + (-leftDays) + "天,请复检"
									});
						}
					}


				}

			}

			/////////////////////////////////////////////
			//查找满足有效期提醒要求的库存批次
			//DATEADD(s, 1, DATEADD(DAY, DATEDIFF(DAY, 0, GETDATE()), 0))     --first second of today
			//DATEADD(s, -1, DATEADD(DAY, DATEDIFF(DAY, 0, GETDATE()) + 1, 0)) --last second of today
			list =  DBHelper.executeQuery(
					"SELECT DISTINCT la.SKU, la.LOT, FORMAT(DATEADD(HH,8,ELOTTABLE11), 'yyyy-MM-dd') ELOTTABLE11, la.LOTTABLE06 ," +
							" DATEDIFF(DAY,DATEADD(s, -1, DATEADD(DAY, DATEDIFF(DAY, 0, GETDATE()) + 1, 0)), DATEADD(s, -1, DATEADD(DAY, DATEDIFF(DAY, 0, ELOTTABLE11) + 1, 0))) AS LEFTDAYS " +
							" FROM V_LOTATTRIBUTE la " +
							" WHERE DATEDIFF(DAY,DATEADD(s, -1, DATEADD(DAY, DATEDIFF(DAY, 0, GETDATE()) + 1, 0)),DATEADD(s, -1, DATEADD(DAY, DATEDIFF(DAY, 0, ELOTTABLE11) + 1, 0))) <= ? " +
							" AND EXISTS (SELECT 1 FROM LOTXLOCXID l WHERE l.LOT = la.LOT AND l.STATUS <> 'HOLD' AND l.QTY>0 )",
					new Object[]{remindDays});

			if(list.size()>0) {
				for (Map<String,String> tempLotAttr : list) {

					int leftDays = Integer.parseInt(tempLotAttr.get("LEFTDAYS"));
					if(leftDays>0){
						DBHelper.executeUpdate(
								"INSERT INTO REMINDER (REMINDERTYPE,[DATE],MSG) VALUES (?,?,?)"
								, new Object[]{
										Const.QA_STATUS_NEAR_EXPIRED_DATE_TYPE,
										UtilHelper.getCurrentSqlDate(),
										//物料、批号、有效期

												" 物料代码:"+tempLotAttr.get("SKU")+
														" 批号:"+tempLotAttr.get("LOTTABLE06")+
														" 有效期:"+tempLotAttr.get("ELOTTABLE11")+
														" 距有效期还剩"+leftDays+"天"
								});

					}if(leftDays==0){
						DBHelper.executeUpdate(
								"INSERT INTO REMINDER (REMINDERTYPE,[DATE],MSG) VALUES (?,?,?)"
								, new Object[]{
										Const.QA_STATUS_AT_EXPIRED_DATE_TYPE,
										UtilHelper.getCurrentSqlDate(),
										//物料、批号、有效期

												" 物料代码:"+tempLotAttr.get("SKU")+
														" 批号:"+tempLotAttr.get("LOTTABLE06")+
														" 有效期:"+tempLotAttr.get("ELOTTABLE11")+
														" 今日已到有效期"
								});

					}else{
						DBHelper.executeUpdate(
								"INSERT INTO REMINDER (REMINDERTYPE,[DATE],MSG) VALUES (?,?,?)"
								, new Object[]{
										Const.QA_STATUS_BEYOND_EXPIRED_DATE_TYPE,
										UtilHelper.getCurrentSqlDate(),
										//物料、批号、有效期
												" 物料代码:"+tempLotAttr.get("SKU")+
														" 批号:"+tempLotAttr.get("LOTTABLE06")+
														" 有效期:"+tempLotAttr.get("ELOTTABLE11")+
														" 已超有效期"+(-leftDays)+"天,请移库至不合格区"
								});
					}


				}

			}
		}
		catch (Exception e)
		{//如果出错,先关闭数据库连接,再按系统要求转换成标准的错误类型抛出错误
			if ( e instanceof FulfillLogicException)
				throw (FulfillLogicException)e;
			else
				throw new FulfillLogicException(e.getMessage());
		}finally {
			
		}



	}


}

