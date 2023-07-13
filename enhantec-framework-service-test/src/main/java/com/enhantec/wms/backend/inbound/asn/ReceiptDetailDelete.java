package com.enhantec.wms.backend.inbound.asn;

import com.enhantec.wms.backend.utils.common.LegacyDBHelper;
import com.enhantec.wms.backend.common.receiving.Receipt;
import com.enhantec.wms.backend.framework.LegacyBaseService;import com.enhantec.wms.backend.framework.Context;import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.framework.ServiceDataMap;
import com.enhantec.wms.backend.utils.audit.Udtrn;
import com.enhantec.wms.backend.utils.common.FulfillLogicException;
import com.enhantec.wms.backend.utils.print.Labels;
import com.enhantec.wms.backend.utils.print.PrintHelper;

import java.sql.Connection;
import java.util.HashMap;

public class ReceiptDetailDelete extends LegacyBaseService
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
		String userid = context.getUserID();

		Connection conn = context.getConnection();

		try {
			String RECEIPTKEY= serviceDataHolder.getInputDataAsMap().getString("RECEIPTKEY");
			String RECEIPTLINENUMBER= serviceDataHolder.getInputDataAsMap().getString("RECEIPTLINENUMBER");
			String ESIGNATUREKEY= serviceDataHolder.getInputDataAsMap().getString("ESIGNATUREKEY");

			HashMap<String,String> receiptHashMap = Receipt.findByReceiptKey(context,conn,RECEIPTKEY,true);

			//if (!receiptHashMap.get("ISCONFIRMED").equals("0"))  throw new Exception("收货单已确认,不允许删除明细行");

			HashMap<String,String> receiptDetailHashMap = Receipt.findReceiptDetailById(context,conn,RECEIPTKEY,RECEIPTLINENUMBER,true);

			//开始收货的ASN不能删除，否则影响桶号的顺序性。如果极端情况发现SN重复导致收货报错，则关闭当前ASN重新建单收货。考虑性能，暂不增加库存校验在建单环节。
			if (receiptDetailHashMap.get("STATUS").equals("5"))  throw new Exception("收货单行已开始收货,不允许删除");
			if (receiptDetailHashMap.get("STATUS").equals("9"))  throw new Exception("收货单行已收货,不允许删除");
			if (receiptDetailHashMap.get("STATUS").equals("11"))  throw new Exception("收货单行已关闭,不允许删除");

			Receipt.deleteReceiptDetail(context,conn,RECEIPTKEY,RECEIPTLINENUMBER);

			deletePrintTaskByReceiptDetail(context,conn,RECEIPTKEY,RECEIPTLINENUMBER);

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
			UDTRN.Insert(context, conn, userid);

			String totalLines = LegacyDBHelper.GetValue(context, conn, "SELECT COUNT(1) FROM RECEIPTDETAIL WHERE RECEIPTKEY=? ",new String[]{RECEIPTKEY},"0");


			ServiceDataMap theOutDO = new ServiceDataMap();
			theOutDO.setAttribValue("TOTALLINES", totalLines);
			serviceDataHolder.setReturnCode(1);
			serviceDataHolder.setOutputData(theOutDO);


		} catch (Exception e) {
			try {
				context.releaseConnection(conn);
			} catch (Exception e1) {
			}
			if ( e instanceof FulfillLogicException )
				throw (FulfillLogicException)e;
			else
				throw new FulfillLogicException(e.getMessage());
		}finally {
			try {
				context.releaseConnection(conn); } catch (Exception e1) {
			}
		}

	}

	private void deletePrintTaskByReceiptDetail(Context context, Connection connection, String receiptKey, String receiptLineNumber)throws Exception{
		PrintHelper.removePrintTaskByReceiptDetail(context,connection, Labels.LPN_UI,receiptKey,receiptLineNumber);
		PrintHelper.removePrintTaskByReceiptDetail(context,connection, Labels.SN_UI,receiptKey,receiptLineNumber);
	}
}