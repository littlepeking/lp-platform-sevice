package com.enhantec.wms.backend.inbound.po;

import com.enhantec.wms.backend.framework.WMSBaseService;
import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.framework.ServiceDataMap;
import com.enhantec.wms.backend.utils.audit.Udtrn;
import com.enhantec.wms.backend.utils.common.DBHelper;
import com.enhantec.wms.backend.utils.common.FulfillLogicException;

import com.enhantec.framework.common.utils.EHContextHelper;

import java.time.LocalDateTime;

public class ReceiptLotRefusedMode  extends WMSBaseService
{



	/**
	 * JOHN
	--注册方法
	 DELETE FROM SCPRDMST.wmsadmin.sproceduremap WHERE COMPOSITE='ReceiptLotRefusedMode';
	 insert into SCPRDMST.wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION,  ADDWHO, EDITWHO, PARAMETERS,LOAD,ISBATCH)
	 values ('ReceiptLotRefusedMode', 'com.enhantec.sce.inbound.po', 'enhantec', 'ReceiptLotRefusedMode', 'TRUE',  'JOHN',  'JOHN' , 'sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,RECEIPTLOT,PROCESSINGMODE','0.10','0');
	
		 */
	

	private static final long serialVersionUID = 1L;

	public ReceiptLotRefusedMode()
	{
	}

	public void execute(ServiceDataHolder serviceDataHolder)
	{
		String userid = EHContextHelper.getUser().getUsername();

	    String RECEIPTLOT= serviceDataHolder.getInputDataAsMap().getString("RECEIPTLOT");
	    String PROCESSINGMODE= serviceDataHolder.getInputDataAsMap().getString("PROCESSINGMODE");
	    String ESIGNATUREKEY= serviceDataHolder.getInputDataAsMap().getString("ESIGNATUREKEY");
	    

		try
		{

			String[] LOTS=(String[]) DBHelper.getValueList( "select STATUS,PROCESSINGMODE from PRERECEIPTCHECK where RECEIPTLOT=?"
					, new String[]{RECEIPTLOT}, String.format("收货批次(%1)未找到",RECEIPTLOT)).toArray();
			if ((!LOTS[0].equals("9"))&&(!LOTS[0].equals("91")))
		        throw new FulfillLogicException("当前状态不支持此操作");


			DBHelper.executeUpdate( "update PRERECEIPTCHECK set STATUS=?,PROCESSINGMODE=?,editwho=?,editdate=? where RECEIPTLOT=?",  new String[]{"91",PROCESSINGMODE,userid, LocalDateTime.now().toString(),RECEIPTLOT});
			Udtrn UDTRN=new Udtrn();
			UDTRN.EsignatureKey=ESIGNATUREKEY;
			UDTRN.FROMTYPE="采购收货检查-拒收后续处理方式维护";
			UDTRN.FROMTABLENAME="PRERECEIPTCHECK";
		    UDTRN.FROMKEY=RECEIPTLOT;
		    UDTRN.FROMKEY1="";
		    UDTRN.FROMKEY2="";
		    UDTRN.FROMKEY3="";
		    UDTRN.TITLE01="收货批次";    UDTRN.CONTENT01=RECEIPTLOT;
		    UDTRN.TITLE02="原状态";    UDTRN.CONTENT02=LOTS[0];
		    UDTRN.TITLE03="新状态";    UDTRN.CONTENT03="91";
		    UDTRN.TITLE04="原处理方式";    UDTRN.CONTENT04=LOTS[1];
		    UDTRN.TITLE05="新处理方式";    UDTRN.CONTENT05=PROCESSINGMODE;
		    UDTRN.Insert( userid);		
		
		}
		catch (Exception e)
		{
			if ( e instanceof FulfillLogicException )
				throw (FulfillLogicException)e;
			else
		        throw new FulfillLogicException(e.getMessage());
		}
		

		//try	{	r1.Close();			}	catch (Exception e1) {		}
		

		ServiceDataMap theOutDO = new ServiceDataMap();
		theOutDO.setAttribValue("RECEIPTLOT", RECEIPTLOT);
		theOutDO.setAttribValue("STATUS", "91");

		serviceDataHolder.setReturnCode(1);
		serviceDataHolder.setOutputData(theOutDO);

		
	}
}