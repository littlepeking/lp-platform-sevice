package com.enhantec.wms.backend.inventory;

import com.enhantec.wms.backend.common.base.IDNotes;
import com.enhantec.wms.backend.common.base.SKU;
import com.enhantec.wms.backend.common.inventory.LotxLocxId;
import com.enhantec.wms.backend.framework.WMSBaseService;
import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.framework.ServiceDataMap;
import com.enhantec.wms.backend.inventory.utils.InventoryHelper;
import com.enhantec.wms.backend.utils.audit.Udtrn;
import com.enhantec.wms.backend.utils.common.ExceptionHelper;
import com.enhantec.wms.backend.utils.common.FulfillLogicException;
import com.enhantec.wms.backend.utils.common.UtilHelper;

import com.enhantec.framework.common.utils.EHContextHelper;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;

public class SplitLpn extends WMSBaseService {
	/**
	--注册方法
	 delete from SCPRDMST.wmsadmin.sproceduremap where THEPROCNAME = 'EHSplitLpn';
	 insert into SCPRDMST.wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION, ADDWHO, EDITWHO, PARAMETERS,LOAD,ISBATCH)
	 values ('EHSplitLpn', 'com.enhantec.sce.inventory', 'enhantec', 'SplitLpn','TRUE','JOHN','JOHN','sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,SKU,FROMID,TOID,GROSSWGT,NETWGT,TAREWGT,SPLITUOM,PRINTER,ESIGNATUREKEY','0.10','0');
	 **/

	private static final long serialVersionUID = 1L;

	public SplitLpn()
	{
	}

	public void execute(ServiceDataHolder serviceDataHolder)
	{
		String userid = EHContextHelper.getUser().getUsername();


		try
		{
			String opName = "容器拆分";
			String sku= serviceDataHolder.getInputDataAsMap().getString("SKU");
			String fromId= serviceDataHolder.getInputDataAsMap().getString("FROMID");
			String toId = serviceDataHolder.getInputDataAsMap().getString("TOID");
			String toBeMovedGrossWgt= serviceDataHolder.getInputDataAsMap().getString("GROSSWGT");
			String toBeMovedNetWgt= serviceDataHolder.getInputDataAsMap().getString("NETWGT");
			String toBeMovedTareWgt= serviceDataHolder.getInputDataAsMap().getString("TAREWGT");
			String toBeMovedUOM= serviceDataHolder.getInputDataAsMap().getString("SPLITUOM");
			String printer= serviceDataHolder.getInputDataAsMap().getString("PRINTER");
			String esignatureKey= serviceDataHolder.getInputDataAsMap().getString("ESIGNATUREKEY");

			if(UtilHelper.isEmpty(fromId)) ExceptionHelper.throwRfFulfillLogicException("容器条码不允许为空");

			Map<String,String> lotxLocxIdHashMap = null;
			String qtyToSplit ="0";
			List<String> snList = new ArrayList<>();

			if(!UtilHelper.isEmpty(toId)) {
				if (SKU.isSerialControl( sku)){
					if(!IDNotes.isBoxId(toId))	ExceptionHelper.throwRfFulfillLogicException("扫描的分拆至箱号不符合系统箱号规则，请检查后再提交");
				}else {
//					ExceptionHelper.throwRfFulfillLogicException("按批次管理的物料不支持指定分拆至容器条码，系统会自动生成");
				}
			}

			if(SKU.isSerialControl(sku) && !IDNotes.isBoxId(fromId)){

				lotxLocxIdHashMap = LotxLocxId.findBySkuAndSerialNum(sku,fromId);

				snList.add(fromId);
				qtyToSplit = "1";

			}else if(SKU.isSerialControl(sku) && IDNotes.isBoxId(fromId)){

				ExceptionHelper.throwRfFulfillLogicException("暂不支持整箱号合并操作，请直接扫描唯一码");
			}else {
				lotxLocxIdHashMap = LotxLocxId.findById( fromId, true);
				qtyToSplit = toBeMovedNetWgt;
			}

			if(UtilHelper.decimalStrCompare(lotxLocxIdHashMap.get("AVAILABLEQTY"),qtyToSplit)<0) {
				ExceptionHelper.throwRfFulfillLogicException("容器不存在或者可供分拆的数量不足");
			}


			String toLoc = lotxLocxIdHashMap.get("LOC");
			if(!UtilHelper.isEmpty(toId)){
				Map<String, String> toIdHashMap = LotxLocxId.findById( toId, false);
				if(toIdHashMap !=null) toLoc = toIdHashMap.get("LOC");
			}

			Map<String,String> result = InventoryHelper.doMove(opName, lotxLocxIdHashMap, snList, toId, lotxLocxIdHashMap.get("LOC"), toLoc, toBeMovedNetWgt, toBeMovedGrossWgt, toBeMovedTareWgt, toBeMovedUOM, printer,true);

			toId = result.get("TOID");
			String printLabel = result.get("PRINT");

			Udtrn UDTRN=new Udtrn();
			UDTRN.EsignatureKey=esignatureKey;
			UDTRN.FROMTYPE=opName;
			UDTRN.FROMTABLENAME="LOTXLOCXID";
			UDTRN.FROMKEY=fromId;
			UDTRN.FROMKEY1=toId;
			UDTRN.FROMKEY2=lotxLocxIdHashMap.get("LOC");
			UDTRN.FROMKEY3=toLoc;
			UDTRN.TITLE01="来源容器条码/箱号";    UDTRN.CONTENT01=fromId;
			UDTRN.TITLE02="来源库位";    UDTRN.CONTENT02=lotxLocxIdHashMap.get("LOC");
			UDTRN.TITLE03="目标容器条码";    UDTRN.CONTENT03=toId;
			UDTRN.TITLE04="目标库位";    UDTRN.CONTENT04=toLoc;
			UDTRN.TITLE05="数量";    UDTRN.CONTENT05=qtyToSplit;

			UDTRN.insert( userid);

			ServiceDataMap theOutDO = new ServiceDataMap();

			theOutDO.setAttribValue("TOID", toId);
			theOutDO.setAttribValue("PRINTLABEL",printLabel);
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