package com.enhantec.wms.backend.qa;

import com.enhantec.wms.backend.framework.WMSBaseService;
import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.framework.ServiceDataMap;
import com.enhantec.wms.backend.utils.audit.AuditService;
import com.enhantec.wms.backend.utils.audit.Udtrn;
import com.enhantec.wms.backend.utils.common.*;

import com.enhantec.framework.common.utils.EHContextHelper;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Map;

public class PostRelease extends WMSBaseService {

	/**
	--注册方法  放行单提交

	 DELETE FROM wmsadmin.sproceduremap WHERE THEPROCNAME = 'EHPostRelease';
	insert into wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION,  ADDWHO,  EDITWHO, PARAMETERS,LOAD,ISBATCH)
	values ('EHPostRelease', 'com.enhantec.sce.qa', 'enhantec', 'PostRelease', 'TRUE',  'JOHN',  'JOHN', 'sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,ESIGNATUREKEY,RELEASEKEY','0.10','0');
	
	 */


	private static final long serialVersionUID = 1L;
	


	public void execute(ServiceDataHolder serviceDataHolder)
	{
		String userid = EHContextHelper.getUser().getUsername();  //当用户
  //取数据库连接
		try
		{


			String RELEASEKEY= serviceDataHolder.getInputDataAsMap().getString("RELEASEKEY");   //传入参数-放行单号
//			String USERNAME=processData.getInputDataMap().getString("USERNAME");
//			String PASSWORD=processData.getInputDataMap().getString("PASSWORD");
//			String REASON=processData.getInputDataMap().getString("REASON");
//			String NOTES=processData.getInputDataMap().getString("NOTES");

			String ESIGNATUREKEY= serviceDataHolder.getInputDataAsMap().getString("ESIGNATUREKEY"); //传入参数-签名记录KEY
		    
		    //取放行表内容
		    String SqlRelease="SELECT STORERKEY,SKU, STATUS, LOTTABLE06, QUALITYSTATUS,FROMQUALITYSTATUS," +
					" FROMLOTTABLE05,FROMLOTTABLE11,ELOTTABLE02,LOTTABLE05, LOTTABLE11,INITIALRELEASE, PROCESSINGMODE,MANUFACTURERNAME" +
					" , RELEASETYPE, NOTES,ELOTTABLE01,ELOTTABLE19,ELOTTABLE20,ELOTTABLE09,ELOTTABLE07,ELOTTABLE04"
		    +" FROM RELEASE WHERE RELEASEKEY=?";
		    Map<String,Object> mRelease= DBHelper.getRawRecord( SqlRelease, new Object[]{RELEASEKEY},"放行单");
		    if (mRelease.isEmpty()) throw new Exception("系统中无此放行单");
		    if (!mRelease.get("STATUS").equals("0")) throw new Exception("放行单已做后续处理,不能继续操作");  //检查放行表是否已处理

			String eLottable05 = UtilHelper.getString(mRelease.get("ELOTTABLE05"));//复测期，停止发运期
			String eLottable11 = UtilHelper.getString(mRelease.get("ELOTTABLE11"));//有效期
			if(!UtilHelper.isEmpty(eLottable05) && !UtilHelper.isEmpty(eLottable11) && eLottable05.compareTo(eLottable11) > 0){
				throw new Exception("复测期不能大于有效期");
			}

		    String LOTTABLE06= UtilHelper.getString(mRelease.get("LOTTABLE06"));
			String FROMQUALITYSTATUS=UtilHelper.getString(mRelease.get("FROMQUALITYSTATUS"));
			String QUALITYSTATUS=UtilHelper.getString(mRelease.get("QUALITYSTATUS"));
			String ELOTTABLE02=UtilHelper.getString(mRelease.get("ELOTTABLE02"));
			String ELOTTABLE01=UtilHelper.getString(mRelease.get("ELOTTABLE01"));
			String ELOTTABLE19=UtilHelper.getString(mRelease.get("ELOTTABLE19"));
			String ELOTTABLE20=UtilHelper.getString(mRelease.get("ELOTTABLE20"));
			String ELOTTABLE07=UtilHelper.getString(mRelease.get("ELOTTABLE07"));
			String ELOTTABLE09=UtilHelper.getString(mRelease.get("ELOTTABLE09"));
			String ELOTTABLE04=UtilHelper.getString(mRelease.get("ELOTTABLE04"));




			if (LegecyUtilHelper.isNull(QUALITYSTATUS)) throw new Exception("质量状态不允许为空");   //检查判定质量状态
			Timestamp FROMELOTTABLE05=(Timestamp)mRelease.get("FROMLOTTABLE05");
			Timestamp FROMELOTTABLE11=(Timestamp)mRelease.get("FROMLOTTABLE11");
			Timestamp ELOTTABLE05=(Timestamp)mRelease.get("LOTTABLE05");
			Timestamp ELOTTABLE11=(Timestamp)mRelease.get("LOTTABLE11");
			int ELOTTABLE13=Integer.valueOf(mRelease.get("INITIALRELEASE").toString());

			//call Audit service to authenticate
			//写入日志
			Udtrn udtrn=new Udtrn();
			udtrn.EsignatureKey=ESIGNATUREKEY;
			udtrn.FROMTYPE="QA放行";
			udtrn.FROMTABLENAME="RELEASE";
			udtrn.FROMKEY=LOTTABLE06; //批号
			udtrn.FROMKEY1=RELEASEKEY;
			udtrn.FROMKEY2="";
			udtrn.FROMKEY3="";
			udtrn.TITLE01="收货批次";    udtrn.CONTENT01=LOTTABLE06;
			udtrn.TITLE02="放行单号";    udtrn.CONTENT02=RELEASEKEY;
			udtrn.TITLE03="质量状态";    udtrn.CONTENT03=QUALITYSTATUS;
			udtrn.TITLE04="保税状态";    udtrn.CONTENT04=ELOTTABLE02;
			udtrn.TITLE05="账册号";    udtrn.CONTENT05=ELOTTABLE01;
			udtrn.TITLE06="偏差号";    udtrn.CONTENT06=ELOTTABLE19;
			udtrn.TITLE07="变更号";    udtrn.CONTENT07=ELOTTABLE20;
			udtrn.TITLE08="生产批号";    udtrn.CONTENT08=ELOTTABLE07;
			udtrn.TITLE09="物料批号/供应商批号";    udtrn.CONTENT09=ELOTTABLE09;
			AuditService.doAudit(udtrn);

		    //取批次当前状态
//			Map<String,Object> laRecord = LotAttribute.findByLottable06(LOTTABLE06,true);
//
//		    String preQUALITYSTATUS=getString(laRecord.get("LOTTABLE03"));  //批次当前的质量状态
//
//		    if (preQUALITYSTATUS.equals("NA"))   //不允许放行为无质量状态
//		    	throw new Exception("不允许放行质量状态为N/A的批次");
//			if (preQUALITYSTATUS.equals(QUALITYSTATUS))
//				throw new Exception("当前库存中的质量状态和放行质量状态不能相同");

		    //更新放行表的状态为已处理
		    DBHelper.executeUpdate( "UPDATE RELEASE SET EDITWHO=?,EDITDATE=?,RELEASEWHO=?,RELEASEDATE=?,STATUS=? WHERE RELEASEKEY=?"
				   , new String[]{userid, LocalDateTime.now().toString(),userid,LocalDateTime.now().toString(),"1",RELEASEKEY});

		    //更新库存批次对应的质量状态

		    DBHelper.executeUpdate( "UPDATE ENTERPRISE.ELOTATTRIBUTE SET EDITWHO=?, EDITDATE=?," +
							" ELOTTABLE02=? , ELOTTABLE03=? , ELOTTABLE05=? , ELOTTABLE11=?, ELOTTABLE13=ELOTTABLE13+?" +
							",ELOTTABLE01=?,ELOTTABLE19=?,ELOTTABLE20=?,ELOTTABLE09=?,ELOTTABLE07=?,ELOTTABLE04=? WHERE ELOT=? "
					   , new Object[]{userid, UtilHelper.getCurrentSqlDate(),ELOTTABLE02,QUALITYSTATUS, UtilHelper.convertTimestampToSqlDate(ELOTTABLE05),
						UtilHelper.convertTimestampToSqlDate(ELOTTABLE11), ELOTTABLE13,
							ELOTTABLE01,ELOTTABLE19,
							ELOTTABLE20,
							ELOTTABLE09,ELOTTABLE07
							,ELOTTABLE04
							,LOTTABLE06
		    			});
		    /*//elottable21 记录首次放行质量状态
			String elotTable21=DBHelper.getValue("select ELOTTABLE21 from ENTERPRISE.ELOTATTRIBUTE where ELOT=?",new Object[]{
					LOTTABLE06)
			},"批属性");
			if (UtilHelper.isEmpty(elotTable21)){
				DBHelper.executeUpdate( "UPDATE ENTERPRISE.ELOTATTRIBUTE SET EDITWHO=?, EDITDATE=?,ELOTTABLE21=? WHERE ELOT=? "
						, new Object[]{userid),UtilHelper.getCurrentSqlDate(),QUALITYSTATUS),LOTTABLE06)});
			}*/

//		    //更新标签表对应的质量状态
//			DBHelper.executeUpdate( "UPDATE IDNOTES SET EDITWHO=?, EDITDATE=?, ELOTTABLE03=? , ELOTTABLE05=? , ELOTTABLE11=? WHERE LOTTABLE06=? "
//					, new Object[]{userid),UtilHelper.getCurrentSqlDate(),QUALITYSTATUS), convert2DateTimeNullable(ELOTTABLE05),convert2DateTimeNullable(ELOTTABLE11), LOTTABLE06)});



			//当质量状态不为NA时,复测期和有效期必填
//		    if (!QUALITYSTATUS.equals("NA"))
//		    {
//			    if ((XtUtils.isNull(mRelease.get("RETESTDAYS")))&&(XtUtils.isNull(mRelease.get("PERIODDAYS"))))
//			    {
//			    	throw new Exception("复测期及有效期不能同时为空");
//			    }
//		    }
		    
//		    if (!XtUtils.isNull(mRelease.get("RETESTDAYS")))  //RETESTDAYS	NVARCHAR2 (10)	N	复测天数（对应LOTTABLE05)
//		    { //如果填写复测日期(文本字段),对复测日期进行处理
//		    	int i1=XtSql.GetCount( "select count(1) from dual where to_date('"+s2+"','MM/dd/yyyy')>sysdate", new String[] {});
//		    	if (i1==0) throw new Exception("复测日期必须在今天之后");
//		    }
//
//		    if (!XtUtils.isNull(mRelease.get("PERIODDAYS")))  //RETESTDAYS	NVARCHAR2 (10)	N	复测天数（对应LOTTABLE05)
//		    {//如果填写有效日期(文本字段),对复测日期进行处理
//		    	int i1=XtSql.GetCount( "select count(1) from dual where to_date('"+s2+"','MM/dd/yyyy')>sysdate", new String[] {});
//		    	if (i1==0) throw new Exception("有效日期必须在今天之后");
//		    }

			//创建反馈对象
			ServiceDataMap theOutDO = new ServiceDataMap();
			theOutDO.setAttribValue("RELEASEKEY", RELEASEKEY);
			serviceDataHolder.setReturnCode(1);
			serviceDataHolder.setOutputData(theOutDO);

		}
		catch (Exception e)
		{
			if ( e instanceof FulfillLogicException )
				throw (FulfillLogicException)e;
			else
		        throw new FulfillLogicException(e.getMessage());
		}finally {
			
		}


		
	}
	

}

