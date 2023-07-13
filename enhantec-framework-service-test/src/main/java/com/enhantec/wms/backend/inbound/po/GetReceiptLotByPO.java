package com.enhantec.wms.backend.inbound.po;

import com.enhantec.wms.backend.utils.common.LegacyDBHelper;
import com.enhantec.wms.backend.framework.LegacyBaseService;
import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.framework.ServiceDataMap;
import com.enhantec.wms.backend.utils.common.FulfillLogicException;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class GetReceiptLotByPO extends LegacyBaseService
{



	/**
	 *  JOHN 20201010按日期 获取采购批次检查记录
	--注册方法
	 DELETE FROM SCPRDMST.wmsadmin.sproceduremap WHERE COMPOSITE='GetReceiptLotByPO';
	 insert into SCPRDMST.wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION,  ADDWHO, EDITWHO, PARAMETERS,LOAD,ISBATCH)
	 values ('GetReceiptLotByPO', 'com.enhantec.sce.inbound.po', 'enhantec', 'GetReceiptLotByPO', 'TRUE', 'JOHN', 'JOHN','sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,ReceiptLot','0.10','0');


		 */


	private static final long serialVersionUID = 1L;

	public GetReceiptLotByPO()
	{
	}

	public void execute(ServiceDataHolder serviceDataHolder)
	{

		String userid = context.getUserID();

		Connection conn  = context.getConnection();

		ServiceDataMap theOutDO = new ServiceDataMap();

		try
		{
		    String ReceiptLot= serviceDataHolder.getInputDataAsMap().getString("ReceiptLot");

			String STORERKEY= LegacyDBHelper.GetValue(context, conn, "select udf1 from codelkup where listname=? and code=?", new String[]{"SYSSET","STORERKEY"}, "");

			ArrayList<LinkedHashMap<String, String>> r1 = LegacyDBHelper.GetRecordMap(context, conn
					, "select FROMKEY,FROMLINENO,STATUS,FROMSKU,FROMSKUDESCR,SKU,FROMLOT,SUPPLIERCODE,MANUFACTURERCODE,PROCESSINGMODE,UOM,QTY,TOTALBARREL,ELOTTABLE11,RETESTDATE,PACKCHECK,FILECHECK,TRANSCHECK,MANUFACTURERDATE,ELOTTABLE07,PACKCOUNTCHECK,abnormalitymesg,abnormality,checkresult,expirydatecheck,supplieritem,qualifiedproducer,POSUPPLIERNAME,ISCOMMONPROJECT,PROJECTCODE from PRERECEIPTCHECK where RECEIPTLOT=? order by serialkey", new String[]{ ReceiptLot});
			if (r1 == null) {
				throw new FulfillLogicException("收货批次(%1)未找到", ReceiptLot);
			}

			for(int i1=0;i1<r1.size();i1++)
			{
				LinkedHashMap<String,String> m1=r1.get(i1);
				if (i1==0)
				{
					String SKUDESCR= LegacyDBHelper.GetValue(context, conn
							, "SELECT DESCR FROM SKU WHERE STORERKEY=? and SKU=?", new String[]{STORERKEY,m1.get("SKU")}, "");
					theOutDO.setAttribValue("STATUS", m1.get("STATUS"));
					theOutDO.setAttribValue("SKU", m1.get("SKU"));
					String SKU_BUSR14= LegacyDBHelper.GetValue(context, conn, "select BUSR14 from SKU where SKU=?", new String[]{m1.get("SKU")}, "");
					theOutDO.setAttribValue("SKU_BUSR14", SKU_BUSR14);
					theOutDO.setAttribValue("SKUDESCR", SKUDESCR);
					theOutDO.setAttribValue("FROMLOT", m1.get("FROMLOT"));
					theOutDO.setAttribValue("SUPPLIERCODE", m1.get("SUPPLIERCODE"));
					theOutDO.setAttribValue("MANUFACTURERCODE", m1.get("MANUFACTURERCODE"));
					theOutDO.setAttribValue("PROCESSINGMODE", m1.get("PROCESSINGMODE"));
					theOutDO.setAttribValue("UOM", m1.get("UOM"));
					theOutDO.setAttribValue("QTY", m1.get("QTY"));
					theOutDO.setAttribValue("TOTALBARREL", m1.get("TOTALBARREL"));
					theOutDO.setAttribValue("ELOTTABLE11", m1.get("ELOTTABLE11"));
					theOutDO.setAttribValue("RETESTDATE", m1.get("RETESTDATE"));
					theOutDO.setAttribValue("PACKCHECK", m1.get("PACKCHECK"));
					theOutDO.setAttribValue("FILECHECK", m1.get("FILECHECK"));
					theOutDO.setAttribValue("TRANSCHECK", m1.get("TRANSCHECK"));
					theOutDO.setAttribValue("PACKCOUNTCHECK", m1.get("PACKCOUNTCHECK"));
					theOutDO.setAttribValue("MANUFACTURERDATE", m1.get("MANUFACTURERDATE"));
					theOutDO.setAttribValue("ELOTTABLE07", m1.get("ELOTTABLE07"));
				//abnormalitymesg,abnormality,checkresult,expirydatecheck,supplieritem,qualifiedproducer,QUALIFIEDSUPPLIER
					theOutDO.setAttribValue("abnormalitymesg", m1.get("abnormalitymesg"));
					theOutDO.setAttribValue("abnormality", m1.get("abnormality"));
					theOutDO.setAttribValue("checkresult", m1.get("checkresult"));
					theOutDO.setAttribValue("expirydatecheck", m1.get("expirydatecheck"));
					theOutDO.setAttribValue("supplieritem", m1.get("supplieritem"));
					theOutDO.setAttribValue("qualifiedproducer", m1.get("qualifiedproducer"));
					theOutDO.setAttribValue("QUALIFIEDSUPPLIER", m1.get("POSUPPLIERNAME"));
					theOutDO.setAttribValue("ISCOMMONPROJECT", m1.get("ISCOMMONPROJECT"));
					theOutDO.setAttribValue("PROJECTCODE", m1.get("PROJECTCODE"));

				}




				LinkedHashMap<String,String> FROMQTY= LegacyDBHelper.GetValueMap(context, conn, "select A.qty-ISNULL(A.receivedqty,0) as QTY,B.supplierNAME,B.SUPPLIER,A.UOM " +
								" FROM WMS_PO_DETAIL A,WMS_PO B WHERE A.POKEY=B.POKEY AND A.POKEY=? AND A.POLINENUMBER=?"
						, new String[]{m1.get("FROMKEY"),m1.get("FROMLINENO")});
				theOutDO.setAttribValue("FROMKEY"+Integer.toString(i1+1), m1.get("FROMKEY")+"-"+m1.get("FROMLINENO"));
				theOutDO.setAttribValue("FROMQTY"+Integer.toString(i1+1), FROMQTY.get("QTY"));
				theOutDO.setAttribValue("POSUPPLYCODE", FROMQTY.get("SUPPLIER"));
				theOutDO.setAttribValue("POSUPPLYNAME", FROMQTY.get("supplierNAME"));
				//theOutDO.setAttribValue("UOM", FROMQTY.get("UOM"));
				//theOutDO.setAttribValue("CONVERSIONRATE", FROMQTY.get("CONVERSIONRATE"));

			}

			try	{	context.releaseConnection(conn); 	}	catch (Exception e1) {		}

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


	}
}