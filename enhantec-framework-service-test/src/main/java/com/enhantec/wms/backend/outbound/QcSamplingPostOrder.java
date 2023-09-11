package com.enhantec.wms.backend.outbound;

import com.enhantec.framework.common.utils.EHContextHelper;
import com.enhantec.wms.backend.utils.common.LegacyDBHelper;
import com.enhantec.wms.backend.common.base.CodeLookup;
import com.enhantec.wms.backend.common.base.IDNotes;
import com.enhantec.wms.backend.common.base.code.CDSysSet;
import com.enhantec.wms.backend.framework.WMSBaseService;
import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.framework.ServiceDataMap;
import com.enhantec.wms.backend.utils.audit.Udtrn;
import com.enhantec.wms.backend.utils.common.*;
import com.enhantec.wms.backend.utils.print.Labels;
import com.enhantec.wms.backend.utils.print.PrintHelper;

import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class QcSamplingPostOrder extends WMSBaseService
{



	/**
	 *JOHN
	//  出库取样扣减库存
	--注册方法
	delete from SCPRDMST.wmsadmin.sproceduremap  where COMPOSITE='QcSamplingPostOrder';
	insert into SCPRDMST.wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION, ADDWHO, EDITWHO, PARAMETERS,LOAD,ISBATCH)
	values ('QcSamplingPostOrder', 'com.enhantec.sce.outbound.order', 'enhantec', 'QcSamplingPostOrder', 'TRUE', 'JOHN', 'JOHN', 'sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,LOTTABLE06,ORDERKEY,ORDERTYPE,KEEPQTY,PRINTER,ESIGNATUREKEY','0.10','0');
	*/
	

	private static final long serialVersionUID = 1L;

	public QcSamplingPostOrder()
	{
	}

	public void execute(ServiceDataHolder serviceDataHolder)
	{
		
		String userid = EHContextHelper.getUser().getUsername();
		//context.theSQLMgr.transactionBegin();


		try
		{
			
		    String LOTTABLE06= serviceDataHolder.getInputDataAsMap().getString( "LOTTABLE06");
			String ORDERKEY= serviceDataHolder.getInputDataAsMap().getString( "ORDERKEY");
			String ORDERTYPE= serviceDataHolder.getInputDataAsMap().getString( "ORDERTYPE");
			String KEEPQTY= serviceDataHolder.getInputDataAsMap().getString( "KEEPQTY");
			String PRINTER= serviceDataHolder.getInputDataAsMap().getString( "PRINTER");
			String ESIGNATUREKEY= serviceDataHolder.getInputDataAsMap().getString( "ESIGNATUREKEY");
			String NewPrg=null;
			Map<String,String>  orderHashMap= DBHelper.getRecord( "select Status,Notes, STORERKEY from orders where orderkey=? and ohtype=?", new String[]{ORDERKEY,ORDERTYPE},"未找到在库取样单("+ORDERKEY+")");

			if (orderHashMap.get("Status").compareTo("90")>=0)  throw new Exception("在库取样单("+ORDERKEY+")已关闭,不能继续操作");

			String storerKey = orderHashMap.get("STORERKEY");

			String iPrint= DBHelper.getStringValue( "select count(1) from orderdetail where orderkey=? and openqty<>0", new String[]{ORDERKEY}, "0");
			if (!iPrint.equals("0"))
				if (LegecyUtilHelper.isNull(PRINTER))
					 throw new Exception("有扣减数量,必须选择打印机");

			String totalOriginalQty= DBHelper.getStringValue( "select SUM(CONVERT(decimal(11,5), SUSR1))  from ORDERDETAIL where orderkey=?",new String[]{ORDERKEY},"0");

			if(UtilHelper.decimalStrCompare(KEEPQTY,
					totalOriginalQty)>0) throw new Exception("留样量不允许大于取样量");

			List<Map<String,String>> Details= DBHelper.executeQuery( "select ORDERLINENUMBER,STORERKEY,SKU,IDREQUIRED,LOTTABLE06,SUSR1,SUSR3,STATUS,PACKKEY, UOM"
							+ ",ORIGINALQTY,OPENQTY,SHIPPEDQTY,QTYPREALLOCATED,QTYALLOCATED,QTYPICKED,UOM,PACKKEY "
							+ " from orderdetail where orderkey=? order by orderlinenumber", new String[]{ORDERKEY});
			String[] IDS=new String[Details.size()];
			String[] SKUs=new String[Details.size()];

			Map<String,String> samplePackInfo=null;

			BigDecimal qyTotalQty = BigDecimal.ZERO; //总取样量

			for(int iDetail=0;iDetail<Details.size();iDetail++)
			{
				Map<String,String> mDetail=Details.get(iDetail);
//				String ORDERLINENUMBER=mDetail.get("ORDERLINENUMBER");
//				String STORERKEY=mDetail.get("STORERKEY");
				String SKU=mDetail.get("SKU");
				String ID=mDetail.get("IDREQUIRED");
				String OPENQTY=mDetail.get("ORIGINALQTY");//扣样量

				qyTotalQty = qyTotalQty.add(new BigDecimal(mDetail.get("SUSR1")));

				IDNotes.decreaseWgtByIdWithAvailQtyCheck(ID,OPENQTY,"扣样量");

				IDS[iDetail]=ID;
				SKUs[iDetail]=SKU;

				if(samplePackInfo==null) {
					samplePackInfo = DBHelper.getRecord(
							"select c.PACKKEY, c.PACKUOM3 UOM, s.SKU SKU, s.DESCR SKUDESCR " +
									" from pack c, SKU s " +
									" where c.PACKKEY = s.SUSR6 and s.STORERKEY = ? and s.sku = ? ",
							new Object[]{storerKey, SKU}, SKU + "的取样包装配置", true);
				}

				//插入取样后剩余量标签
				Map<String,String> leftIdNotesHashMap = IDNotes.findById(ID,true);

				//扣样后剩余量>0并且扣样量>0，打印剩余量标签
				if(UtilHelper.decimalStrCompare(leftIdNotesHashMap.get("NETWGT"), "0")>0
						&& UtilHelper.decimalStrCompare(OPENQTY, "0")>0 && CDSysSet.enableLabelWgt()) {
					PrintHelper.printLPNByIDNotes( ID, Labels.LPN_UI_SY, PRINTER, "1", "取样单_剩余量标签");
				}

				//插入取样标签，一个无重量的取样标签

				Map<String,String> existIDNotes = IDNotes.findById(ID,true);
				String qyLpn = IdGenerationHelper.generateLpn( LOTTABLE06+"QY");
				//取样出库单里的susr1记录了取样的取样标签的重量信息。
				Map<String,String> qyIdnotes = new HashMap<>();
				qyIdnotes.put("AddWho", userid);
				qyIdnotes.put("EditWho", userid);
				qyIdnotes.put("ID", qyLpn);//取样流水号
				qyIdnotes.put("FROMID", ID);//来源容器LPN(目前用于取样)
				qyIdnotes.put("GROSSWGT", "0");//毛重
				qyIdnotes.put("TAREWGT", "0");//皮重
				qyIdnotes.put("NETWGT", "0");//净重
				qyIdnotes.put("ORIGINALGROSSWGT", mDetail.get("SUSR1"));//毛重
				qyIdnotes.put("ORIGINALTAREWGT", "0");//皮重
				qyIdnotes.put("ORIGINALNETWGT", mDetail.get("SUSR1"));//净重
				qyIdnotes.put("STORERKEY", existIDNotes.get("STORERKEY"));
				qyIdnotes.put("SKU", existIDNotes.get("SKU"));//物料代码
				qyIdnotes.put("PACKKEY", samplePackInfo.get("PACKKEY"));//取样包装
				qyIdnotes.put("UOM", samplePackInfo.get("UOM"));//；取样计量单位
				qyIdnotes.put("BARRELNUMBER", qyLpn.substring(qyLpn.length()-5));//桶号
				qyIdnotes.put("TOTALBARREL", existIDNotes.get("TOTALBARREL"));//总桶号
				qyIdnotes.put("BARRELDESCR", qyLpn.substring(qyLpn.length()-5) + " / " + existIDNotes.get("TOTALBARREL"));//桶描述
				qyIdnotes.put("LOT", existIDNotes.get("LOT"));
				qyIdnotes.put("PROJECTCODE", existIDNotes.get("PROJECTCODE"));
				qyIdnotes.put("ISOPENED", "0");
				qyIdnotes.put("ORDERKEY", ORDERKEY);
				LegacyDBHelper.ExecInsert( "IDNOTES", qyIdnotes);
				PrintHelper.printLPNByIDNotes( qyLpn,Labels.LPN_SAMPLE, PRINTER, "1","取样单_取样标签");
			}

			//插入留样容器的IDNOTES，重量信息是传进来的KEEPQTY(留样重量)
			Map<String, String> existIDNotes = IDNotes.findById( IDS[0], true);

			if(UtilHelper.decimalStrCompare(KEEPQTY, "0")>0) {

				String lyLpn = IdGenerationHelper.generateLpn( LOTTABLE06 + "LY");

				Map<String,String> lyIDNOTES = new HashMap<String,String>();
				lyIDNOTES.put("AddWho", userid);
				lyIDNOTES.put("EditWho", userid);
				lyIDNOTES.put("ID", lyLpn);//留样LPN
				lyIDNOTES.put("GROSSWGT", "0");//毛重
				lyIDNOTES.put("TAREWGT", "0");//皮重
				lyIDNOTES.put("NETWGT", "0");//净重
				lyIDNOTES.put("ORIGINALGROSSWGT", KEEPQTY);//毛重
				lyIDNOTES.put("ORIGINALTAREWGT", "0");//皮重
				lyIDNOTES.put("ORIGINALNETWGT", KEEPQTY);//净重
				lyIDNOTES.put("STORERKEY", existIDNotes.get("STORERKEY"));
				lyIDNOTES.put("SKU", existIDNotes.get("SKU"));//物料代码

				lyIDNOTES.put("PACKKEY", samplePackInfo.get("PACKKEY"));//取样包装
				lyIDNOTES.put("UOM", samplePackInfo.get("UOM"));//；取样计量单位
				lyIDNOTES.put("BARRELNUMBER", lyLpn.substring(lyLpn.length() - 5));//桶号
				lyIDNOTES.put("TOTALBARREL", existIDNotes.get("TOTALBARREL"));//总桶号
				lyIDNOTES.put("BARRELDESCR", lyLpn.substring(lyLpn.length() - 5) + " / " + existIDNotes.get("TOTALBARREL"));//桶描述
				lyIDNOTES.put("LOT", existIDNotes.get("LOT"));
//			IDNOTES.put("LOTTABLE01", existIDNotes.get("LOTTABLE01"));
//			IDNOTES.put("LOTTABLE02", existIDNotes.get("LOTTABLE02"));
//			IDNOTES.put("LOTTABLE03", existIDNotes.get("LOTTABLE03"));
//			IDNOTES.put("LOTTABLE04", existIDNotes.get("LOTTABLE04"));
//			IDNOTES.put("LOTTABLE05", existIDNotes.get("LOTTABLE05"));
//			IDNOTES.put("LOTTABLE06", existIDNotes.get("LOTTABLE06"));
//			IDNOTES.put("LOTTABLE07", existIDNotes.get("LOTTABLE07"));
//			IDNOTES.put("LOTTABLE08", existIDNotes.get("LOTTABLE08"));
//			IDNOTES.put("LOTTABLE09", existIDNotes.get("LOTTABLE09"));
//			IDNOTES.put("LOTTABLE10", existIDNotes.get("LOTTABLE10"));
//			IDNOTES.put("LOTTABLE11", existIDNotes.get("LOTTABLE11"));
//			IDNOTES.put("LOTTABLE12", existIDNotes.get("LOTTABLE12"));
				lyIDNOTES.put("PROJECTCODE", existIDNotes.get("PROJECTCODE"));
				lyIDNOTES.put("ISOPENED", "0");
				lyIDNOTES.put("ORDERKEY", ORDERKEY);
				LegacyDBHelper.ExecInsert( "IDNOTES", lyIDNOTES);

				//自动生成留样入库单

				ServiceHelper.executeService( "EHReturnCreateAsn",
						new ServiceDataHolder(new ServiceDataMap(new HashMap<String,Object>() {{
							put("LPN", lyLpn);
							put("RECTYPE", CDSysSet.getSampleReceiptType());
							put("GROSSWGT", KEEPQTY);
							put("TAREWGT", "0");
							put("NETWGT", KEEPQTY);
							put("ISOPENED", "0");
							put("ESIGNATUREKEY", ESIGNATUREKEY);
							put("PRINTER", PRINTER);
						}}))

				);

				PrintHelper.printLPNByIDNotes( lyLpn, Labels.SAMPLE_LY, PRINTER, "1","取样单_留样标签");

				//更新留样容器号、留样量
				DBHelper.executeUpdate( "UPDATE orders SET TRADINGPARTNER = ? , SUSR4 = ? where orderkey = ? ", new Object[]{lyLpn ,KEEPQTY, ORDERKEY});

			}

			//样品编号生成
			/*
				每个批次（取样单）打印1张样品标签。
				样品编号需要系统自动生成存储在ORDERS.SUSR3。
				规则：AN-MANB+流水，流水在每次打印后+1.
				样品编号前缀配置CODELKUP,LISTNAME=SYSSET,CODE=SAMPLEPRE,UDF1=AN-MANB
				取值字段如下：
				样品名称：SKU.DESCR  样品批号:ORDERS. EXTERNORDERKEY
				样品编号：ORDERS.SUSR3

			 */
			Map<String,String> sampleLpnPrefix = CodeLookup.getCodeLookupByKey( "SYSSET","SAMPLEPRE");
			String prefix = sampleLpnPrefix.get("UDF1");
			String ypLpn = IdGenerationHelper.generateID(userid,prefix,1);


			//插入样品标签，会插入一个无重量的样品标签
			Map<String,String> ypIdnotes = new HashMap<String,String>();
			ypIdnotes.put("AddWho", userid);
			ypIdnotes.put("EditWho", userid);
			ypIdnotes.put("ID", ypLpn);//留样LPN
			ypIdnotes.put("GROSSWGT", "0");//毛重
			ypIdnotes.put("TAREWGT", "0");//皮重
			ypIdnotes.put("NETWGT", "0");//净重
			ypIdnotes.put("ORIGINALGROSSWGT", qyTotalQty.subtract(new BigDecimal(KEEPQTY)).toPlainString());//毛重
			ypIdnotes.put("ORIGINALTAREWGT", "0");//皮重
			ypIdnotes.put("ORIGINALNETWGT", qyTotalQty.subtract(new BigDecimal(KEEPQTY)).toPlainString());//净重
			ypIdnotes.put("STORERKEY", existIDNotes.get("STORERKEY"));
			ypIdnotes.put("SKU", existIDNotes.get("SKU"));//物料代码

			ypIdnotes.put("PACKKEY", samplePackInfo.get("PACKKEY"));//取样包装
			ypIdnotes.put("UOM", samplePackInfo.get("UOM"));//；取样计量单位
			ypIdnotes.put("BARRELNUMBER", ypLpn);//桶号  样品LPN为 AN-MANB+2位数字，目前暂时将该LPN用作桶号
			ypIdnotes.put("TOTALBARREL", existIDNotes.get("TOTALBARREL"));//总桶号
			ypIdnotes.put("BARRELDESCR", ypLpn + " / " + existIDNotes.get("TOTALBARREL"));//桶描述
			ypIdnotes.put("LOT", existIDNotes.get("LOT"));
			ypIdnotes.put("PROJECTCODE", existIDNotes.get("PROJECTCODE"));
			ypIdnotes.put("ISOPENED", "0");
			ypIdnotes.put("ORDERKEY", ORDERKEY);
			LegacyDBHelper.ExecInsert( "IDNOTES", ypIdnotes);

			//更新样品标签流水号
			DBHelper.executeUpdate( "UPDATE orders SET SUSR3 = ? where orderkey = ? ", new Object[]{ ypLpn, ORDERKEY});
			//PrintHelper.printSamplingLpnLabel( ORDERKEY,Labels.SAMPLE_YP, PRINTER, "1","取样单_样品标签");
			PrintHelper.printLPNByIDNotes( ypLpn,Labels.SAMPLE_YP, PRINTER, "1","取样单_样品标签");


			Udtrn UDTRN=new Udtrn();
			UDTRN.EsignatureKey=ESIGNATUREKEY;
			UDTRN.FROMTYPE="在库取样-执行记帐";
			UDTRN.FROMTABLENAME="ORDERS";
			UDTRN.FROMKEY=LOTTABLE06;
			UDTRN.FROMKEY1=ORDERKEY;
			UDTRN.FROMKEY2="";
			UDTRN.FROMKEY3="";
			UDTRN.TITLE01="取样批次";    UDTRN.CONTENT01=LOTTABLE06;
			UDTRN.TITLE02="出库单号";    UDTRN.CONTENT02=ORDERKEY;
			UDTRN.Insert( userid);


			//call system api to do allocate and ship here to avoid transaction rollback by other issues, like incorrect label sql etc...
			// as the pickdetail record still there after system api rollback.
			//最后掉用减少发运订单可能是因为，使用系统api发运后如果出现异常回滚，pickdetail依然存在，放在后面减小异常概率
			OutboundUtils.allocateAndShip( ORDERKEY,false);
			//更新取样时间
			for (int iDetail = 0; iDetail < Details.size(); iDetail++) {
				Map<String,String> mDetail = Details.get(iDetail);
				String id = mDetail.get("IDREQUIRED");
				DBHelper.executeUpdate(
						"UPDATE IDNOTES SET INSPECTIONDATE = ? WHERE ID = ? ", new ArrayList<Object>() {
							{   add(UtilHelper.getCurrentSqlDate());
								add(id);
							}
						});
			}

//			context.theSQLMgr.transactionCommit();
			//-------------------------------------
//
//			EXEDataObject theOutDO = new EXEDataObject();
//			theOutDO.clearDO();
//			theOutDO.setRow(theOutDO.createRow());


			ServiceDataMap theOutDO = new ServiceDataMap();
			theOutDO.setAttribValue("ORDERKEY", ORDERKEY);
			theOutDO.setAttribValue("NOTES", NewPrg);

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