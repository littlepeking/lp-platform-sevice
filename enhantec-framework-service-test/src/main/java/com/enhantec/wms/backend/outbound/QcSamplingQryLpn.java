package com.enhantec.wms.backend.outbound;

import com.enhantec.wms.backend.utils.common.*;
import com.enhantec.wms.backend.common.base.CodeLookup;
import com.enhantec.wms.backend.common.base.SKU;
import com.enhantec.wms.backend.common.inventory.LotxLocxId;
import com.enhantec.wms.backend.framework.WMSBaseService;
import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.framework.ServiceDataMap;

import com.enhantec.framework.common.utils.EHContextHelper;
import java.util.Map;

public class QcSamplingQryLpn extends WMSBaseService
{



	/**
	 * JOHN 20201021 查询取样记录
	 *
	 --注册方法
	 DELETE FROM wmsadmin.sproceduremap where COMPOSITE= 'QcSamplingQryLpn';
	insert into wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION, ADDWHO, EDITWHO, PARAMETERS,LOAD,ISBATCH)
	values ('QcSamplingQryLpn', 'com.enhantec.sce.outbound.order', 'enhantec', 'QcSamplingQryLpn', 'TRUE', 'JOHN',  'JOHN', 'sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server, LOTTABLE06,ORDERKEY,ORDERTYPE,LOC,LPN,ESIGNATUREKEY','0.10','0');

	 */
	

	private static final long serialVersionUID = 1L;

	public QcSamplingQryLpn()
	{
	}

	public void execute(ServiceDataHolder serviceDataHolder)
	{
		
		String userid = EHContextHelper.getUser().getUsername();



		try
		{
			
			
			
			String LOTTABLE06= serviceDataHolder.getInputDataAsMap().getString( "LOTTABLE06");
		    String ORDERKEY= serviceDataHolder.getInputDataAsMap().getString( "ORDERKEY");
		    String ORDERTYPE= serviceDataHolder.getInputDataAsMap().getString( "ORDERTYPE");
			String LOC= serviceDataHolder.getInputDataAsMap().getString( "LOC");
			String LPN= serviceDataHolder.getInputDataAsMap().getString( "LPN");
			String ESIGNATUREKEY= serviceDataHolder.getInputDataAsMap().getString( "ESIGNATUREKEY");
			String openQty="";//扣样量
			String originalQty=""; //取样量
			String availableQty="";//该ID可用量
			String uom= "";


			String PACKKEY="";
			//String SKU="";
			String SKUDESCR="";
			//String STORERKEY=XtSql.GetValue( "select udf1 from codelkup where listname=? and code=?", new String[]{"STASYSSET","STORERKEY"}, "");

			Map<String,String> Order = DBHelper.getRecord( "select Status,NOTES from orders where orderkey=? and ohtype=?", new String[]{ORDERKEY,ORDERTYPE},"未找到在库取样单("+ORDERKEY+")");
			if (Order.get("Status").compareTo("90")>=0)  throw new Exception("在库取样单("+ORDERKEY+")已关闭,不能继续操作");
			//String cnt1=XtSql.GetValue(context			//		, "select count(1) from ORDERQCLOCKLOC where orderkey=? and loc=?"
			//		, new String[]{ORDERKEY,LOC}, "0");
			//String PROJECT=XtSql.GetValue( "SELECT UDF1 FROM CODELKUP WHERE LISTNAME=? AND CODE=?", new String[] {"STASYSSET","LGPROJECT"}, "");

			Map<String,String>  locHashMap = Loc.findById(LOC,true);;

			if(!locHashMap.get("LOCATIONTYPE").equals("OTHER"))  ExceptionHelper.throwRfFulfillLogicException("不允许对'其他'之外的库位类型进行取样");

			Map<String,String> locxlocxIdHashMap = LotxLocxId.findAvailInvByLocId( LOC, LPN, false,true);
			if(!LOTTABLE06.equals(locxlocxIdHashMap.get("LOTTABLE06"))) throw new Exception("容器条码和收货批次不匹配");

			Map<String,String> skuHashMap =  SKU.findById(locxlocxIdHashMap.get("SKU"),true);

			if(locxlocxIdHashMap.get("LOTTABLE01").equals(skuHashMap.get("SUSR6"))) throw new Exception("容器"+LPN+"已为取样容器，不允许再次取样");

			availableQty = locxlocxIdHashMap.get("AVAILABLEQTY");

			Map<String,String> res= DBHelper.getRecord("select c.PACKUOM3 UOM, s.SKU SKU, s.DESCR SKUDESCR from v_lotattribute a, pack c, SKU s where s.SKU = a.SKU and a.STORERKEY = ? and a.lot=?", new String[]{CodeLookup.getSysConfig("STORERKEY"), locxlocxIdHashMap.get("LOT")},"");

			uom = res.get("UOM");
			
			/*
			String PUTAWAYZONE=XtSql.GetValue( "select PUTAWAYZONE from loc where loc=?", new String[]{LOC}, "");
			if (PUTAWAYZONE.equals("DOCK"))
			{
				
			}
			else
			{
				
			}*/
			//Map<String,String> mIDNOTES= XtSql.GetValueMap( "SELECT PACKKEY,UOM FROM IDNOTES WHERE ID=?", new String[] {LPN});
			//PACKKEY=mKC.get("PACKKEY");
			//PACKKEY=XtSql.GetValue( "select packkey from id where id=?", new String[]{LPN}, "");
			//UOM=XtSql.GetValue( "select packuom3 from PACK where PACKKEY=?", new String[]{PACKKEY}, "");

		    Map<String,String> odMap= DBHelper.getRecord( "SELECT UOM, ORIGINALQTY,OPENQTY "
		    				+ " FROM ORDERDETAIL WHERE ORDERKEY=? AND IDREQUIRED=?"
		    		, new String[] {ORDERKEY,LPN},"");
		    boolean recordExists = false;
			if (odMap.isEmpty()){
				originalQty="";
				openQty="";
			}else{
				originalQty=odMap.get("ORIGINALQTY");
				openQty=odMap.get("OPENQTY");
				uom=odMap.get("UOM");
				recordExists = true;
			}

			//目前只允许删除后添加，不允许修改
			if(recordExists) ExceptionHelper.throwRfFulfillLogicException("该容器条码已存在于本取样单中，请删除后再添加");

		    //String UOM=XtUtils.Nz(mORDER.get("LOTTABLE09"),"");
		    //String ORDERLINENUMBER=XtUtils.Nz(mORDER.get("ORDERLINENUMBER"),"");
			//String STORERKEY=mKC.get("STORERKEY");
			//SKU=mKC.get("SKU");
			//SKUDESCR=XtSql.GetValue( "select descr from sku where storerkey=? and sku=?"
			//		, new String[]{STORERKEY,SKU}, "");

			ServiceDataMap theOutDO = new ServiceDataMap();
			theOutDO.setAttribValue("AVAILABLEQTY", availableQty);
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