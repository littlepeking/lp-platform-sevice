package com.enhantec.wms.backend.inbound.po;

import com.enhantec.wms.backend.utils.common.DBHelper;
import com.enhantec.wms.backend.utils.common.LegacyDBHelper;
import com.enhantec.wms.backend.framework.LegacyBaseService;
import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.framework.ServiceDataMap;
import com.enhantec.wms.backend.utils.audit.Udtrn;
import com.enhantec.wms.backend.utils.common.FulfillLogicException;

import java.sql.Connection;


public class ReceiptLotClose  extends LegacyBaseService
{
	/**
	 *  JOHN 20201010按日期 关闭采购批次检查记录
	 DELETE FROM SCPRDMST.wmsadmin.sproceduremap WHERE COMPOSITE='ReceiptLotClose';
	 insert into SCPRDMST.wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION,  ADDWHO, EDITWHO, PARAMETERS,LOAD,ISBATCH)
	 values ('ReceiptLotClose', 'com.enhantec.sce.inbound.po', 'enhantec', 'ReceiptLotClose', 'TRUE',  'JOHN',  'JOHN', 'sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,RECEIPTLOT,ESIGNATUREKEY','0.10','0');
	
		 */
	

	private static final long serialVersionUID = 1L;

	public ReceiptLotClose()
	{
	}

	public void execute(ServiceDataHolder serviceDataHolder)
	{
		
		String userid = context.getUserID();

	    String RECEIPTLOT= serviceDataHolder.getInputDataAsMap().getString("RECEIPTLOT");
	    String ESIGNATUREKEY= serviceDataHolder.getInputDataAsMap().getString("ESIGNATUREKEY");

		try
		{
			

			String lot = DBHelper.getValue(context, "select STATUS from PRERECEIPTCHECK where RECEIPTLOT=?"
					, new String[]{RECEIPTLOT},String.format("收货批次(%1)未找到", RECEIPTLOT));
			if (!lot.equals("0"))
		        throw new FulfillLogicException("当前状态不支持此操作");
		
			DBHelper.executeUpdate(context, "update PRERECEIPTCHECK set STATUS=?,editwho=?,editdate=? where RECEIPTLOT=?",  new String[]{"99",userid,"@date",RECEIPTLOT});
	
			Udtrn UDTRN=new Udtrn();
				UDTRN.EsignatureKey=ESIGNATUREKEY;
				UDTRN.FROMTYPE="采购收货检查-关闭";
				UDTRN.FROMTABLENAME="PRERECEIPTCHECK";
			    UDTRN.FROMKEY=RECEIPTLOT;
			    UDTRN.FROMKEY1="";
			    UDTRN.FROMKEY2="";
			    UDTRN.FROMKEY3="";
			    UDTRN.TITLE01="收货批次";    UDTRN.CONTENT01=RECEIPTLOT;
			    UDTRN.Insert(context, userid);
			
		
		}
		catch (Exception e)
		{

			if ( e instanceof FulfillLogicException )
				throw (FulfillLogicException)e;
			else
		        throw new FulfillLogicException(e.getMessage());
		}
		

		

		ServiceDataMap theOutDO = new ServiceDataMap();

		theOutDO.setAttribValue("RECEIPTLOT", RECEIPTLOT);
		theOutDO.setAttribValue("STATUS", "99");

		serviceDataHolder.setReturnCode(1);
		serviceDataHolder.setOutputData(theOutDO);
		
	}
}