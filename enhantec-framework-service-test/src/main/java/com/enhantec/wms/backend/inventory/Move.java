package com.enhantec.wms.backend.inventory;

import com.enhantec.framework.common.utils.EHContextHelper;
import com.enhantec.wms.backend.common.base.CodeLookup;
import com.enhantec.wms.backend.common.base.IDNotes;
import com.enhantec.wms.backend.common.base.SKU;
import com.enhantec.wms.backend.common.base.SerialInventory;
import com.enhantec.wms.backend.common.base.code.CDSysSet;
import com.enhantec.wms.backend.common.inventory.LotxLocxId;
import com.enhantec.wms.backend.framework.LegacyBaseService;
import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.framework.ServiceDataMap;
import com.enhantec.wms.backend.inventory.utils.InventoryHelper;
import com.enhantec.wms.backend.utils.audit.Udtrn;
import com.enhantec.wms.backend.utils.common.*;

import com.enhantec.framework.common.utils.EHContextHelper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class Move extends LegacyBaseService {
	/**
	--注册方法
	delete from wmsadmin.sproceduremap where THEPROCNAME = 'EHMove';
	insert into wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION, ADDWHO, EDITWHO, PARAMETERS,LOAD,ISBATCH)
	values ('EHMove', 'com.enhantec.sce.inventory', 'enhantec', 'Move','TRUE','JOHN','JOHN'
	, 'sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,SKU,FROMID,TOID,FROMLOC,TOLOC,TOBEMOVEDQTY, ESIGNATUREKEY','0.10','0');
    **/

	private static final long serialVersionUID = 1L;

	public Move()
	{
	}

	public void execute(ServiceDataHolder serviceDataHolder){
		String userid = EHContextHelper.getUser().getUsername();



	    boolean isMoveAction=false;

		try
		{
			String opName= serviceDataHolder.getInputDataAsMap().getString("OPNAME");

			if(UtilHelper.isEmpty(opName)){
				opName = "容器移动";
				isMoveAction =true;
			}
			String sku= serviceDataHolder.getInputDataAsMap().getString("SKU");
			String fromId= serviceDataHolder.getInputDataAsMap().getString("FROMID");
			String toId= serviceDataHolder.getInputDataAsMap().getString("TOID");
			String fromLoc= serviceDataHolder.getInputDataAsMap().getString("FROMLOC");
			String toLoc= serviceDataHolder.getInputDataAsMap().getString("TOLOC");
			String ESIGNATUREKEY= serviceDataHolder.getInputDataAsMap().getString("ESIGNATUREKEY");
			//String TOBEMOVEDQTY=processData.getInputDataMap().getString("TOBEMOVEDQTY");//该参数未使用，分拆容器使用SPLITLPN

			if (LegecyUtilHelper.isNull(fromId)) throw new Exception("容器条码不能为空");

			if(UtilHelper.isEmpty(toId)) toId = fromId;
			if(UtilHelper.isEmpty(toLoc)) toLoc = fromLoc;

			if(isMoveAction) {

				HashMap<String, String> packZoneInfo = CodeLookup.getCodeLookupByKey( "SYSSET", "PACKZONE");

				String locCountQuery = "SELECT count(1) TOTALNUM FROM LOC WHERE PUTAWAYZONE = ? AND LOC = ? ";
				HashMap<String, String> countRecord = DBHelper.getRecord( locCountQuery, new Object[]{
						packZoneInfo.get("UDF1"),
						toLoc

				}, "");

				if (!countRecord.get("TOTALNUM").equals("0")) {
					//增加移动校验，避免出现未知的库存移入并被无故分装的情况。
					ExceptionHelper.throwRfFulfillLogicException("分装间库位不允许作为移动操作的至库位，请使用分装备货功能进行备货");
				}
			}

			HashMap<String,String> lotxLocxIdHashMap;
			String qtyToMove;
			List<String> snList = new ArrayList<>();

			if(SKU.isSerialControl(sku) && !IDNotes.isBoxId(fromId)){

				lotxLocxIdHashMap = LotxLocxId.findBySkuAndSerialNum(sku,fromId);
				snList.add(fromId);
				qtyToMove = "1";
				if(UtilHelper.decimalStrCompare(lotxLocxIdHashMap.get("QTY"),"1")==0) {
					toId = lotxLocxIdHashMap.get("ID");
				}else {
					if(!CDSysSet.isAllowMoveSN()){
						ExceptionHelper.throwRfFulfillLogicException("不允许直接移动唯一码，请扫描箱号");
					}
					else {
						toId = "";
					}
				}

			}else if(SKU.isSerialControl(sku) && IDNotes.isBoxId(fromId)){

				lotxLocxIdHashMap = LotxLocxId.findById( fromId, true);

				List<HashMap<String,String>> snHashMapList =  SerialInventory.findByLpn(fromId,true);
				snList.addAll(snHashMapList.stream().map(x->x.get("SERIALNUMBER")).collect(Collectors.toList()));
				qtyToMove = String.valueOf(snList.size());
				toId =fromId;

			}else {
				lotxLocxIdHashMap = LotxLocxId.findById( fromId, true);
				qtyToMove = lotxLocxIdHashMap.get("QTY");
				toId =fromId;
			}

			if(UtilHelper.decimalStrCompare(lotxLocxIdHashMap.get("AVAILABLEQTY"),qtyToMove)<0)
				ExceptionHelper.throwRfFulfillLogicException("容器不存在或者可供移动的数量不足");
			InventoryHelper.checkLocQuantityLimit(toLoc);
			InventoryHelper.doMove( opName, lotxLocxIdHashMap, snList ,toId, fromLoc, toLoc, "", "", "", "", "-1",false);

			Udtrn UDTRN=new Udtrn();
			UDTRN.EsignatureKey=ESIGNATUREKEY;
			UDTRN.FROMTYPE=opName;
			UDTRN.FROMTABLENAME="LOTXLOCXID";
			UDTRN.FROMKEY=fromId;
			UDTRN.FROMKEY1=toId;
			UDTRN.FROMKEY2=fromLoc;
			UDTRN.FROMKEY3=toLoc;
			UDTRN.TITLE01="来源容器条码/箱号";    UDTRN.CONTENT01=fromId;
			UDTRN.TITLE02="来源库位";    UDTRN.CONTENT02=fromLoc;
			//UDTRN.TITLE02="目标容器条码";    UDTRN.CONTENT02=TOID;
			UDTRN.TITLE03="目标库位";    UDTRN.CONTENT03=toLoc;
			//UDTRN.TITLE04="数量";    UDTRN.CONTENT04=TOBEMOVEDQTY;

			UDTRN.Insert( userid);

			

			ServiceDataMap theOutDO = new ServiceDataMap();
			theOutDO.setAttribValue("OK", "1");
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