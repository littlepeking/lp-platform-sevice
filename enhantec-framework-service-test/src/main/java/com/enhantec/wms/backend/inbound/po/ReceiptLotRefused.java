package com.enhantec.wms.backend.inbound.po;

import com.enhantec.wms.backend.utils.common.DBHelper;
import com.enhantec.wms.backend.framework.*;
import com.enhantec.wms.backend.utils.audit.Udtrn;
import com.enhantec.wms.backend.framework.WMSBaseService;
import com.enhantec.wms.backend.utils.common.FulfillLogicException;
import com.enhantec.wms.backend.utils.common.LegecyUtilHelper;

import com.enhantec.framework.common.utils.EHContextHelper;

import java.time.LocalDateTime;


public class ReceiptLotRefused  extends WMSBaseService
{



	/**
	 *  JOHN
	 DELETE FROM SCPRDMST.wmsadmin.sproceduremap WHERE COMPOSITE='ReceiptLotRefused';
	 insert into SCPRDMST.wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION,  ADDWHO, EDITWHO, PARAMETERS,LOAD,ISBATCH)
	 values ('ReceiptLotRefused', 'com.enhantec.sce.inbound.po', 'enhantec', 'ReceiptLotRefused', 'TRUE', 'JOHN',  'JOHN' , 'sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,RECEIPTLOT,ESIGNATUREKEY','0.10','0');
	
		 */
	

	private static final long serialVersionUID = 1L;

	public ReceiptLotRefused()
	{
	}

	public void execute(ServiceDataHolder serviceDataHolder)
	{
		
		String userid = EHContextHelper.getUser().getUsername();



		try
		{

		    String RECEIPTLOT= serviceDataHolder.getInputDataAsMap().getString("RECEIPTLOT");
		    String ESIGNATUREKEY= serviceDataHolder.getInputDataAsMap().getString("ESIGNATUREKEY");

			String[] LOTS = (String[]) DBHelper.getValueList( "select STATUS from PRERECEIPTCHECK where RECEIPTLOT=?"
					, new String[]{RECEIPTLOT}).toArray();
			if (LOTS==null)
		        throw new FulfillLogicException("收货批次(%1)未找到",RECEIPTLOT);
			if (!LOTS[0].equals("0"))
		        throw new FulfillLogicException("当前状态不支持此操作");
			/*
			0	未检查
			1	通过
			2	无需检查

			 */
			//boolean canRefused=false;
//			if ((!FILECHECK.equals("2"))&&(!FILECHECK.equals("1"))) canRefused=true;
//			if ((!SUPPLIERCHECK.equals("2"))&&(!SUPPLIERCHECK.equals("1"))) canRefused=true;
//			if ((!PACKCHECK.equals("2"))&&(!PACKCHECK.equals("1"))) canRefused=true;
//			if ((!WEIGHTCHECK.equals("2"))&&(!WEIGHTCHECK.equals("1"))) canRefused=true;
			//if (!canRefused) throw new Exception("检查全都通过,不允许拒收");
			DBHelper.executeUpdate( "update PRERECEIPTCHECK set STATUS=?,editwho=?,editdate=? where RECEIPTLOT=?",  new String[]{"9",userid, LocalDateTime.now().toString(),RECEIPTLOT});

			
			String RECEIPTKEY= DBHelper.getValue( "SELECT RECEIPTKEY FROM RECEIPT WHERE STATUS=? AND EXTERNRECEIPTKEY=? AND TYPE=?", new String[]{"0",RECEIPTLOT,"101"}, "");
			if (!LegecyUtilHelper.isNull(RECEIPTKEY))
			{//关闭收货单
				DBHelper.executeUpdate("update RECEIPT set status=?,editwho=?,editdate=? where RECEIPTKEY=?"
						,new String[]{"20",userid,LocalDateTime.now().toString(),RECEIPTKEY});
			}
			
			Udtrn UDTRN=new Udtrn();
			UDTRN.EsignatureKey=ESIGNATUREKEY;
			UDTRN.FROMTYPE="采购收货检查-拒收";
			UDTRN.FROMTABLENAME="PRERECEIPTCHECK";
		    UDTRN.FROMKEY=RECEIPTLOT;
		    UDTRN.FROMKEY1="";
		    UDTRN.FROMKEY2="";
		    UDTRN.FROMKEY3="";
		    UDTRN.TITLE01="收货批次";    UDTRN.CONTENT01=RECEIPTLOT;
		    UDTRN.Insert( userid);
			
			//String STORERKEY=XtSql.GetValue( "select udf1 from codelkup where listname=? and code=?", new String[]{"STASYSSET","STORERKEY"}, "");
			//MailBeanByPreReceipt mail=new MailBeanByPreReceipt();
			//mail.Mail(userid,STORERKEY,RECEIPTLOT,"拒收","收货检查拒收通知",ESIGNATUREKEY);
			
			//------------------------------------------------------


			ServiceDataMap theOutDO = new ServiceDataMap();
			theOutDO.setAttribValue("RECEIPTLOT", RECEIPTLOT);
			theOutDO.setAttribValue("STATUS", "9");

			serviceDataHolder.setReturnCode(1);
			serviceDataHolder.setOutputData(theOutDO);

		}
		catch (Exception e)
		{

			if ( e instanceof FulfillLogicException )
				throw (FulfillLogicException)e;
			else
		        throw new FulfillLogicException(e.getMessage());
		}
		
	}
}