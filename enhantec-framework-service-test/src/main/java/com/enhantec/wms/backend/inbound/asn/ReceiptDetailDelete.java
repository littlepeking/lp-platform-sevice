package com.enhantec.wms.backend.inbound.asn;

import com.enhantec.framework.common.utils.EHContextHelper;
import com.enhantec.wms.backend.utils.common.DBHelper;
import com.enhantec.wms.backend.common.receiving.Receipt;
import com.enhantec.wms.backend.framework.WMSBaseService;
import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.framework.ServiceDataMap;
import com.enhantec.wms.backend.utils.audit.Udtrn;
import com.enhantec.wms.backend.utils.common.FulfillLogicException;
import com.enhantec.wms.backend.utils.print.Labels;
import com.enhantec.wms.backend.utils.print.PrintHelper;

import java.util.Map;

public class ReceiptDetailDelete extends WMSBaseService
{


	/**
	 john
	 --注册方法
	 delete from SCPRDMST.wmsadmin.sproceduremap where THEPROCNAME='EHReceiptDetailDelete';
	 insert into SCPRDMST.wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION,  ADDWHO,  EDITWHO, PARAMETERS,LOAD,ISBATCH)
	 values ('EHReceiptDetailDelete', 'com.enhantec.sce.inbound.asn', 'enhantec', 'ReceiptDetailDelete', 'TRUE',  'john',  'john', 'sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,RECEIPTKEY,RECEIPTLINENUMBER,ESIGNATUREKEY','0.10','0');
	 */

	private static final long serialVersionUID = 1L;


	public void execute(ServiceDataHolder serviceDataHolder) {
		String userid = EHContextHelper.getUser().getUsername();

		try {
			String RECEIPTKEY= serviceDataHolder.getInputDataAsMap().getString("RECEIPTKEY");
			String RECEIPTLINENUMBER= serviceDataHolder.getInputDataAsMap().getString("RECEIPTLINENUMBER");
			String ESIGNATUREKEY= serviceDataHolder.getInputDataAsMap().getString("ESIGNATUREKEY");

			Map<String,String> receiptHashMap = Receipt.findByReceiptKey(RECEIPTKEY,true);

			//if (!receiptHashMap.get("ISCONFIRMED").equals("0"))  throw new Exception("收货单已确认,不允许删除明细行");

			Map<String,String> receiptDetailHashMap = Receipt.findReceiptDetailByLineNumber(RECEIPTKEY,RECEIPTLINENUMBER,true);

			//开始收货的ASN不能删除，否则影响桶号的顺序性。如果极端情况发现SN重复导致收货报错，则关闭当前ASN重新建单收货。考虑性能，暂不增加库存校验在建单环节。
			if (receiptDetailHashMap.get("STATUS").equals("5"))  throw new Exception("收货单行已开始收货,不允许删除");
			if (receiptDetailHashMap.get("STATUS").equals("9"))  throw new Exception("收货单行已收货,不允许删除");
			if (receiptDetailHashMap.get("STATUS").equals("11"))  throw new Exception("收货单行已关闭,不允许删除");

			Receipt.deleteReceiptDetail(RECEIPTKEY,RECEIPTLINENUMBER);

			deletePrintTaskByReceiptDetail(RECEIPTKEY,RECEIPTLINENUMBER);

			Udtrn UDTRN=new Udtrn();
			UDTRN.EsignatureKey=ESIGNATUREKEY;
			UDTRN.FROMTYPE="删除ASN收货明细";
			UDTRN.FROMTABLENAME="RECEIPTDETAIL";
			UDTRN.FROMKEY=RECEIPTKEY;
			UDTRN.FROMKEY1=RECEIPTLINENUMBER;
			UDTRN.FROMKEY2="";
			UDTRN.FROMKEY3="";
			UDTRN.TITLE01="收货单号";    UDTRN.CONTENT01=RECEIPTKEY;
			UDTRN.TITLE02="收货行号";    UDTRN.CONTENT02=RECEIPTLINENUMBER;
			UDTRN.TITLE03="容器条码/箱号";    UDTRN.CONTENT03=receiptDetailHashMap.get("TOID");
			UDTRN.insert( userid);

			String totalLines = DBHelper.getStringValue( "SELECT COUNT(1) FROM RECEIPTDETAIL WHERE RECEIPTKEY=? ",new String[]{RECEIPTKEY},"0");


			ServiceDataMap theOutDO = new ServiceDataMap();
			theOutDO.setAttribValue("TOTALLINES", totalLines);
			serviceDataHolder.setReturnCode(1);
			serviceDataHolder.setOutputData(theOutDO);


		} catch (Exception e) {
			if ( e instanceof FulfillLogicException )
				throw (FulfillLogicException)e;
			else
				throw new FulfillLogicException(e.getMessage());
		}finally {

		}

	}

	private void deletePrintTaskByReceiptDetail( String receiptKey, String receiptLineNumber)throws Exception{
		PrintHelper.removePrintTaskByReceiptDetail( Labels.LPN_UI,receiptKey,receiptLineNumber);
		PrintHelper.removePrintTaskByReceiptDetail( Labels.SN_UI,receiptKey,receiptLineNumber);
	}
}