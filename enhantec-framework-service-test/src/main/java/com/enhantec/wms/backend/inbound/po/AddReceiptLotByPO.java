package com.enhantec.wms.backend.inbound.po;

import com.enhantec.wms.backend.utils.common.DBHelper;
import com.enhantec.wms.backend.utils.common.LegacyDBHelper;
import com.enhantec.wms.backend.framework.LegacyBaseService;
import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.framework.ServiceDataMap;
import com.enhantec.wms.backend.utils.audit.Udtrn;
import com.enhantec.wms.backend.utils.common.FulfillLogicException;

import java.util.List;
import java.util.HashMap;

import static com.enhantec.wms.backend.utils.common.LegecyUtilHelper.Nz;


public class AddReceiptLotByPO extends LegacyBaseService
{



	/**
	 * JOHN 20201010按日期 增加PO在采购批次检查记录
	--注册方法

	 DELETE FROM SCPRDMST.wmsadmin.sproceduremap WHERE COMPOSITE='AddReceiptLotByPO';
	 insert into SCPRDMST.wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION, ADDWHO, EDITWHO, PARAMETERS,LOAD,ISBATCH)
	 values ('AddReceiptLotByPO', 'com.enhantec.sce.inbound.po', 'enhantec', 'AddReceiptLotByPO', 'TRUE', 'JOHN', 'JOHN', 'sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,RECEIPTLOT,POKEY,ESIGNATUREKEY','0.10','0');

	 */
	

	private static final long serialVersionUID = 1L;

	public AddReceiptLotByPO()
	{
	}

	public void execute(ServiceDataHolder serviceDataHolder)
	{
		String userid = context.getUserID();

		

		try
		{

		    String RECEIPTLOT= serviceDataHolder.getInputDataAsMap().getString("RECEIPTLOT");
		    String POKEY= serviceDataHolder.getInputDataAsMap().getString("POKEY");
		    String ESIGNATUREKEY= serviceDataHolder.getInputDataAsMap().getString("ESIGNATUREKEY");
		    
		    int iPoKey=POKEY.indexOf('-');
		    if (iPoKey<=0) throw new Exception("PO号应该带行号,请联系系统管理员");
		    String POLINENUMBER=POKEY.substring(iPoKey+1);
		    POKEY=POKEY.substring(0,iPoKey);

			int Cnt= (int) DBHelper.getRawValue(context, "SELECT COUNT(1) FROM PRERECEIPTCHECK WHERE FROMKEY=? AND FROMLINENO=? AND RECEIPTLOT=?"
					, new Object[]{POKEY,POLINENUMBER,RECEIPTLOT});
			if (Cnt>0)
		        throw new FulfillLogicException("记录已存在,不能重复生成");


			HashMap<String,String> mRec = DBHelper.getRecord(context, "SELECT FROMTYPE,FROMKEY,FROMLINENO,FROMSKU,FROMSKUDESCR,A.SKU,B.ERPLOC,A.FROMLOT" +
							"RECEIPTLOT,MANUFACTURERCODE,A.UOM,A.QTY,A.STATUS,PROCESSINGMODE,SKUSTATUSCHECK,SKUSTATUSINPUT,A.PROJECTCODE,A.ISCOMMONPROJECT,A.ELOTTABLE07,A.ELOTTABLE22,A.ELOTTABLE11," +
							"SKUSTATUSFROM,POSUPPLIERCODE,POSUPPLIERNAME,TOTALBARREL,MANUFACTURERDATE,RETESTDATE,TRANSCHECK,FILECHECK,PACKCHECK,abnormalitymesg,abnormality,checkresult,expirydatecheck,A.supplieritem,A.qualifiedproducer,PACKCOUNTCHECK" +
							" FROM PRERECEIPTCHECK A, WMS_PO_DETAIL B,WMS_PO S WHERE A.FROMKEY = B.POKEY  AND A.FROMLINENO = B.POLINENUMBER AND S.POKEY = B.POKEY " +
							"AND RECEIPTLOT=?", new String[]{ RECEIPTLOT});
			if (mRec.isEmpty()) throw new Exception("未找到批次检查记录");

			HashMap<String,String> mPO = DBHelper.getRecord(context,
					"SELECT A.SUPPLIER,B.ERPLOC FROM WMS_PO A,WMS_PO_DETAIL B WHERE  A.POKEY=B.POKEY AND A.POKEY=? AND A.STATUS<? AND POLINENUMBER=?"
					, new String[]{POKEY, "9",POLINENUMBER});
			if (mPO.isEmpty()) 
		        throw new FulfillLogicException("PO(%1),物料代码(%2)无有效记录",POKEY,mRec.get("FROMSKU"));

			if (!Nz(mRec.get("POSUPPLIERCODE"),"").equals(Nz(mPO.get("SUPPLIER"),"")))  throw new Exception("当前PO与其它PO供应商不一致");
			if (!Nz(mRec.get("ERPLOC"),"").equals(Nz(mPO.get("ERPLOC"),"")))  throw new Exception("当前PO与其它PO分布场所不一致");

			HashMap<String,String> mPRERECEIPTCHECK=new HashMap<String,String>();
			mPRERECEIPTCHECK.put("WHSEID", "@user");
			mPRERECEIPTCHECK.put("addwho", userid);
			mPRERECEIPTCHECK.put("editwho", userid);
			mPRERECEIPTCHECK.put("FROMTYPE", mRec.get("FROMTYPE"));
			mPRERECEIPTCHECK.put("FROMKEY", POKEY);
			mPRERECEIPTCHECK.put("FROMLINENO", POLINENUMBER);
			mPRERECEIPTCHECK.put("FROMSKU", mRec.get("FROMSKU"));
			mPRERECEIPTCHECK.put("FROMSKUDESCR", mRec.get("FROMSKUDESCR"));
			mPRERECEIPTCHECK.put("SKU", mRec.get("SKU"));
			mPRERECEIPTCHECK.put("RECEIPTLOT", mRec.get("RECEIPTLOT"));
			//mPRERECEIPTCHECK.put("SUPPLIERCODE", mRec.get("SUPPLIERCODE"));
			//mPRERECEIPTCHECK.put("MANUFACTURERCODE", mRec.get("MANUFACTURERCODE"));
			mPRERECEIPTCHECK.put("UOM", mRec.get("UOM"));
			mPRERECEIPTCHECK.put("QTY", mRec.get("QTY"));
			mPRERECEIPTCHECK.put("STATUS", mRec.get("STATUS"));
			mPRERECEIPTCHECK.put("PROCESSINGMODE", mRec.get("PROCESSINGMODE"));
			mPRERECEIPTCHECK.put("SKUSTATUSCHECK", mRec.get("SKUSTATUSCHECK"));
			mPRERECEIPTCHECK.put("SKUSTATUSINPUT", mRec.get("SKUSTATUSINPUT"));
			mPRERECEIPTCHECK.put("SKUSTATUSFROM", mRec.get("SKUSTATUSFROM"));
			//mPRERECEIPTCHECK.put("POSUPPLIERCODE", mPO.get("SUPPLIER"));
			//mPRERECEIPTCHECK.put("POSUPPLIERNAME", mPO.get("NAMEALPHA"));
			mPRERECEIPTCHECK.put("TOTALBARREL", mRec.get("TOTALBARREL"));
			mPRERECEIPTCHECK.put("MANUFACTURERDATE", mRec.get("MANUFACTURERDATE"));
			mPRERECEIPTCHECK.put("RETESTDATE", mRec.get("RETESTDATE"));
			mPRERECEIPTCHECK.put("TRANSCHECK", mRec.get("TRANSCHECK"));
			mPRERECEIPTCHECK.put("FILECHECK", mRec.get("FILECHECK"));
			mPRERECEIPTCHECK.put("PACKCHECK", mRec.get("PACKCHECK"));
			//abnormalitymesg,abnormality,checkresult,expirydatecheck,supplieritem,qualifiedproducer,PACKCOUNTCHECK
			mPRERECEIPTCHECK.put("abnormalitymesg", mRec.get("abnormalitymesg"));
			mPRERECEIPTCHECK.put("abnormality", mRec.get("abnormality"));
			mPRERECEIPTCHECK.put("checkresult", mRec.get("checkresult"));
			mPRERECEIPTCHECK.put("expirydatecheck", mRec.get("expirydatecheck"));
			mPRERECEIPTCHECK.put("supplieritem", mRec.get("supplieritem"));
			mPRERECEIPTCHECK.put("qualifiedproducer", mRec.get("qualifiedproducer"));
			mPRERECEIPTCHECK.put("PACKCOUNTCHECK", mRec.get("PACKCOUNTCHECK"));
			mPRERECEIPTCHECK.put("PROJECTCODE", mRec.get("PROJECTCODE"));
			mPRERECEIPTCHECK.put("ISCOMMONPROJECT", mRec.get("ISCOMMONPROJECT"));
			mPRERECEIPTCHECK.put("ELOTTABLE07", mRec.get("ELOTTABLE07"));
			mPRERECEIPTCHECK.put("ELOTTABLE22", mRec.get("ELOTTABLE22"));
			mPRERECEIPTCHECK.put("ELOTTABLE11", mRec.get("ELOTTABLE11"));
			mPRERECEIPTCHECK.put("FROMLOT", mRec.get("FROMLOT"));
			mPRERECEIPTCHECK.put("POSUPPLIERCODE", mRec.get("POSUPPLIERCODE"));

			
			LegacyDBHelper.ExecInsert(context, "PRERECEIPTCHECK", mPRERECEIPTCHECK);
			

			Udtrn UDTRN=new Udtrn();
			UDTRN.EsignatureKey=ESIGNATUREKEY;
			UDTRN.FROMTYPE="采购收货检查-增加PO";
			UDTRN.FROMTABLENAME="PRERECEIPTCHECK";
		    UDTRN.FROMKEY=RECEIPTLOT;
		    UDTRN.FROMKEY1=POKEY;
		    UDTRN.FROMKEY2="";
		    UDTRN.FROMKEY3="";
		    UDTRN.TITLE01="收货批次";    UDTRN.CONTENT01=RECEIPTLOT;
		    UDTRN.TITLE02="采购单号";    UDTRN.CONTENT02=POKEY;
		    UDTRN.TITLE03="物料代码";    UDTRN.CONTENT03=mRec.get("FROMSKU");
		    UDTRN.TITLE04="物料描述";    UDTRN.CONTENT04=mRec.get("FROMSKUDESCR");
		    UDTRN.TITLE05="厂家来源批次";    UDTRN.CONTENT05=mRec.get("FROMLOT");
		    UDTRN.Insert(context, userid);

		    


			ServiceDataMap theOutDO = new ServiceDataMap();

			List<HashMap<String,String>> aChk = DBHelper.executeQuery(context, "select a.FROMKEY,a.FROMLINENO," +
					"b.qty-ISNULL(b.receivedqty,0) as QTY" +
					"  from prereceiptcheck a,WMS_PO_DETAIL b where a.fromkey=b.pokey and a.fromlineno=b.polinenumber " +
					" and a.RECEIPTLOT=? order by a.serialkey", new String[]{RECEIPTLOT});
			for(int i1=0;i1<aChk.size();i1++)
			{
				HashMap<String,String> mChk=aChk.get(i1);
				theOutDO.setAttribValue("FROMKEY"+Integer.toString(i1+1), mChk.get("FROMKEY")+"-"+mChk.get("FROMLINENO"));
				theOutDO.setAttribValue("FROMQTY"+Integer.toString(i1+1), mChk.get("QTY"));
			}

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