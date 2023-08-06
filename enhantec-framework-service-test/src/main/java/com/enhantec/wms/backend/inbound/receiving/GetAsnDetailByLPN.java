package com.enhantec.wms.backend.inbound.receiving;

import com.enhantec.wms.backend.utils.common.LegacyDBHelper;
import com.enhantec.wms.backend.common.base.SKU;
import com.enhantec.wms.backend.common.base.UOM;
import com.enhantec.wms.backend.framework.LegacyBaseService;
import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.framework.ServiceDataMap;
import com.enhantec.wms.backend.utils.common.*;

import java.math.BigDecimal;
import com.enhantec.framework.common.utils.EHContextHelper;
import java.util.*;
import java.util.stream.Collectors;

import static com.enhantec.wms.backend.utils.common.UtilHelper.trimZerosAndToStr;


public class GetAsnDetailByLPN extends LegacyBaseService
{



	/**
	--注册方法
	insert into wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION, ADDDATE, ADDWHO, EDITDATE, EDITWHO, PARAMETERS,LOAD,ISBATCH)
	values ('GetAsnDetailByLPN', 'com.enhantec.sce.inbound.receiving', 'enhantec', 'GetAsnDetailByLPN', 'TRUE', SYSDATE, 'JOHN', SYSDATE, 'JOHN'
	, 'sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,LPN,RECEIPTKEY','0.10','0');
	
	
		 */
	

	private static final long serialVersionUID = 1L;

	public GetAsnDetailByLPN()
	{
	}

	public void execute(ServiceDataHolder serviceDataHolder)
	{

		try
		{
			/*
			批属性01 包装
			批属性02 存货类型(康龙)
			E批属性03 质量状态
			批属性04 入库日期
			E批属性05 复测期
			批属性06 内部收货批号
			E批属性07 规格(康龙)
			E批属性08 供应商代码
			E批属性09 供应商批号/生产批号
			批属性10
			E批属性11 有效期
			E批属性12 取样日期/生产日期
			 */
		    String lpn= serviceDataHolder.getInputDataAsMap().getString("LPN");
			String loc= serviceDataHolder.getInputDataAsMap().getString("LOC");

			String receiptDetailSql="SELECT " +
					" A.STORERKEY, " +
					" A.TYPE, " +
					" A.ISCONFIRMED, " +
					" B.SKU, " +
					" B.TOLOC, " +
					" B.ELOTTABLE02, " +
					" B.LOTTABLE06, " +
					" B.ELOTTABLE07, " +
					" B.ELOTTABLE08, " +
					" B.ELOTTABLE09, " +
					" B.PACKKEY, " +
					" B.UOM, " +
					" B.RECEIPTKEY, " +
					" B.BARRELNUMBER," +
					" B.TOTALBARRELNUMBER," +
					" B.QTYEXPECTED, " +
					" B.GROSSWGTEXPECTED, " +
					" B.TAREWGTEXPECTED " +
					" FROM RECEIPT A,RECEIPTDETAIL B " +
					"WHERE A.RECEIPTKEY=B.RECEIPTKEY AND B.QTYEXPECTED>0 AND A.STATUS IN ('0','5') AND B.STATUS='0' AND B.TOID=? ";

			Map<String, String> lpnInfo;
			String barrelDescr="";

			lpnInfo = DBHelper.getRecord( receiptDetailSql, new Object[]{lpn}, "待收货明细行", true);

			if(UtilHelper.isEmpty(lpnInfo.get("RECEIPTKEY"))) ExceptionHelper.throwRfFulfillLogicException("未找到待收货明细行");
			barrelDescr=lpnInfo.get("BARRELNUMBER") +"/"+lpnInfo.get("TOTALBARRELNUMBER");


			if(!lpnInfo.get("ISCONFIRMED").equals("2")) ExceptionHelper.throwRfFulfillLogicException("收货单"+lpnInfo.get("RECEIPTKEY")+"未复核,不允许收货");


			String storerKey=lpnInfo.get("STORERKEY");
			String receiptKey=lpnInfo.get("RECEIPTKEY");
			String sku=lpnInfo.get("SKU");
			String qty=lpnInfo.get("QTYEXPECTED");
			String fromLoc=lpnInfo.get("TOLOC"); //这里使用ASN行上的待收货库位计算上架，实际上并未收货
			String lottable01=lpnInfo.get("LOTTABLE01"); //项目号
			String elottable02=lpnInfo.get("ELOTTABLE02"); //质量等级
			String lottable06=lpnInfo.get("LOTTABLE06"); //批号
			String elottable07=lpnInfo.get("ELOTTABLE07"); //规格
			String elottable08=lpnInfo.get("ELOTTABLE08"); //供应商
			String elottable09=lpnInfo.get("ELOTTABLE09"); //供应商批次
			String uom=lpnInfo.get("UOM");
			String packkey=lpnInfo.get("PACKKEY");
			String receiptType=lpnInfo.get("TYPE");
			String totalCount= DBHelper.getValue( "SELECT COUNT(1) AS C1 FROM RECEIPTDETAIL WHERE RECEIPTKEY=? AND SKU=? AND TOID IS NOT NULL AND TOID <>'' AND QTYEXPECTED>0"
					, new String[]{receiptKey,sku}, "0");
			int receivedCount= (int) DBHelper.getRawValue( "SELECT COUNT(1) AS C1 FROM RECEIPTDETAIL WHERE RECEIPTKEY=? AND SKU=? AND TOID IS NOT NULL AND TOID <>'' AND QTYEXPECTED>0 AND STATUS>0"
					, new Object[]{receiptKey,sku});

			String lastReceivedLoc = DBHelper.getValue( "SELECT TOP 1 TOLOC AS LOC FROM RECEIPTDETAIL WHERE RECEIPTKEY=? AND SKU=? AND QTYEXPECTED>0 AND STATUS>0 order by editdate desc"
					, new String[]{receiptKey,sku});

			String total = receivedCount +" / "+totalCount;
			//直接取库存中已存在的库区，如果相同ASN已放入多个库区，结果可能会不准,暂时保留--John
			final String toloc = !UtilHelper.isEmpty(loc)? loc: lastReceivedLoc!=null? lastReceivedLoc:"";

			Map<String,String> skuHashMap  =  SKU.findById(sku,true);
			String skuDescr=skuHashMap.get("DESCR");
			String storageconditions=skuHashMap.get("COMMODITYCLASS");
			//界面需要显示原UOM
			String GROSSWGTEXPECTED = trimZerosAndToStr(UOM.Std2UOMQty( packkey,uom,new BigDecimal(lpnInfo.get("GROSSWGTEXPECTED"))));
			String TAREWGTEXPECTED = trimZerosAndToStr(UOM.Std2UOMQty( packkey,uom,new BigDecimal(lpnInfo.get("TAREWGTEXPECTED"))));
			String NETWGTEXPECTED = trimZerosAndToStr(UOM.Std2UOMQty( packkey,uom,new BigDecimal(lpnInfo.get("QTYEXPECTED"))));
			List<String> locList = new ArrayList<>();
			String skuPutawayStrategyKey = skuHashMap.get("PUTAWAYSTRATEGYKEY");

			if ("MULTIZONES".equals(skuPutawayStrategyKey)){
				List<String> searchZoneArray=null;
				//Get SKU configured multiple zone list
				String zones= DBHelper.getValue( "SELECT SUSR7 FROM SKU WHERE storerKey=? AND SKU=?"
						, new String[] {storerKey,sku}, "");

				if(UtilHelper.isEmpty(zones)){
					ExceptionHelper.throwRfFulfillLogicException("物料上架区未配置");
				}else{
					searchZoneArray = Arrays.asList(zones.split(","));
					Collections.sort(searchZoneArray);
				}



				for(String searchZone : searchZoneArray){

					if(!UtilHelper.isEmpty(searchZone)) {

						int sepPos = searchZone.lastIndexOf("-");
						if (sepPos == -1) {
							ExceptionHelper.throwRfFulfillLogicException("上架区配置有误，格式应为:楼号-库房-库区");
						}
						//下面的loc是为了标识库房，格式为楼号-库房
						String locCandidate = searchZone.substring(0, sepPos);

						if(locList.contains(locCandidate)){ continue;}
						else {


							ServiceDataMap res = (ServiceDataMap) ServiceHelper.executeService("RFPutawayMultiZones",
									new ServiceDataHolder(
											new ServiceDataMap(
									new HashMap<String,Object>() {{

										put("storerkey", storerKey);
										put("sku", sku);
										//put("lot", lot));  还未收货，无lot值。今后可考虑改成先收到STAGE库位，再做2次收货建议。
										put("FromID", lpn);
										put("uom", uom);
										put("toqty", qty);
										put("sourcekey", receiptKey);
										put("sourcetype", receiptType);
										put("fromloc", fromLoc);
										put("zones", searchZone);
									}})));

							String toLocation = res.getString("ToLoc");

							if(!toLocation.equals("UNKNOWN")) locList.add(locCandidate);
						}
					}

				}

			}else {
				locList.add(lpnInfo.get("TOLOC"));
			}

			ServiceDataMap theOutDO = new ServiceDataMap();
			theOutDO.setAttribValue("RECEIPTKEY", receiptKey);
			theOutDO.setAttribValue("LOTTABLE01", lottable01);
			theOutDO.setAttribValue("ELOTTABLE02", elottable02);
			theOutDO.setAttribValue("LOTTABLE06", lottable06);
			theOutDO.setAttribValue("ELOTTABLE07", elottable07);
			theOutDO.setAttribValue("ELOTTABLE08", elottable08);
			theOutDO.setAttribValue("ELOTTABLE09", elottable09);
			theOutDO.setAttribValue("TOTAL", total);
			theOutDO.setAttribValue("RECEIPTTYPE", receiptType);
			theOutDO.setAttribValue("SKU", sku);
			theOutDO.setAttribValue("SKUDESCR", skuDescr);
			theOutDO.setAttribValue("UOM", uom);
			theOutDO.setAttribValue("QTY", qty);
			theOutDO.setAttribValue("LOC", toloc);
			theOutDO.setAttribValue("LOCLIST", locList.stream().collect(Collectors.joining(",")));
			theOutDO.setAttribValue("BARRELDESCR", barrelDescr);
			theOutDO.setAttribValue("GROSSWGTEXPECTED", GROSSWGTEXPECTED);
			theOutDO.setAttribValue("NETWGTEXPECTED", NETWGTEXPECTED);
			theOutDO.setAttribValue("TAREWGTEXPECTED", TAREWGTEXPECTED);
			theOutDO.setAttribValue("STORAGECONDITIONS", storageconditions);

			theOutDO.setAttribValue("BUSR9", skuHashMap.get("BUSR9"));
			theOutDO.setAttribValue("BUSR10", skuHashMap.get("BUSR10"));
			theOutDO.setAttribValue("BUSR11", skuHashMap.get("BUSR11"));

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