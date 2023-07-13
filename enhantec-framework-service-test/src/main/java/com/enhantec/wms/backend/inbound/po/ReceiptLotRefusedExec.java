package com.enhantec.wms.backend.inbound.po;

import com.enhantec.wms.backend.utils.common.LegacyDBHelper;
import com.enhantec.wms.backend.framework.LegacyBaseService;
import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.framework.ServiceDataMap;
import com.enhantec.wms.backend.utils.audit.Udtrn;
import com.enhantec.wms.backend.utils.common.FulfillLogicException;

import java.sql.Connection;

public class ReceiptLotRefusedExec  extends LegacyBaseService
{



	/**
	 *  JOHN
	 DELETE FROM SCPRDMST.wmsadmin.sproceduremap WHERE COMPOSITE='ReceiptLotRefusedExec';
	 insert into SCPRDMST.wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION,  ADDWHO, EDITWHO, PARAMETERS,LOAD,ISBATCH)
	 values ('ReceiptLotRefusedExec', 'com.enhantec.sce.inbound.po', 'enhantec', 'ReceiptLotRefusedExec', 'TRUE', 'JOHN',  'JOHN' , 'sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,RECEIPTLOT,ESIGNATUREKEY','0.10','0');
	
		 */
	

	private static final long serialVersionUID = 1L;

	public ReceiptLotRefusedExec()
	{
	}

	public void execute(ServiceDataHolder serviceDataHolder)
	{
		
		String userid = context.getUserID();

		Connection conn = context.getConnection();
		//XtSql r1=null;

		try
		{
		    String RECEIPTLOT= serviceDataHolder.getInputDataAsMap().getString("RECEIPTLOT");
		    String ESIGNATUREKEY= serviceDataHolder.getInputDataAsMap().getString("ESIGNATUREKEY");


			String[] LOTS= LegacyDBHelper.GetValueList(context, conn
					, "select STATUS,PROCESSINGMODE from PRERECEIPTCHECK where RECEIPTLOT=?"
					, new String[]{RECEIPTLOT}, 2);
			if (LOTS==null)
		        throw new FulfillLogicException("收货批次(%1)未找到",RECEIPTLOT);
			if (!LOTS[0].equals("91"))
		        throw new FulfillLogicException("当前状态不支持此操作");


			LegacyDBHelper.ExecSql(context, conn
					, "update PRERECEIPTCHECK set STATUS=?,editwho=?,editdate=?,REFUSEEXECWHO=?,REFUSEEXECDATE=? where RECEIPTLOT=?"
					,  new String[]{"92",userid,"@date",userid,"@date",RECEIPTLOT});
			Udtrn UDTRN=new Udtrn();
			UDTRN.EsignatureKey=ESIGNATUREKEY;
			UDTRN.FROMTYPE="采购收货检查-拒收后续处理完成";
			UDTRN.FROMTABLENAME="PRERECEIPTCHECK";
		    UDTRN.FROMKEY=RECEIPTLOT;
		    UDTRN.FROMKEY1="";
		    UDTRN.FROMKEY2="";
		    UDTRN.FROMKEY3="";
		    UDTRN.TITLE01="收货批次";    UDTRN.CONTENT01=RECEIPTLOT;
		    UDTRN.TITLE02="处理方式";    UDTRN.CONTENT02=LOTS[1];
		    UDTRN.TITLE03="处理方式名称";    UDTRN.CONTENT03= LegacyDBHelper.GetValue(context, conn, "select description from codelkup where listname=? and code=?", new String[]{"PROCEMODE",LOTS[1]}, "") ;
		    UDTRN.Insert(context, conn, userid);			
			
			//String STORERKEY=XtSql.GetValue(context, conn, "select udf1 from codelkup where listname=? and code=?", new String[]{"STASYSSET","STORERKEY"}, "");
			//MailBeanByPreReceipt mail=new MailBeanByPreReceipt();
			//mail.Mail(context,conn,userid,STORERKEY,RECEIPTLOT,"退供应商","收货检查退供应商通知",ESIGNATUREKEY);
			
			//--------------------------------
			try	{	context.releaseConnection(conn); 	}	catch (Exception e1) {		}
			ServiceDataMap theOutDO = new ServiceDataMap();
			theOutDO.setAttribValue("RECEIPTLOT", RECEIPTLOT);
			theOutDO.setAttribValue("STATUS", "92");

			serviceDataHolder.setReturnCode(1);
			serviceDataHolder.setOutputData(theOutDO);

			
		}
		catch (Exception e)
		{
			try
			{
				context.releaseConnection(conn);
			}	catch (Exception e1) {		}
			if ( e instanceof FulfillLogicException )
				throw (FulfillLogicException)e;
			else
		        throw new FulfillLogicException(e.getMessage());
		}
		

		//try	{	r1.Close();			}	catch (Exception e1) {		}

		
	}
}