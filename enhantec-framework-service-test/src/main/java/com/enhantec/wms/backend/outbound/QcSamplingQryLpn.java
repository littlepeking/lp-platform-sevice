package com.enhantec.wms.backend.outbound;

import com.enhantec.wms.backend.utils.common.LegacyDBHelper;
import com.enhantec.wms.backend.common.base.CodeLookup;
import com.enhantec.wms.backend.common.base.SKU;
import com.enhantec.wms.backend.common.inventory.LotxLocxId;
import com.enhantec.wms.backend.framework.LegacyBaseService;
import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.framework.ServiceDataMap;
import com.enhantec.wms.backend.utils.common.ExceptionHelper;
import com.enhantec.wms.backend.utils.common.FulfillLogicException;
import com.enhantec.wms.backend.utils.common.Loc;

import java.sql.Connection;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class QcSamplingQryLpn extends LegacyBaseService
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
		
		String userid = context.getUserID();

		Connection conn = context.getConnection();

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
			//String STORERKEY=XtSql.GetValue(context, conn, "select udf1 from codelkup where listname=? and code=?", new String[]{"STASYSSET","STORERKEY"}, "");

			String[] Orders= LegacyDBHelper.GetValueList(context, conn
					, "select Status,NOTES from orders where orderkey=? and ohtype=?", new String[]{ORDERKEY,ORDERTYPE});
			if (Orders==null) throw new Exception("未找到在库取样单("+ORDERKEY+")");
			if (Orders[0].compareTo("90")>=0)  throw new Exception("在库取样单("+ORDERKEY+")已关闭,不能继续操作");
			//String cnt1=XtSql.GetValue(context, conn
			//		, "select count(1) from ORDERQCLOCKLOC where orderkey=? and loc=?"
			//		, new String[]{ORDERKEY,LOC}, "0");
			//String PROJECT=XtSql.GetValue(context, conn, "SELECT UDF1 FROM CODELKUP WHERE LISTNAME=? AND CODE=?", new String[] {"STASYSSET","LGPROJECT"}, "");

			HashMap<String,String>  locHashMap = Loc.findById(context,conn,LOC,true);;

			if(!locHashMap.get("LOCATIONTYPE").equals("OTHER"))  ExceptionHelper.throwRfFulfillLogicException("不允许对'其他'之外的库位类型进行取样");

			HashMap<String,String> locxlocxIdHashMap = LotxLocxId.findAvailInvByLocId(context, conn, LOC, LPN, false,true);
			if(!LOTTABLE06.equals(locxlocxIdHashMap.get("LOTTABLE06"))) throw new Exception("容器条码和收货批次不匹配");

			HashMap<String,String> skuHashMap =  SKU.findById(context,conn,locxlocxIdHashMap.get("SKU"),true);

			if(locxlocxIdHashMap.get("LOTTABLE01").equals(skuHashMap.get("SUSR6"))) throw new Exception("容器"+LPN+"已为取样容器，不允许再次取样");

			availableQty = locxlocxIdHashMap.get("AVAILABLEQTY");

			LinkedHashMap<String,String> res= LegacyDBHelper.GetValueMap(context, conn,"select c.PACKUOM3 UOM, s.SKU SKU, s.DESCR SKUDESCR from v_lotattribute a, pack c, SKU s where s.SKU = a.SKU and a.STORERKEY = ? and a.lot=?", new String[]{CodeLookup.getSysConfig(context,conn,"STORERKEY"), locxlocxIdHashMap.get("LOT")});

			uom = res.get("UOM");
			
			/*
			String PUTAWAYZONE=XtSql.GetValue(context, conn, "select PUTAWAYZONE from loc where loc=?", new String[]{LOC}, "");
			if (PUTAWAYZONE.equals("DOCK"))
			{
				
			}
			else
			{
				
			}*/
			//LinkedHashMap<String,String> mIDNOTES= XtSql.GetValueMap(context, conn, "SELECT PACKKEY,UOM FROM IDNOTES WHERE ID=?", new String[] {LPN});
			//PACKKEY=mKC.get("PACKKEY");
			//PACKKEY=XtSql.GetValue(context, conn, "select packkey from id where id=?", new String[]{LPN}, "");
			//UOM=XtSql.GetValue(context, conn, "select packuom3 from PACK where PACKKEY=?", new String[]{PACKKEY}, "");

		    LinkedHashMap<String,String> odMap= LegacyDBHelper.GetValueMap(context, conn
		    		, "SELECT UOM, ORIGINALQTY,OPENQTY "
		    				+ " FROM ORDERDETAIL WHERE ORDERKEY=? AND IDREQUIRED=?"
		    		, new String[] {ORDERKEY,LPN});
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
			//SKUDESCR=XtSql.GetValue(context, conn, "select descr from sku where storerkey=? and sku=?"
			//		, new String[]{STORERKEY,SKU}, "");

			ServiceDataMap theOutDO = new ServiceDataMap();
			theOutDO.setAttribValue("AVAILABLEQTY", availableQty);
			serviceDataHolder.setReturnCode(1);
			serviceDataHolder.setOutputData(theOutDO);


			
		}
		catch (Exception e)
		{
			try
			{
				context.releaseConnection(conn);
			}	catch (Exception e1) {		}
			if ( e instanceof FulfillLogicException)
				throw (FulfillLogicException)e;
			else
		        throw new FulfillLogicException(e.getMessage());
		}finally {
			try	{	context.releaseConnection(conn); }	catch (Exception e1) {		}
		}
		
	}
}