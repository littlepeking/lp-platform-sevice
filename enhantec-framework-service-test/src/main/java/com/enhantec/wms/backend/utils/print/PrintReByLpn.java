package com.enhantec.wms.backend.utils.print;

import com.enhantec.framework.common.utils.EHContextHelper;
import com.enhantec.wms.backend.utils.common.LegacyDBHelper;
import com.enhantec.wms.backend.common.base.CodeLookup;
import com.enhantec.wms.backend.framework.WMSBaseService;
import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.framework.ServiceDataMap;
import com.enhantec.wms.backend.utils.audit.Udtrn;
import com.enhantec.wms.backend.utils.common.*;

import java.util.Map;

public class PrintReByLpn extends WMSBaseService
{



	/**
	--注册方法

	 DELETE FROM scprdmst.wmsadmin.sproceduremap WHERE THEPROCNAME = 'PrintReByLpn';
	 insert into scprdmst.wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION,  ADDWHO,  EDITWHO, PARAMETERS,LOAD,ISBATCH)
	 values ('PrintReByLpn', 'com.enhantec.sce.utils.print', 'enhantec', 'PrintReByLpn', 'TRUE',  'john',  'john', 'sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,PRINTER,LPN,LABELTYPE,ESIGNATUREKEY','0.10','0');

	 */
	

	private static final long serialVersionUID = 1L;

	public PrintReByLpn()
	{
	}

	public void execute(ServiceDataHolder serviceDataHolder)
	{
		//public RrDateTime currentDate;
		//this.currentDate = UtilHelper.getCurrentDate();

//		EXEDataObjectprocessData.getInputDataMap() = (EXEDataObject)context.theEXEDataObjectStack.stackList.get(1);
		
		String userid = EHContextHelper.getUser().getUsername();



		try
		{
			LegacyDBHelper r1=null;
		    String PRINTER= serviceDataHolder.getInputDataAsMap().getString("PRINTER");
		    String LPN= serviceDataHolder.getInputDataAsMap().getString("LPN");
//		    String LOCATION=processData.getInputDataMap().getString( "LOCATION");
			String LABELTYPE = serviceDataHolder.getInputDataAsMap().getString("LABELTYPE");
			String ESIGNATUREKEY= serviceDataHolder.getInputDataAsMap().getString("ESIGNATUREKEY");
			//if (PRINTER.equals("0")) throw new Exception("不能选择缓存打印机");
//			if ((!LOCATION.equals("CJ"))&&(!LOCATION.equals("CK")))
//				throw new Exception("参数错误");
			
			Map<String,String> mID= DBHelper.getRecord( "select id.ID, id.SKU,l.LOTTABLE06,sku.DESCR,id.BARRELDESCR,sku.DESCR from IDNOTES id,SKU sku,v_lotattribute l where sku.sku = id.sku and id.lot=l.lot and id.ID=?", new String[] {LPN});
			if (mID.isEmpty()) throw new Exception("未找到桶信息");

			DBHelper.getRecord( "select PRINTERNAME from LABELPRINTER WHERE PRINTERNAME=?", new Object[]{PRINTER},"注册的打印机",true);

			CodeLookup.getCodeLookupByKey("IDREPRINT",LABELTYPE);

			PrintHelper.printLPNByIDNotes(mID.get("ID"), LABELTYPE,PRINTER,"1","补打标签");



			Udtrn UDTRN=new Udtrn();
			UDTRN.EsignatureKey=ESIGNATUREKEY;
			UDTRN.FROMTYPE="容器标签补打";
//			if (LOCATION.equals("CK")) UDTRN.FROMTYPE="标签补打印-仓库";
			UDTRN.FROMTABLENAME="IDNOTES";
		    UDTRN.FROMKEY=mID.get("LOTTABLE06");
		    UDTRN.FROMKEY1=LPN;
		    UDTRN.FROMKEY2="";
		    UDTRN.FROMKEY3="";
		    UDTRN.TITLE01="收货批次";    UDTRN.CONTENT01=mID.get("LOTTABLE06");
		    UDTRN.TITLE02="容器条码";    UDTRN.CONTENT02=LPN;
		    UDTRN.TITLE03="桶号";    UDTRN.CONTENT03=mID.get("BARRELDESCR");
		    UDTRN.TITLE04="物料编号";    UDTRN.CONTENT04=mID.get("SKU");
		    UDTRN.TITLE05="物料名称";    UDTRN.CONTENT05=mID.get("DESCR");
		   
		    UDTRN.Insert( userid);
			



			ServiceDataMap theOutDO = new ServiceDataMap();
			serviceDataHolder.setReturnCode(1);
			serviceDataHolder.setOutputData(theOutDO);


		}
		catch (Exception e)
		{
			if ( e instanceof FulfillLogicException )
				throw (FulfillLogicException)e;
			else
		        throw new FulfillLogicException(e.getMessage());
		}finally {
			
		}

		
	}
}