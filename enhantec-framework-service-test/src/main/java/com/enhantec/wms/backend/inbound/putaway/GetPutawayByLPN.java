package com.enhantec.wms.backend.inbound.putaway;

import com.enhantec.wms.backend.common.base.code.CDSysSet;
import com.enhantec.wms.backend.common.inventory.LotxLocxId;
import com.enhantec.wms.backend.framework.LegacyBaseService;
import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.framework.ServiceDataMap;
import com.enhantec.wms.backend.utils.common.*;

import com.enhantec.framework.common.utils.EHContextHelper;
import java.util.*;
import java.util.stream.Collectors;

import static com.enhantec.wms.backend.utils.common.UtilHelper.trimZerosAndToStr;

public class GetPutawayByLPN extends LegacyBaseService {

	/**
	--注册方法
	insert into wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION, ADDDATE, ADDWHO, EDITDATE, EDITWHO, PARAMETERS,LOAD,ISBATCH)
	values ('GetPutawayByLPN', 'com.enhantec.sce.inbound.putaway', 'enhantec', 'GetPutawayByLPN', 'TRUE', SYSDATE, 'JOHN', SYSDATE, 'JOHN',
	'sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,'||
	'LPN,PUTAWAYZONE','0.10','0');
		 */


	private static final long serialVersionUID = 1L;

	public GetPutawayByLPN()
	{
	}

	public void execute(ServiceDataHolder serviceDataHolder)
	{

		/*
			批属性01 项目号
			批属性02 桶号
			批属性03 质量状态
			批属性04 首次入库日期
			批属性05 复测期
			批属性06 批号
			批属性07 规格
			批属性08 供应商+供应商批次
			批属性09 入库单号
			批属性10 标签
			批属性11 有效期
			批属性12 取样日期

			INOTES.BARRELDESCR 桶号
			 */
		String userid = EHContextHelper.getUser().getUsername();

		try
		{
		    String lpn= serviceDataHolder.getInputDataAsMap().getString("LPN");
			String putawayzone= serviceDataHolder.getInputDataAsMap().getString("PUTAWAYZONE");

//		    String sql="SELECT A.STORERKEY,A.SKU,C.DESCR,C.COMMODITYCLASS STORAGECONDITIONS, C.PACKKEY, C.BUSR8,P.PACKUOM3 UOM, I.BARRELDESCR , A.LOC,A.QTY,A.QTYALLOCATED,A.QTYPICKED,A.LOT,B.LOTTABLE06,B.ELOTTABLE07,B.ELOTTABLE08,B.ELOTTABLE09,B.ELOTTABLE03,B.ELOTTABLE02,B.LOTTABLE01 "
//		    		+ "FROM LOTXLOCXID A,V_LOTATTRIBUTE B,SKU C,PACK P,IDNOTES I WHERE A.LOT=B.LOT AND A.STORERKEY=C.STORERKEY AND A.SKU=C.SKU AND C.PACKKEY = P.PACKKEY"
//		    		+ " AND A.ID=? AND A.ID =I.ID AND A.QTY>0";
//		    HashMap<String,String> lpnInfo=XtSql.GetValueMap( sql, new String[]{lpn});
//		    if (lpnInfo.isEmpty()) throw new Exception("当前容器在库存帐面已无数量");
//			String qtyAllocated=lpnInfo.get("QTYALLOCATED");
//			String qtyPicked=lpnInfo.get("QTYPICKED");
//			if (new BigDecimal(qtyAllocated).compareTo(BigDecimal.ZERO)!=0)
//				throw new Exception("当前容器已被分配,不允许移动");
//			if (new BigDecimal(qtyPicked).compareTo(BigDecimal.ZERO)!=0)
//				throw new Exception("当前容器已被拣货,不允许移动");

			//获取箱或容器可用库存信息
			HashMap<String, String> idHashMap = LotxLocxId.findFullAvailInvById(lpn,"未找到可用于上架的容器条码");

			String storerKey=idHashMap.get("STORERKEY");
			String sku=idHashMap.get("SKU");
			//boolean isProjectSku=idSuggestionHashMap.get("BUSR8")==null? false : idSuggestionHashMap.get("BUSR8").equals("1") ; //是否项目料 1：项目料 0：通用料
			String storageconditions=idHashMap.get("STORAGECONDITIONS");
			String skuDescr=idHashMap.get("SKUDESCR");
			String qty=idHashMap.get("QTY");

			String fromLoc=idHashMap.get("LOC");
			String lot=idHashMap.get("LOT");
			String elottable02=idHashMap.get("ELOTTABLE02");
			String elottable03=idHashMap.get("ELOTTABLE03"); //质量状态
			String lottable06=idHashMap.get("LOTTABLE06"); //批号
			String elottable07=idHashMap.get("ELOTTABLE07"); //规格
			String elottable08=idHashMap.get("ELOTTABLE08"); //供应商
			String elottable09=idHashMap.get("ELOTTABLE09"); //供应商批次
			String uom=idHashMap.get("UOM");
			String packKey=idHashMap.get("PACKKEY");


			String toBePutawayCount = DBHelper.getValue( "SELECT COUNT(1) FROM LOTXLOCXID A,LOC C WHERE A.LOC=C.LOC AND A.QTY>0 AND C.PUTAWAYZONE='DOCK' AND A.LOT=?", new String[]{lot}, "0");
			String allPutawayCount = DBHelper.getValue( "SELECT COUNT(1) FROM LOTXLOCXID A WHERE A.QTY>0 AND A.LOT=?", new String[]{lot}, "0");
			String totalText = toBePutawayCount + " / " + allPutawayCount;

//			String[] IDs = XtSql.GetValueList( "select GROSSWGT,TAREWGT,NETWGT from IDNOTES where ID=?", new String[]{lpn});
//			String weight = "";
//			if (IDs != null) {
//				weight = trimZerosAndToStr(XtUtils.Nz(idHashMap.get("GROSSWGT"), "0")) + XtUtils.Nz(uom, "")
//						+ " / " + trimZerosAndToStr(XtUtils.Nz(idHashMap.get("TAREWGT"), "0")) + XtUtils.Nz(uom, "")
//						+ " / " + trimZerosAndToStr(XtUtils.Nz(idHashMap.get("NETWGT"), "0")) + XtUtils.Nz(uom, "");
//			}

			String weight;
			if(CDSysSet.enableLabelWgt()) {
				weight = trimZerosAndToStr(LegecyUtilHelper.Nz(idHashMap.get("GROSSWGT"), "0")) + LegecyUtilHelper.Nz(uom, "")
						+ " / " + trimZerosAndToStr(LegecyUtilHelper.Nz(idHashMap.get("TAREWGT"), "0")) + LegecyUtilHelper.Nz(uom, "")
						+ " / " + trimZerosAndToStr(LegecyUtilHelper.Nz(idHashMap.get("NETWGT"), "0")) + LegecyUtilHelper.Nz(uom, "");
			}else{
				weight = trimZerosAndToStr(LegecyUtilHelper.Nz(idHashMap.get("NETWGT"),"0"))+ LegecyUtilHelper.Nz(uom,"");
			}

			String isMultizones= serviceDataHolder.getInputDataAsMap().getString("ISMULTIZONES");

			String toLoc = "";
			String toZone = "";
			String TOZONELIST = "";
			String TOZONELIST1 = "";

			if ("Y".equals(isMultizones)) {
				String count = (String) DBHelper.getValue( "SELECT COUNT(1) FROM LOC WHERE PUTAWAYZONE='DOCK' AND LOC=?", new Object[]{fromLoc}, "0");
				if (count.equals("0")) throw new Exception("未找到库房 " + fromLoc + "，请确认该库房隶属于待上架区（DOCK）");

				//项目料不做上架建议
				//if (!isProjectSku)
				{

					List<String> searchZoneArray = null;
					//Get SKU configured multiple zone list
					String zones = DBHelper.getValue( "SELECT SUSR7 FROM SKU WHERE storerKey=? AND SKU=?"
							, new String[]{storerKey, sku}, "");

					if (UtilHelper.isEmpty(zones)) {
						ExceptionHelper.throwRfFulfillLogicException("物料上架区未配置");
					} else {
						searchZoneArray = Arrays.asList(zones.split(","));
						Collections.sort(searchZoneArray);
					}

					//可使用的库区列表
					final List<String> candidateZoneList = new ArrayList<>();

					for (String searchZone : searchZoneArray) {

						if (!UtilHelper.isEmpty(searchZone)) {

							int sepPos = searchZone.lastIndexOf("-");
							if (sepPos == -1) {
								ExceptionHelper.throwRfFulfillLogicException("上架区配置有误，格式应为:楼号-库房-库区");
							}
							//下面的loc是为了标识库房，格式为楼号-库房
							String loc = searchZone.substring(0, sepPos);
							//获得当前收货库房fromLoc对应的上架区列表
							if (fromLoc.equals(loc)) {
								candidateZoneList.add(searchZone);
							}
						}
					}

					if (candidateZoneList.size() == 0) ExceptionHelper.throwRfFulfillLogicException("未找到此物料对应库房的上架区配置");

					if (!UtilHelper.isEmpty(putawayzone)) {//上架库区已确定，只在该库区寻找库位

						if (!candidateZoneList.contains(putawayzone))
							ExceptionHelper.throwRfFulfillLogicException("该物料的上架配置不包含此库区");

						candidateZoneList.clear();
						candidateZoneList.add(putawayzone);
					}

					ServiceDataMap res = (ServiceDataMap) ServiceHelper.executeService( "RFPutawayMultiZones",
						new ServiceDataHolder(new ServiceDataMap(new HashMap<String, Object>() {{
								put("storerkey", storerKey);
								put("sku", sku);
								put("lot", lot);
								put("FromID", idHashMap.get("ID"));
								put("uom", uom);
								put("toqty", qty);
								//put("sourcekey", receiptKey));
								//put("sourcetype", receiptType));
								put("fromloc", fromLoc);
								put("zones", candidateZoneList.stream().collect(Collectors.joining(",")));
							}})));


					toLoc = res.getString("loc");
					toZone = res.getString("zone");

					for (int i = 0; i < candidateZoneList.size(); i++) {
						if (i != 0) {
							TOZONELIST += ",";
							TOZONELIST1 += ",";
						}
						TOZONELIST += candidateZoneList.get(i);
						TOZONELIST1 += DBHelper.getValue( "SELECT DESCR FROM PUTAWAYZONE WHERE PUTAWAYZONE=?", new String[]{candidateZoneList.get(i)}, candidateZoneList.get(i));
					}

				}

			}else {
				ServiceDataMap res = (ServiceDataMap) ServiceHelper.executeService( "RFPutawayP1S1Wrapper",
						new ServiceDataHolder(new ServiceDataMap(new HashMap<String, Object>() {{
							put("storerkey", storerKey);
							put("sku", sku);
							put("lot", lot);
							put("FromID", idHashMap.get("ID"));
							put("uom", uom);
							put("toqty", qty);
							//put("sourcekey", receiptKey));
							//put("sourcetype", receiptType));
							put("fromloc", fromLoc);
						}})));
				toLoc=res.getString("ToLoc");
			}




		    //--------------------------------------------------------------------------
			

			ServiceDataMap theOutDO = new ServiceDataMap();

			theOutDO.setAttribValue("BARRELDESCR", idHashMap.get("BARRELDESCR"));
			//theOutDO.setAttribValue("ISPROJECTSKU", isProjectSku));
			theOutDO.setAttribValue("TOTAL", totalText);
			theOutDO.setAttribValue("ELOTTABLE02", elottable02);
			theOutDO.setAttribValue("ELOTTABLE03", elottable03);
			theOutDO.setAttribValue("LOTTABLE06", lottable06);
			theOutDO.setAttribValue("ELOTTABLE07", elottable07);
			theOutDO.setAttribValue("ELOTTABLE08", elottable08);
			theOutDO.setAttribValue("ELOTTABLE09", elottable09);

			//theOutDO.setAttribValue("BARRELDESCR", BARRELDESCR);
			theOutDO.setAttribValue("STORERKEY", storerKey);
			theOutDO.setAttribValue("SKU", sku);
			theOutDO.setAttribValue("SKUDESCR", skuDescr);
			theOutDO.setAttribValue("UOM", uom);
			theOutDO.setAttribValue("PACKKEY", packKey);
			theOutDO.setAttribValue("QTY", trimZerosAndToStr(idHashMap.get("QTY")));
			theOutDO.setAttribValue("FROMLOC", fromLoc);
			theOutDO.setAttribValue("LOT", lot);
			theOutDO.setAttribValue("TOZONE", toZone);
			theOutDO.setAttribValue("TOZONELIST", TOZONELIST);
			theOutDO.setAttribValue("TOZONELIST1", TOZONELIST1);
			theOutDO.setAttribValue("TOLOC", toLoc);
			theOutDO.setAttribValue("LASTSHIPPEDLOC", idHashMap.get("LASTSHIPPEDLOC"));
			theOutDO.setAttribValue("WEIGHT", weight);
			theOutDO.setAttribValue("STORAGECONDITIONS", storageconditions);


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