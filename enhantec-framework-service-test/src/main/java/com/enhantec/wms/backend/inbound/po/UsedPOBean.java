package com.enhantec.wms.backend.inbound.po;

import com.enhantec.wms.backend.utils.common.DBHelper;
import com.enhantec.wms.backend.utils.common.LegacyDBHelper;
import com.enhantec.wms.backend.inbound.asn.utils.ReceiptUtilHelper;
import com.enhantec.framework.common.utils.EHContextHelper;

import java.math.BigDecimal;
import com.enhantec.framework.common.utils.EHContextHelper;
import java.util.Map;
import java.util.HashMap;

public class UsedPOBean {

	public String POKEY=null;
	public String POLINENUMBER=null;
	public String SKU=null;
	//public String SKUDESCR=null;
	public String UOM=null;  
	public BigDecimal QTY=null; 
	public BigDecimal RECEIVEDQTY=null;
	public String STATUS=null;  
	
	public String SUPPLIER=null;  
	public String NAMEALPHA=null;
	public String SPEC=null;
	public String EXTSKU=null;
	public String MPLEVEL=null;
	public String PROJECTCODE=null;
	public String PROJECTID=null;
	public String ERPLOC=null;

	public BigDecimal  AsnQTY=null;


	public UsedPOBean( String vPOKEY, String vPOLINENUMBER, String sku) throws Exception
	{
		Map<String,String> mPO= DBHelper.getRecord( "select A.UOM,A.QTY,A.RECEIVEDQTY"
				+ ",A.STATUS,B.SUPPLIER,B.PROJECTCODE,A.SKU,B.PROJECTID,A.ERPLOC"
				+ " FROM WMS_PO_DETAIL A,WMS_PO B WHERE A.POKEY=B.POKEY AND A.POKEY=? AND A.POLINENUMBER=?", new String[] {vPOKEY,vPOLINENUMBER});
		//由于通过jde编码建单的数据 po里没有相关sku信息 用收货检查表中sku信息
		if (mPO.isEmpty()) throw new Exception("未找到采购单("+vPOKEY+"."+vPOLINENUMBER+")");
		POKEY=vPOKEY;
		POLINENUMBER=vPOLINENUMBER;
		SKU=mPO.get("SKU");
		UOM=mPO.get("UOM");
		QTY= ReceiptUtilHelper.poWgt2StdQty(mPO.get("QTY"),sku);
		RECEIVEDQTY=ReceiptUtilHelper.poWgt2StdQty(mPO.get("RECEIVEDQTY"),sku);
		STATUS=mPO.get("STATUS");
		SUPPLIER=mPO.get("SUPPLIER");
		SPEC=" ";
		EXTSKU=" ";
		MPLEVEL=" ";
		PROJECTCODE=mPO.get("PROJECTCODE");
		PROJECTID=mPO.get("PROJECTID");
		ERPLOC=mPO.get("ERPLOC");
		if (STATUS.compareTo("9")>=0) throw new Exception("采购单("+vPOKEY+"."+vPOLINENUMBER+")已关闭");

	}
	
	
	public BigDecimal ClacByAsn(BigDecimal remainQty) throws Exception
	{
			if (QTY.subtract(RECEIVEDQTY).compareTo(remainQty)>0)
			{
				AsnQTY=remainQty;
				STATUS="5";
			}
			else
			{
				AsnQTY=QTY.subtract(RECEIVEDQTY);
				STATUS="9";
			}
			return AsnQTY;

	}
	
}
