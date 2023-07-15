package com.enhantec.wms.backend.utils.print;

import com.enhantec.wms.backend.common.base.SKU;
import com.enhantec.wms.backend.common.receiving.Receipt;
import com.enhantec.wms.backend.framework.LegacyBaseService;
import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.framework.ServiceDataMap;
import com.enhantec.wms.backend.utils.audit.Udtrn;
import com.enhantec.wms.backend.utils.common.*;

import java.sql.Connection;
import java.util.HashMap;

public class PrintSNLabelByAsn extends LegacyBaseService
{



	/**
	--注册方法

	 DELETE FROM scprdmst.wmsadmin.sproceduremap WHERE THEPROCNAME = 'PrintSNLabelByAsn';
	 insert into scprdmst.wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION,  ADDWHO,  EDITWHO, PARAMETERS,LOAD,ISBATCH)
	 values ('PrintSNLabelByAsn', 'com.enhantec.sce.utils.print', 'enhantec', 'PrintSNLabelByAsn', 'TRUE',  'john',  'john', 'sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,PRINTER,LPN,ESIGNATUREKEY','0.10','0');


	 */


	private static final long serialVersionUID = 1L;

	public PrintSNLabelByAsn()
	{
	}

	public void execute(ServiceDataHolder serviceDataHolder)
	{
		
		String userid = context.getUserID();



		try
		{


		    String PRINTER= serviceDataHolder.getInputDataAsMap().getString("PRINTER");
		    String LPN= serviceDataHolder.getInputDataAsMap().getString("LPN");
			String ESIGNATUREKEY= serviceDataHolder.getInputDataAsMap().getString( "ESIGNATUREKEY");

			HashMap<String, String> lastReceiptDetailByLPN = Receipt.findLastReceiptDetailByLPN(context, LPN, true);
			HashMap<String, String> sku = SKU.findById(context, lastReceiptDetailByLPN.get("SKU"), true);

//			CodeLookup.getCodeLookupByKey(context,"IDREPRINT",Labels.SN_UI_CD);

			PrintHelper.rePrintSnByLPN(context,LPN, Labels.SN_UI_CD,PRINTER,"1","补打标签");



			Udtrn UDTRN=new Udtrn();
			UDTRN.EsignatureKey=ESIGNATUREKEY;
			UDTRN.FROMTYPE="唯一码打印";
			UDTRN.FROMTABLENAME="IDNOTES";
		    UDTRN.FROMKEY=lastReceiptDetailByLPN.get("LOTTABLE06");
		    UDTRN.FROMKEY1=LPN;
		    UDTRN.FROMKEY2="";
		    UDTRN.FROMKEY3="";
		    UDTRN.TITLE01="收货批次";    UDTRN.CONTENT01=lastReceiptDetailByLPN.get("LOTTABLE06");
		    UDTRN.TITLE02="容器条码";    UDTRN.CONTENT02=LPN;
		    UDTRN.TITLE03="桶号";    UDTRN.CONTENT03=lastReceiptDetailByLPN.get("BARRELNUMBER")+"/"+lastReceiptDetailByLPN.get("TOTALBARRELNUMBER");
		    UDTRN.TITLE04="物料编号";    UDTRN.CONTENT04=lastReceiptDetailByLPN.get("SKU");
		    UDTRN.TITLE05="物料名称";    UDTRN.CONTENT05=sku.get("DESCR");

		    UDTRN.Insert(context, userid);
			



			ServiceDataMap theOutDO = new ServiceDataMap();
			theOutDO.setAttribValue("OK", "1");
			serviceDataHolder.setReturnCode(1);
			serviceDataHolder.setOutputData(theOutDO);


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