package com.enhantec.wms.backend.outbound;

import com.enhantec.wms.backend.utils.common.LegacyDBHelper;
import com.enhantec.wms.backend.common.base.UOM;
import com.enhantec.wms.backend.common.inventory.LotxLocxId;
import com.enhantec.wms.backend.common.outbound.Orders;
import com.enhantec.wms.backend.framework.WMSBaseService;
import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.framework.ServiceDataMap;
import com.enhantec.wms.backend.utils.audit.Udtrn;
import com.enhantec.wms.backend.utils.common.*;

import java.math.BigDecimal;
import com.enhantec.framework.common.utils.EHContextHelper;
import java.util.Map;
import java.util.HashMap;

public class QcSamplingLpnQty extends WMSBaseService
{



	/**
	john
	--注册方法
	 delete from wmsadmin.sproceduremap where COMPOSITE = 'QcSamplingLpnQty';
	 insert into wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION,  ADDWHO,  EDITWHO, PARAMETERS,LOAD,ISBATCH)
	values ('QcSamplingLpnQty', 'com.enhantec.sce.outbound.order', 'enhantec', 'QcSamplingLpnQty', 'TRUE', 'JOHN', 'JOHN', 'sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,LOTTABLE06,ORDERKEY,ORDERTYPE,LOC,LPN,OPENQTY,UOM,SAMPLEQTY,SAMPLEUOM,ESIGNATUREKEY','0.10','0');
	*/
	

	private static final long serialVersionUID = 1L;

	public QcSamplingLpnQty()
	{
	}
	

	public void execute(ServiceDataHolder serviceDataHolder)
	{
		//public RrDateTime currentDate;
		//this.currentDate = UtilHelper.getCurrentDate();

//		context = ()processData.getInputDataMap();
//		EXEDataObjectprocessData.getInputDataMap() = (EXEDataObject)context.theEXEDataObjectStack.stackList.get(1);1
		
		String userid = EHContextHelper.getUser().getUsername();



		try
		{
			
			String LOTTABLE06= serviceDataHolder.getInputDataAsMap().getString( "LOTTABLE06");
		    String ORDERKEY= serviceDataHolder.getInputDataAsMap().getString( "ORDERKEY");
		    String ORDERTYPE= serviceDataHolder.getInputDataAsMap().getString( "ORDERTYPE");
			String LOC= serviceDataHolder.getInputDataAsMap().getString( "LOC");
			String LPN= serviceDataHolder.getInputDataAsMap().getString( "LPN");
			//String MUOM=processData.getInputDataMap().getString( "MUOM");
			String openQty= serviceDataHolder.getInputDataAsMap().getString( "OPENQTY");
			String uom= serviceDataHolder.getInputDataAsMap().getString( "UOM");
			//String PROJECT=processData.getInputDataMap().getString( "PROJECT");
			String sampleUom= serviceDataHolder.getInputDataAsMap().getString( "SAMPLEUOM");
			String sampleQty= serviceDataHolder.getInputDataAsMap().getString( "SAMPLEQTY");
			String ESIGNATUREKEY= serviceDataHolder.getInputDataAsMap().getString( "ESIGNATUREKEY");
			String TOTAL1="";
			String TOTAL3="";
			
			String orderlinenumber=null;

			//String STORERKEY=XtSql.GetValue( "select udf1 from codelkup where listname=? and code=?", new String[]{"STASYSSET","STORERKEY"}, "");

			String Status= DBHelper.getStringValue( "select Status from orders where orderkey=? and ohtype=?", new String[]{ORDERKEY,ORDERTYPE}, "");
			if (LegecyUtilHelper.isNull(Status)) throw new Exception("未找到在库取样单("+ORDERKEY+")");
			if (Status.compareTo("90")>=0)  throw new Exception("在库取样单("+ORDERKEY+")已关闭,不能继续操作");


			Map<String,String> odMap= DBHelper.getRecord( "SELECT OPENQTY, UOM "
							+ " FROM ORDERDETAIL WHERE ORDERKEY=? AND IDREQUIRED=?"
					, new String[] {ORDERKEY,LPN},"");

			if(!odMap.isEmpty()) ExceptionHelper.throwRfFulfillLogicException("该容器条码已存在于本取样单中，请删除后再添加");

			Map<String,String> mKC = LotxLocxId.findAvailInvByLocId( LOC, LPN, false,true);
			if(!LOTTABLE06.equals(mKC.get("LOTTABLE06"))) throw new Exception("容器条码和取样批次不匹配");

			String packKey=mKC.get("PACKKEY");
			
			//String PUTAWAYZONE=XtSql.GetValue( "select PUTAWAYZONE from loc where loc=?", new String[]{LOC}, "");
			//if (PUTAWAYZONE.equals("DOCK")) throw new Exception("不允许在收货库位取样,请先上架");
			
//			Map<String,String> mIDNOTES=XtSql.GetValueMap( "SELECT PACKKEY,UOM,BARRELDESCR FROM IDNOTES WHERE ID=?", new String[]{LPN});
//			if (mIDNOTES.isEmpty()) throw new Exception("未找到桶信息");
			
			String STORERKEY=mKC.get("STORERKEY");
			String SKU=mKC.get("SKU");
			BigDecimal availableQty=new BigDecimal(mKC.get("AVAILABLEQTY"));
			
			BigDecimal bOpenQty=new BigDecimal(openQty);
			//取样量目前只用做审计使用，不参与任何计算
			//if (bOpenQty.compareTo(bOriginalQty)>0) throw new Exception("扣样量不允许大于取样量");

			//保留此句，为了今后兼容取样时的扣样计量单位不是主单位的情况
			BigDecimal baseUomOpenQty= UOM.UOMQty2StdQty( packKey, uom,bOpenQty);

			if (baseUomOpenQty.compareTo(availableQty)>0) throw new Exception("扣样量不允许大于可用库存");
			orderlinenumber= DBHelper.getStringValue( "select max(orderlinenumber) from orderdetail where orderkey=?"
					,new String[]{ORDERKEY}, "0");
			orderlinenumber=Integer.toString(Integer.parseInt(orderlinenumber)+1);
			while (orderlinenumber.length()<5) orderlinenumber="0"+orderlinenumber;

			Map<String, String> orderInfo = Orders.findByOrderKey(ORDERKEY,true);
			
			Map<String,String> OrderDetail=new HashMap<String,String>();
			OrderDetail.put("ADDWHO", userid);
			OrderDetail.put("EDITWHO", userid);
			OrderDetail.put("STATUS", "02");
			OrderDetail.put("ORDERKEY", ORDERKEY);
			OrderDetail.put("ORDERLINENUMBER", orderlinenumber);
			OrderDetail.put("EXTERNORDERKEY", orderInfo.get("EXTERNORDERKEY"));
			//OrderDetail.put("EXTERNLINENO", PROJECT);
			OrderDetail.put("STORERKEY", STORERKEY);
			OrderDetail.put("SKU", SKU);
			OrderDetail.put("ORIGINALQTY", String.valueOf(bOpenQty));//扣样量
			OrderDetail.put("OPENQTY",  String.valueOf(bOpenQty));//扣样量
			OrderDetail.put("PACKKEY", packKey);
			OrderDetail.put("UOM", uom);//扣样量计量单位
			OrderDetail.put("SUSR1",  String.valueOf(sampleQty));//取样量
			OrderDetail.put("SUSR2",  String.valueOf(sampleUom));//取样计量单位
			OrderDetail.put("SUSR4", mKC.get("BARRELDESCR")); //桶号
			OrderDetail.put("IDREQUIRED", LPN);
			OrderDetail.put("LOTTABLE06", LOTTABLE06);

			OrderDetail.put("NEWALLOCATIONSTRATEGY", "N21"); //分配策略:匹配数量，然后最佳适配

			//OrderDetail.put("SUSR1", "取样量:"+originalQty+uom);
//			if (openQty.equals("0"))
//				OrderDetail.put("SUSR2", "扣样量:");
//			else
//				OrderDetail.put("SUSR2", "扣样量:"+OPENQTY1+UOM);
			//OrderDetail.put("SUSR3", "取样库位:"+LOC);
			//OrderDetail.put("NEWALLOCATIONSTRATEGY", "STD");
			
			LegacyDBHelper.ExecInsert( "orderdetail", OrderDetail);
			
			Udtrn UDTRN=new Udtrn();
			UDTRN.EsignatureKey=ESIGNATUREKEY;
			UDTRN.FROMTYPE="在库取样-新增取样记录";
			UDTRN.FROMTABLENAME="ORDERDETAIL";
		    UDTRN.FROMKEY=LOTTABLE06;
		    UDTRN.FROMKEY1=ORDERKEY;
		    UDTRN.FROMKEY2=orderlinenumber;
		    UDTRN.FROMKEY3="";
		    UDTRN.TITLE01="取样批次";    UDTRN.CONTENT01=LOTTABLE06;
		    UDTRN.TITLE02="出库单号";    UDTRN.CONTENT02=ORDERKEY;
		    UDTRN.TITLE03="出库单行号";    UDTRN.CONTENT03=orderlinenumber;
		    UDTRN.TITLE04="SKU";    UDTRN.CONTENT04=SKU;
		    UDTRN.TITLE05="容器条码";    UDTRN.CONTENT05=LPN;
		    UDTRN.TITLE06="取样量";    UDTRN.CONTENT06=sampleQty;
			UDTRN.TITLE07="取样计量单位";    UDTRN.CONTENT07=sampleUom;
			UDTRN.TITLE08="扣样量";    UDTRN.CONTENT08=openQty;
			UDTRN.TITLE09="扣样计量单位";    UDTRN.CONTENT09=uom;
		    UDTRN.TITLE10="库位";    UDTRN.CONTENT10=LOC;
		    UDTRN.Insert( userid);
			
				
			//----------------
            //String CountLpn1=XtSql.GetValue( "select count(1) from (select distinct IDREQUIRED from ORDERDETAIL where orderkey=?) a",new String[]{ORDERKEY},"");
			//            TOTAL1="取样容器("+CountLpn1+")";
//			List<Map<String,String>> mapList =XtSql.GetRecordMap( "select ORIGINALQTY,OPENQTY,UOM from ORDERDETAIL where orderkey=?",new String[]{ORDERKEY});
//
//			Function<Map<String,String>, BigDecimal> calcORIGINALQTY = e-> {
//				try {
//					return uomConverter.UOMQty2StdQty(packKey, e.get("UOM"), new BigDecimal(e.get("ORIGINALQTY")));
//				} catch (Exception exception) {
//					return BigDecimal.ZERO;
//				}
//			};
//
//			Function<Map<String,String>, BigDecimal> calcOPENQTY = e-> {
//				try {
//					return uomConverter.UOMQty2StdQty(packKey, e.get("UOM"), new BigDecimal(e.get("OPENQTY")));
//				} catch (Exception exception) {
//					return BigDecimal.ZERO;
//				}
//			};

//			if(!mapList.isEmpty()){
//				TOTAL1 = String.valueOf(mapList.stream().map(calcORIGINALQTY).reduce(BigDecimal.ZERO, (BigDecimal subtotal, BigDecimal element) -> subtotal.add(element)));
//				TOTAL2 = String.valueOf(mapList.stream().map(calcOPENQTY).reduce(BigDecimal.ZERO, (BigDecimal subtotal, BigDecimal element) -> subtotal.add(element)));
//			}

			TOTAL1= DBHelper.getStringValue( "select SUM(CONVERT(decimal(11,5), SUSR1)) from ORDERDETAIL where orderkey=?",new String[]{ORDERKEY},"0");
			TOTAL3= DBHelper.getStringValue( "SELECT SUM(CONVERT(decimal(11,5), OPENQTY)) from ORDERDETAIL where orderkey=?",new String[]{ORDERKEY},"0");

			ServiceDataMap theOutDO = new ServiceDataMap();
			theOutDO.setAttribValue("TOTAL1", TOTAL1);//取样量合计
			theOutDO.setAttribValue("TOTAL3", TOTAL3);//扣样量合计
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