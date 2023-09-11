package com.enhantec.wms.backend.inbound.putaway;

import com.enhantec.framework.common.utils.EHContextHelper;
import com.enhantec.wms.backend.common.inventory.LotxLocxId;
import com.enhantec.wms.backend.framework.WMSBaseService;
import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.framework.ServiceDataMap;
import com.enhantec.wms.backend.inventory.utils.InventoryHelper;
import com.enhantec.wms.backend.inventory.utils.InventoryValidationHelper;
import com.enhantec.wms.backend.utils.audit.Udtrn;
import com.enhantec.wms.backend.utils.common.DBHelper;
import com.enhantec.wms.backend.utils.common.FulfillLogicException;

import java.util.Map;

public class PutawayByLPN extends WMSBaseService {

	/**
	--注册方法
	insert into wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION, ADDDATE, ADDWHO, EDITDATE, EDITWHO, PARAMETERS,LOAD,ISBATCH)
	values ('PutawayByLPN', 'com.enhantec.sce.inbound.putaway', 'enhantec', 'PutawayByLPN', 'TRUE', SYSDATE, 'XT', SYSDATE, 'XT', 'sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,'||
	'zonetype,storerkey,lot,sku,fromloc,fromid,toloc,toid,qty,uom,packkey,refnum,suggestedtoloc,finaltoloc,erryes,transactionkey,ESIGNATUREKEY','0.10','0');
		 */
	

	private static final long serialVersionUID = 1L;

	public PutawayByLPN()
	{
	}

	public void execute(ServiceDataHolder serviceDataHolder)
	{
		String userid = EHContextHelper.getUser().getUsername();

		try
		{
			String fromid= serviceDataHolder.getInputDataAsMap().getString("fromid");
		    String toid= serviceDataHolder.getInputDataAsMap().getString("toid");
			String fromloc= serviceDataHolder.getInputDataAsMap().getString("fromloc"); //来源库位
		    String toloc= serviceDataHolder.getInputDataAsMap().getString("toloc"); //最终库位
		    String sku= serviceDataHolder.getInputDataAsMap().getString("sku");
			String packkey= serviceDataHolder.getInputDataAsMap().getString("packkey");
			String uom= serviceDataHolder.getInputDataAsMap().getString("uom");
		    String finaltoloc= serviceDataHolder.getInputDataAsMap().getString("finaltoloc");
			String putawayzone= serviceDataHolder.getInputDataAsMap().getString("putawayzone");
		    String ESIGNATUREKEY= serviceDataHolder.getInputDataAsMap().getString("ESIGNATUREKEY");

			//确保LPN没有被分配和拣货才允许移动。
			Map<String, String> idHashMapWithLot = LotxLocxId.findFullAvailInvById( fromloc, fromid,"容器不存在或者已被分配或拣货，不允许上架");
			InventoryValidationHelper.validateLocMix( fromid ,fromloc , toloc);
			InventoryHelper.checkLocQuantityLimit(toloc);
//	todo		super.execute(pObject);

			Udtrn UDTRN=new Udtrn();
			UDTRN.EsignatureKey=ESIGNATUREKEY;
			UDTRN.FROMTYPE="上架";
			UDTRN.FROMTABLENAME="LOTXLOCXID";
		    UDTRN.FROMKEY=toid;
		    UDTRN.FROMKEY1="";
		    UDTRN.FROMKEY2="";
		    UDTRN.FROMKEY3="";
		    UDTRN.TITLE01="容器条码";    UDTRN.CONTENT01=fromid;
		    UDTRN.TITLE02="SKU";    UDTRN.CONTENT02=sku;
		    UDTRN.TITLE03="来源库位";    UDTRN.CONTENT03=toloc;
		    UDTRN.TITLE04="目标库位";    UDTRN.CONTENT04=finaltoloc;
		    UDTRN.Insert( userid);

		    String TOTAL1= DBHelper.getStringValue( "SELECT COUNT(1) FROM LOTXLOCXID A,LOC C WHERE A.LOC=C.LOC AND A.QTY>0 AND C.PUTAWAYZONE='DOCK' AND A.LOT=?", new String[]{idHashMapWithLot.get("LOT")}, "0");
    		String TOTAL2= DBHelper.getStringValue(
    		 "SELECT COUNT(1) FROM LOTXLOCXID A WHERE A.LOT=? AND A.QTY>0", new String[]{idHashMapWithLot.get("LOT")}, "0");
		    String TOTAL=TOTAL1+" / "+TOTAL2;
		    
			

			ServiceDataMap theOutDO = new ServiceDataMap();
			theOutDO.setAttribValue("TOTAL", TOTAL);
			serviceDataHolder.setReturnCode(1);
			serviceDataHolder.setOutputData(theOutDO);

		}
		catch (Exception e)
		{
			
			if ( e instanceof FulfillLogicException)
				throw (FulfillLogicException)e;
			else
		        throw new FulfillLogicException(e.getMessage());
		}

		
	}
}