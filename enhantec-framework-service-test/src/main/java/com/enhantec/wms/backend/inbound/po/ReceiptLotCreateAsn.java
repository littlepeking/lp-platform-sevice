package com.enhantec.wms.backend.inbound.po;

import com.enhantec.wms.backend.common.Const;
import com.enhantec.wms.backend.common.base.CodeLookup;
import com.enhantec.wms.backend.common.base.SKU;
import com.enhantec.wms.backend.common.base.code.CDQualityStatus;
import com.enhantec.wms.backend.common.base.code.CDSysSet;
import com.enhantec.wms.backend.framework.LegacyBaseService;
import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.framework.ServiceDataMap;
import com.enhantec.wms.backend.inbound.asn.utils.ReceiptUtilHelper;
import com.enhantec.wms.backend.utils.audit.ESignatureService;
import com.enhantec.wms.backend.utils.audit.Udtrn;
import com.enhantec.wms.backend.utils.common.*;
import com.enhantec.wms.backend.utils.print.Labels;
import com.enhantec.wms.backend.utils.print.PrintHelper;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.*;
import java.util.stream.Collectors;

import static com.enhantec.wms.backend.common.base.LotxId.buildReceiptLotxIdInfo;
import static com.enhantec.wms.backend.utils.common.UtilHelper.trimZerosAndToStr;


public class ReceiptLotCreateAsn extends LegacyBaseService
{



	/**
	 *
	--注册方法
	
	insert into wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION, ADDWHO, EDITWHO, PARAMETERS,LOAD,ISBATCH)
	values ('ReceiptLotCreateAsn', 'com.enhantec.sce.inbound.po', 'enhantec', 'ReceiptLotCreateAsn', 'TRUE',  'JOHN',  'JOHN',
	'sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,PRINTER,RECEIPTLOT,PACKKEY,UOM,CONVERSIONRATE,GROSSWGT,TAREWGT,NETWGT,GROSSWGTLABEL,TAREWGTLABEL,NETWGTLABEL,BARRELQTY,NOTE,SUMNETWGT,SUMBARRELQTY,ESIGNATUREKEY','0.10','0');
		 */

	private static final long serialVersionUID = 1L;

	public ReceiptLotCreateAsn()
	{
	}

	public void execute(ServiceDataHolder serviceDataHolder)
	{
		/*
		 0-检查收货检查表状态,以及检查结果
		 1-检查总桶数净重与各桶的合计
		 2-检查PO    (确定PO明细行与数量,PO负数量的处理,确定锁定量)
		 3-取LOTTABLE
		 4-生成 桶标签 桶号 (零头桶生成第1桶,其它按录入顺序)
		 5-写入数据库
		 	5.1    WMS_PO    PO表
			5.3    PRERECEIPTCHECK    收货检查表
			5.4    PRCPO	收货检查PO对照表
			5.5    RECEIPT    RECEIPTDETAIL   ASN表
			5.8    IDNOTES   写入桶号,总桶数 ,数量=0
		 
		 */
		
		String userid = context.getUserID();

		Connection conn = context.getConnection();

		try
		{
			/* *
			 	KR10	采购入库（原料入库）
				KR11	车间留样入库
				KR12	车间入库（终产品/中间体）
				KR13	车间退库（原料/中间体退库）
				KR14	客户退货
				KR15	成品召回
				KR16	调拨入库
				KR17	盘盈入库
				KR18	杂项入库
			* */




		    String PRINTER= serviceDataHolder.getInputDataAsMap().getString("PRINTER");
		    String RECEIPTLOT= serviceDataHolder.getInputDataAsMap().getString("RECEIPTLOT");
			//String PACKKEY=processData.getInputDataMap().getString("PACKKEY");
			//String UOM=processData.getInputDataMap().getString("UOM");
			//String CONVERSIONRATE=processData.getInputDataMap().getString("CONVERSIONRATE");
			
			String GROSSWGT= serviceDataHolder.getInputDataAsMap().getString("GROSSWGT");
			String TAREWGT= serviceDataHolder.getInputDataAsMap().getString("TAREWGT");
			String NETWGT= serviceDataHolder.getInputDataAsMap().getString("NETWGT");
			
			String GROSSWGTLABEL= serviceDataHolder.getInputDataAsMap().getString("GROSSWGTLABEL");
			String TAREWGTLABEL= serviceDataHolder.getInputDataAsMap().getString("TAREWGTLABEL");
			String NETWGTLABEL= serviceDataHolder.getInputDataAsMap().getString("NETWGTLABEL");

			String BARRELQTY= serviceDataHolder.getInputDataAsMap().getString("BARRELQTY");
			String NOTE= serviceDataHolder.getInputDataAsMap().getString("NOTE");
			String SUMNETWGT= serviceDataHolder.getInputDataAsMap().getString("SUMNETWGT");//预期总净重
			String SUMBARRELQTY= serviceDataHolder.getInputDataAsMap().getString("SUMBARRELQTY");//预期总桶数
			String ESIGNATUREKEY= serviceDataHolder.getInputDataAsMap().getString("ESIGNATUREKEY");
			String qualifiedproducer ="";
			String RECEIPTKEY="";
			List<HashMap<String,String>> receiptypelist = DBHelper.executeQuery(context, conn, "select c.UDF1 from PRERECEIPTCHECK p ,WMS_PO wp,CODELKUP c where p.FROMKEY =wp.POKEY" +
					"    and c.CODE =wp.POTYPE and c.LISTNAME ='EHPOTYPE' and p.RECEIPTLOT=? ", Arrays.asList(new Object[]{RECEIPTLOT}));
			//String RECEIPTYPE= CDSysSet.getPOReceiptType(context,conn); //	采购入库（原料入库）
			if (receiptypelist.isEmpty()) throw new Exception("入库类型未配置,无法创建ASN");
			String RECEIPTYPE= receiptypelist.get(0).get("UDF1"); //	采购入库（原料入库）
			if (LegecyUtilHelper.isNull(ESIGNATUREKEY)) throw new Exception("未关联到签名信息");
			//String[] aPACKKEY=PACKKEY.split(";");
			String[] aGROSSWGT=GROSSWGT.split(";");
			String[] aTAREWGT=TAREWGT.split(";");
			String[] aNETWGT=NETWGT.split(";");
			String[] aGROSSWGTLABEL=GROSSWGTLABEL.split(";");
			String[] aTAREWGTLABEL=TAREWGTLABEL.split(";");
			String[] aNETWGTLABEL=NETWGTLABEL.split(";");
			String[] aBARRELQTY=BARRELQTY.split(";");
			String[] aNOTE=NOTE.split(";");
//			if (aPACKKEY.length==0)
//				throw new Exception("未成功获取包装数据");
			if (aGROSSWGT.length==0) 
				throw new Exception("未成功获取毛重数据");
			if (aGROSSWGT.length!=aTAREWGT.length) 
				throw new Exception("未成功获取皮重数据");
			if (aGROSSWGT.length!=aNETWGT.length) 
				throw new Exception("未成功获取净重数据");
			if (aGROSSWGT.length!=aBARRELQTY.length) 
				throw new Exception("未成功获取桶数量数据");
//			if (aGROSSWGT.length!=aPACKKEY.length)
//				throw new Exception("未成功获取包装数据");
			if (aGROSSWGT.length!=aNOTE.length) 
				throw new Exception("未成功获取备注数据");
			
			int sumBarrelQty=0;
			BigDecimal sumnetwgt=BigDecimal.ZERO;
			for(int i1=0;i1<aBARRELQTY.length;i1++)
			{
				sumBarrelQty+=Integer.parseInt(aBARRELQTY[i1]);
				BigDecimal b1=new BigDecimal(aBARRELQTY[i1]);
				BigDecimal b2=new BigDecimal(aNETWGT[i1]);
				BigDecimal b3=b1.multiply(b2);
				sumnetwgt=sumnetwgt.add(b3);
			}
			int totalBarrelQty=Integer.parseInt(SUMBARRELQTY);
			/*if (sumBarrelQty>totalBarrelQty)
		        throw new Exception("桶数量合计不能大于总桶数");*/
			//BigDecimal bSUMNETWGT=new BigDecimal(SUMNETWGT).multiply(new BigDecimal(CONVERSIONRATE));
			BigDecimal totalNetWeight=new BigDecimal(SUMNETWGT);
			if (sumnetwgt.compareTo(totalNetWeight)>0)
		        throw new Exception("净重合计不允许大于预期净重");
			
			
			
			String STORERKEY= LegacyDBHelper.GetValue(context, conn, "select udf1 from codelkup where listname=? and code=?", new String[]{"SYSSET","STORERKEY"}, "");

			//ASN由小到大扣减PO库存
			ArrayList<LinkedHashMap<String,String>> aPOKEY= LegacyDBHelper.GetRecordMap(context, conn,
					 "select FROMKEY,FROMLINENO,FILECHECK,SUPPLIERCHECK,PACKCHECK,WEIGHTCHECK,STATUS,SKU,FROMLOT," +
							 " SUPPLIERCODE,SUPPLIERNAME,MANUFACTURERCODE,MANUFACTURERNAME,FROMSKU,FROMSKUDESCR," +
							 " POSUPPLIERCODE,POSUPPLIERNAME,ELOTTABLE07," +
								 " FORMAT(ELOTTABLE11,'"+ Const.DateTimeFormat+"') as ELOTTABLE11,qualifiedproducer "+
							 " from PRERECEIPTCHECK where RECEIPTLOT=? ORDER BY SERIALKEY ",new String[]{RECEIPTLOT});

			if (aPOKEY==null) throw new Exception("未找到收货检查记录");
			String sku=null;
			String FROMLOT=null;
			String POSUPPLIERCODE=null;
			String POSUPPLIERNAME=null;
			//String SUPPLIERCODE=null;
			//String SUPPLIERNAME=null;
			String MANUFACTURERCODE=null;//哪里取值??
			String MANUFACTURERNAME=null;//哪里取值??
			//String FROMSKU=null;
			//String FROMSKUDESCR=null;
//			String DISTRIBUTIONSITE=null;
//			String PROJECTCODE=null;
//			String SAMPLEQTY=null;

			String LOTTABLE02=null;//存货类型
			String ELOTTABLE07=null;
			String ELOTTABLE11=null;
			String ELOTTABLE06=null;

			ArrayList<UsedPOBean> usedPOBeans=new ArrayList<UsedPOBean>();
			
			//ArrayList<PO_bean> Run_PP=new ArrayList<PO_bean>();
			for(int iPO=0;(iPO<aPOKEY.size())&&(sumnetwgt.compareTo(BigDecimal.ZERO)>0);iPO++)
			{
				LinkedHashMap<String,String> mPOKEY=aPOKEY.get(iPO);
				String POKEY=mPOKEY.get("FROMKEY");
				String FROMLINENO=mPOKEY.get("FROMLINENO");
				String STATUS=mPOKEY.get("STATUS");
				sku=mPOKEY.get("SKU");
				FROMLOT=mPOKEY.get("FROMLOT");
				POSUPPLIERCODE=mPOKEY.get("POSUPPLIERCODE");
				POSUPPLIERNAME=mPOKEY.get("POSUPPLIERNAME");
				MANUFACTURERCODE=mPOKEY.get("MANUFACTURERCODE");
				MANUFACTURERNAME=mPOKEY.get("MANUFACTURERNAME");
				//FROMSKU=mPOKEY.get("FROMSKU");
				//FROMSKUDESCR=mPOKEY.get("FROMSKUDESCR");
				ELOTTABLE07=mPOKEY.get("ELOTTABLE07");
				ELOTTABLE11=mPOKEY.get("ELOTTABLE11");
				qualifiedproducer=mPOKEY.get("qualifiedproducer");
				if (!STATUS.equals("0")) throw new Exception("当前状态已不能生成ASN");
				
				UsedPOBean tempUsedPOBean=new UsedPOBean(context, conn,POKEY,FROMLINENO,sku);
				tempUsedPOBean.NAMEALPHA=mPOKEY.get("POSUPPLIERNAME");
				BigDecimal bClacQty = tempUsedPOBean.ClacByAsn(sumnetwgt);
				
				sumnetwgt=sumnetwgt.subtract(bClacQty);
				usedPOBeans.add(tempUsedPOBean);
			}
			if (sumnetwgt.compareTo(BigDecimal.ZERO)>0) throw new Exception("PO可用数量不足");


			HashMap<String,String> skuInfo= SKU.findById(context,conn,sku,true);


			LinkedHashMap<String,String> packInfo= LegacyDBHelper.GetValueMap(context, conn, "SELECT P.PACKUOM3 UOM, P.PACKDESCR, P.PACKKEY FROM PACK P, SKU S WHERE P.PACKKEY=S.PACKKEY AND SKU = ?", new String[] {sku});


			LpnInfo[] lpnInfos=new LpnInfo[sumBarrelQty];

			int j=0;
			for(int i=0;i<aBARRELQTY.length;i++)
			{
					for(int i2=0;i2<Integer.parseInt(aBARRELQTY[i]);i2++)
					{
						LpnInfo b1=new LpnInfo();
						if(SKU.isSerialControl(context,conn,sku)){
							b1.LPN = IdGenerationHelper.createBoxId(context,conn,RECEIPTLOT);
						}else {
							b1.LPN = IdGenerationHelper.generateLpn(context, conn, RECEIPTLOT);
						}
						b1.QTY=new BigDecimal(aNETWGT[i]);
						b1.PACKKEY=packInfo.get("PACKKEY");

						b1.UOM = packInfo.get("UOM");
						b1.NOTE= LegecyUtilHelper.NzParams(aNOTE[i]);
						b1.GROSSWGT=aGROSSWGT[i];
						b1.NETWGT=aNETWGT[i];
						b1.TAREWGT=aTAREWGT[i];
						b1.GROSSWGTLABEL=aGROSSWGTLABEL[i];
						b1.NETWGTLABEL=aNETWGTLABEL[i];
						b1.TAREWGTLABEL=aTAREWGTLABEL[i];
						lpnInfos[j++]=b1;
					}
			}
			
			
			/*
				LISTNAME	CODE	DESCRIPTION
				RECCHKSTAT	0	未检查
				RECCHKSTAT	1	检查通过
				RECCHKSTAT	2	已收货
				RECCHKSTAT	9	检查未通过
				RECCHKSTAT	91	检查未通过已填写处理方式
				RECCHKSTAT	92	检查未通过已处理
			 */

			RECEIPTKEY= LegacyDBHelper.GetNCounterBill(context, conn, "RECEIPT");

			HashSet<String> poHashSet = new HashSet<>();
			HashSet<String> projectCodeHashSet = new HashSet<>();

			for(int i=0;i<usedPOBeans.size();i++)
			{
				UsedPOBean usedPOBean=usedPOBeans.get(i);

				LegacyDBHelper.ExecSql(context, conn
						, "Update WMS_PO_DETAIL set RECEIVEDQTY=ISNULL(RECEIVEDQTY,0)+?,STATUS=? where POKEY=? and POLINENUMBER=?"
						, new String[]{trimZerosAndToStr(ReceiptUtilHelper.stdQty2PoWgt(context,conn,skuInfo.get("SNAVGWGT"),usedPOBean.AsnQTY,skuInfo.get("SKU")) ),usedPOBean.STATUS,usedPOBean.POKEY,usedPOBean.POLINENUMBER});
				LegacyDBHelper.ExecSql(context, conn
						, "Update WMS_PO set STATUS=? where POKEY=?"
						, new String[]{usedPOBean.STATUS,usedPOBean.POKEY});
				poHashSet.add(usedPOBean.POKEY);
				projectCodeHashSet.add(usedPOBean.PROJECTCODE);
				//记录该PO本次生成ASN的数量，该数量将用于在收货完成后收货接口回传ERP时参考使用
				LegacyDBHelper.ExecSql(context, conn
						,"update PRERECEIPTCHECK set POUSEDQTY = ? where RECEIPTLOT=? and fromkey=? and fromlineno=? "
						,new String[]{trimZerosAndToStr(ReceiptUtilHelper.stdQty2PoWgt(context,conn,skuInfo.get("SNAVGWGT"),usedPOBean.AsnQTY,skuInfo.get("SKU"))),RECEIPTLOT,usedPOBean.POKEY,usedPOBean.POLINENUMBER });
			}

			String poListStr = UtilHelper.nvl(poHashSet.stream().collect(Collectors.joining(";")),"");
			String projectCodeListStr = UtilHelper.nvl(projectCodeHashSet.stream().collect(Collectors.joining(",")),"");

			LegacyDBHelper.ExecSql(context, conn
					,"update PRERECEIPTCHECK set status=?,editwho=?,editdate=? where RECEIPTLOT=?"
					,new String[]{"1",userid,"@date",RECEIPTLOT});
			
			LinkedHashMap<String,String> RECEIPT=new LinkedHashMap<String,String>();
			if(ESignatureService.getUserByEsignaturkey(context,conn,ESIGNATUREKEY).indexOf(':')==-1){
				RECEIPT.put("ISCONFIRMEDUSER", usedPOBeans.get(0).SUPPLIER);
			}else {
				//复核
				String[] eSignatureKeys = ESignatureService.getUserByEsignaturkey(context,conn,ESIGNATUREKEY).split(":");
				RECEIPT.put("ISCONFIRMEDUSER", eSignatureKeys[0]);
				RECEIPT.put("ISCONFIRMEDUSER2",eSignatureKeys[1]);;

			}
			RECEIPT.put("ADDWHO", userid);
			RECEIPT.put("EDITWHO", userid);
			RECEIPT.put("RECEIPTKEY", RECEIPTKEY);
			RECEIPT.put("EXTERNRECEIPTKEY", RECEIPTLOT);
			//RECEIPT.put("SUSR5", CONVERSIONRATE);

			RECEIPT.put("STATUS", "0");
			RECEIPT.put("ALLOWAUTORECEIPT", "0");
			RECEIPT.put("TYPE", RECEIPTYPE);
			RECEIPT.put("STORERKEY", STORERKEY);
			RECEIPT.put("ISCONFIRMED", "2"); //接口指令创建的ASN默认为已确认并且不允许取消确认
			
			RECEIPT.put("SHIPFROMADDRESSLINE1", usedPOBeans.get(0).SUPPLIER);
			RECEIPT.put("SHIPFROMADDRESSLINE2", usedPOBeans.get(0).NAMEALPHA);
			RECEIPT.put("POKEY", poListStr);
			//采购入库保税检查 prereceiptcheck->receipt
			LinkedHashMap<String,String> bondedCheck= LegacyDBHelper.GetValueMap(context, conn,
					"select TOP 1 LOTTABLE10,BONDEDSTORES,MHTASKKEY,MHLINENO,ELOTTABLE22" +
							" from PRERECEIPTCHECK  where   RECEIPTLOT=?  ",new String[]{RECEIPTLOT});
			RECEIPT.put("ELOTTABLE01",bondedCheck.get("LOTTABLE10"));
			RECEIPT.put("ELOTTABLE02",bondedCheck.get("BONDEDSTORES"));
			RECEIPT.put("ELOTTABLE23",UtilHelper.isEmpty(bondedCheck.get("MHTASKKEY"))?" ":bondedCheck.get("MHTASKKEY"));
			RECEIPT.put("ELOTTABLE24",UtilHelper.isEmpty(bondedCheck.get("MHLINENO"))?" ":bondedCheck.get("MHLINENO"));
			RECEIPT.put("ELOTTABLE22",UtilHelper.isEmpty(bondedCheck.get("ELOTTABLE22"))?" ":bondedCheck.get("ELOTTABLE22"));
			//RECEIPT.put("ELOTTABLE25",UtilHelper.isEmpty(bondedCheck.get("ELOTTABLE25"))?" ":bondedCheck.get("ELOTTABLE25"));
			LegacyDBHelper.ExecInsert(context, conn, "RECEIPT", RECEIPT);

			for(int i1=0;i1<lpnInfos.length;i1++)
			{
				String BARRELNUMBER=Integer.toString(i1+1); while (BARRELNUMBER.length()<3) BARRELNUMBER="0"+BARRELNUMBER;
				String TOTALBARREL=Integer.toString(lpnInfos.length); while (TOTALBARREL.length()<3) TOTALBARREL="0"+TOTALBARREL;



				/*
					批属性01 项目号
					批属性02	质量等级
					批属性03	状态
					批属性04	入库日期
					批属性05	复验日期
					批属性06	批号
					批属性07	规格
					批属性08	供应商
					批属性09 供应商批次
					批属性10 采购编码
					批属性11	有效期
					批属性12	原材料-取样日期/成品-生产日期
				*/
				//根据lot获收货检查表中信息
				//是否检验根据sku表中busr3    ELOTTABLE03
				//规格LOTTABLE07根据 ReceiptCheck ELOTTABLE07
				//ELOTTABLE08 供应商代码  ReceiptCheck  POSUPPLIERCODE
				//ELOTTABLE05 复验期  ReceiptCheck RETESTDATE
				//LOTTABLE09 供应商名称  ReceiptCheck    POSUPPLIERNAME
				//ELOTTABLE11 有效期  ReceiptCheck  ELOTTABLE11
				//ELOTTABLE12 生产日期  ReceiptCheck MANUFACTURERDATE
				LinkedHashMap<String,String> pReceiptCheck= LegacyDBHelper.GetValueMap(context, conn,
						"select p.ELOTTABLE07,p.POSUPPLIERCODE,p.POSUPPLIERNAME,s.busr3 ," +
								" FORMAT(p.ELOTTABLE11,'"+ Const.DateTimeFormat+"') as ELOTTABLE11,"+
								" FORMAT(p.MANUFACTURERDATE,'"+ Const.DateTimeFormat+"') as MANUFACTURERDATE, "+
								" FORMAT(p.RETESTDATE,'"+ Const.DateTimeFormat+"') as RETESTDATE,checkresult,ISCOMMONPROJECT,PROJECTCODE"+
								" from PRERECEIPTCHECK p,SKU s where p.RECEIPTLOT=? and s.sku=p.sku ",new String[]{RECEIPTLOT});

				LinkedHashMap<String,String> RECEIPTDETAIL=new LinkedHashMap<String,String>();
				RECEIPTDETAIL.put("ELOTTABLE03", pReceiptCheck.get("busr3"));

				RECEIPTDETAIL.put("ELOTTABLE08", pReceiptCheck.get("POSUPPLIERCODE"));
				RECEIPTDETAIL.put("ELOTTABLE20", pReceiptCheck.get("POSUPPLIERNAME"));
				if(!UtilHelper.isEmpty(pReceiptCheck.get("RETESTDATE"))) {
					RECEIPTDETAIL.put("ELOTTABLE05", "@date|" + pReceiptCheck.get("RETESTDATE") );
				}
				if(!UtilHelper.isEmpty(pReceiptCheck.get("ELOTTABLE11"))) {
					RECEIPTDETAIL.put("ELOTTABLE11", "@date|" + pReceiptCheck.get("RETESTDATE") );
				}
				if(!UtilHelper.isEmpty(pReceiptCheck.get("MANUFACTURERDATE"))) {
					RECEIPTDETAIL.put("ELOTTABLE12", "@date|" + pReceiptCheck.get("MANUFACTURERDATE") );
				}

				RECEIPTDETAIL.put("GROSSWGTEXPECTED", lpnInfos[i1].GROSSWGT);
				RECEIPTDETAIL.put("TAREWGTEXPECTED", lpnInfos[i1].TAREWGT);
				RECEIPTDETAIL.put("BARRELNUMBER", BARRELNUMBER); //桶号
				RECEIPTDETAIL.put("TOTALBARRELNUMBER", TOTALBARREL); //总桶数

				//RECEIPTDETAIL.put("SUSR1", extSku); //采购编码改为LOTTABLE10
				RECEIPTDETAIL.put("SUSR2", BARRELNUMBER+" / "+TOTALBARREL); //桶号
				RECEIPTDETAIL.put("SUSR3", FROMLOT); //供应商批号
				RECEIPTDETAIL.put("SUSR4", usedPOBeans.get(0).SUPPLIER); //供应商
				RECEIPTDETAIL.put("SUSR5", usedPOBeans.get(0).NAMEALPHA); //供应商名称
				if ("1".equalsIgnoreCase(pReceiptCheck.get("ISCOMMONPROJECT"))) {
					RECEIPTDETAIL.put("SUSR6", projectCodeListStr);
				}else {
					RECEIPTDETAIL.put("SUSR6", CDSysSet.getDefaultProjectCode(context,conn));
				}
				RECEIPTDETAIL.put("SUSR12", usedPOBeans.get(0).PROJECTID); //项目号id暂时取第一条 不考虑多项目号情况

				RECEIPTDETAIL.put("SUSR16", poListStr); //SUSR16 PO单号
				RECEIPTDETAIL.put("EXTERNRECEIPTKEY", RECEIPTLOT);
				//RECEIPTDETAIL.put("POKEY", RECEIPTLOT);
				RECEIPTDETAIL.put("ADDWHO", userid);
				RECEIPTDETAIL.put("EDITWHO", userid);
				RECEIPTDETAIL.put("RECEIPTKEY", RECEIPTKEY);
				RECEIPTDETAIL.put("TYPE", RECEIPTYPE);
				RECEIPTDETAIL.put("RECEIPTLINENUMBER", LegecyUtilHelper.To_Char(i1+1, 5));
				RECEIPTDETAIL.put("STORERKEY", STORERKEY);
				RECEIPTDETAIL.put("SKU", sku);
				RECEIPTDETAIL.put("QTYEXPECTED", trimZerosAndToStr(lpnInfos[i1].QTY));
				RECEIPTDETAIL.put("UOM",packInfo.get("UOM"));
				RECEIPTDETAIL.put("PACKKEY",packInfo.get("PACKKEY"));
				RECEIPTDETAIL.put("TOLOC", "STAGE");
				RECEIPTDETAIL.put("TOID", lpnInfos[i1].LPN);
				RECEIPTDETAIL.put("CONDITIONCODE", "OK");
				RECEIPTDETAIL.put("LOTTABLE01",  " ");
				RECEIPTDETAIL.put("LOTTABLE02",  LegecyUtilHelper.Nz(LOTTABLE02,"0"));	//存货类型默认为0=正常
				RECEIPTDETAIL.put("LOTTABLE03", " ");
				RECEIPTDETAIL.put("LOTTABLE04", "@date11");
				RECEIPTDETAIL.put("LOTTABLE06", RECEIPTLOT);
				RECEIPTDETAIL.put("LOTTABLE07", " ");
				RECEIPTDETAIL.put("LOTTABLE08", " ");
				RECEIPTDETAIL.put("LOTTABLE09", " ");
				RECEIPTDETAIL.put("LOTTABLE10", " ");
				RECEIPTDETAIL.put("ELOTTABLE01", "");
				RECEIPTDETAIL.put("ELOTTABLE02",  "");
				/*
					--康龙：
					质量状态由SKU带入。逻辑：
					SKU.SUSR3=1(无需放行）:质量状态为NA
					SKU.SUSR3=0(需要放行）:质量状态为QUARANTINE(待检)
				    QUALITYSTATUS: QUARANTINE 待验、NA 无质量状态

					--CSS/CS
					是否检验
					SKU.BUSR3=1(是）:质量状态为QUARANTINE(待检)
					SKU.BUSR3=0(否）:质量状态为RELEASE(无需检验,直接放行)

				*/
				String qualityStatus = CDQualityStatus.findByReceiptType(context,conn,RECEIPTYPE,sku,"");
				String poqualityStatus= CodeLookup.getCodeLookupValue(context,conn,"RECCHKRES",pReceiptCheck.get("checkresult"),"UDF2","收货检查结果质量状态");
				RECEIPTDETAIL.put("ELOTTABLE03",  UtilHelper.isEmpty(poqualityStatus)?qualityStatus:poqualityStatus);
				RECEIPTDETAIL.put("ELOTTABLE06",  LegecyUtilHelper.Nz(ELOTTABLE06,""));
				RECEIPTDETAIL.put("ELOTTABLE07",  LegecyUtilHelper.Nz(ELOTTABLE07,""));
				//RECEIPTDETAIL.put("ELOTTABLE07", XtUtils.Nz(spec,"")); 跟客户确认后，PO不传型号 20210119
				RECEIPTDETAIL.put("ELOTTABLE08", LegecyUtilHelper.Nz(usedPOBeans.get(0).SUPPLIER,""));
				RECEIPTDETAIL.put("ELOTTABLE09", LegecyUtilHelper.Nz(FROMLOT,""));
				RECEIPTDETAIL.put("ELOTTABLE10", LegecyUtilHelper.Nz(RECEIPTLOT,""));
				RECEIPTDETAIL.put("ELOTTABLE14", LegecyUtilHelper.Nz(qualifiedproducer,""));

				RECEIPTDETAIL.put("ELOTTABLE11",  ELOTTABLE11);
				RECEIPTDETAIL.put("ELOTTABLE21",  LegecyUtilHelper.Nz(usedPOBeans.get(0).ERPLOC,""));
				RECEIPTDETAIL.put("NOTES", lpnInfos[i1].NOTE);
				LegacyDBHelper.ExecInsert(context, conn, "RECEIPTDETAIL", RECEIPTDETAIL);

				//因不存在非整个唯一码可能性，使用重量作为唯一码数量
				if(SKU.isSerialControl(context,conn,sku)){

					int snNum = lpnInfos[i1].QTY.intValue();
					String[] snlist = new String[snNum];
					String[] snwgtlist = new String[snNum];
					String[] snuomlist = new String[snNum];
					for (int i =0 ; i<snNum;i++){
						 snlist[i] = IdGenerationHelper.createSNID(context,conn);
						 if (CDSysSet.enableSNwgt(context,conn)){
						 	snwgtlist[i] = skuInfo.get("SNAVGWGT");
						 	snuomlist[i] = skuInfo.get("SNUOM");
						 }else {
							 snwgtlist[i] = "0";
							 snuomlist[i] = " ";
						 }
					}
					buildReceiptLotxIdInfo(context, conn, sku,"I",lpnInfos[i1].LPN,RECEIPTKEY, LegecyUtilHelper.To_Char(i1+1, 5), snlist,snwgtlist,snuomlist);


				}
			}

//			EXEDataObject printDO = new EXEDataObject();
//			printDO.setAttribValue("printername"), PRINTER));
//			printDO.setAttribValue("receiptkey"), RECEIPTKEY));
//			printDO.setAttribValue("receiptlinenumber"), ""));
//			printDO.setAttribValue("labelname"), "LPN"));
//			printDO.setAttribValue("copies"), "2"));//NOT USE
//			ServiceHelper.executeService(context, "PrintASNLables", printDO);
			if(!PRINTER.equals("-1")) {
				PrintHelper.printLPNByReceiptKey(context, conn, RECEIPTKEY, Labels.LPN_UI, PRINTER, "1", "RF采购收货标签");
			}


			Udtrn UDTRN=new Udtrn();
			if(ESIGNATUREKEY.indexOf(':')==-1){

				UDTRN.EsignatureKey=ESIGNATUREKEY;
			}else {
				//复核
				String[] eSignatureKeys = ESIGNATUREKEY.split(":");
				UDTRN.EsignatureKey=eSignatureKeys[0];
				UDTRN.EsignatureKey1=eSignatureKeys[1];
			}

			UDTRN.FROMTYPE="采购收货检查-创建ASN";
			UDTRN.FROMTABLENAME="PRERECEIPTCHECK";
		    UDTRN.FROMKEY=RECEIPTLOT;
		    UDTRN.FROMKEY1=RECEIPTKEY;
		    UDTRN.FROMKEY2="";
		    UDTRN.FROMKEY3="";
		    UDTRN.TITLE01="收货批次";    UDTRN.CONTENT01=RECEIPTLOT;
		    UDTRN.TITLE02="ASN单号";    UDTRN.CONTENT02=RECEIPTKEY;
		    UDTRN.TITLE04="SKU";    UDTRN.CONTENT04=sku;
		    UDTRN.TITLE05="厂家来源批次";    UDTRN.CONTENT05=FROMLOT;
		    UDTRN.TITLE07="厂家名称";    UDTRN.CONTENT07=MANUFACTURERNAME;//??
		    UDTRN.TITLE08="合计净重";    UDTRN.CONTENT08=SUMNETWGT;
		    UDTRN.TITLE09="合计桶数";    UDTRN.CONTENT09=SUMBARRELQTY;
		    UDTRN.TITLE10="包装";    UDTRN.CONTENT10=packInfo.get("PACKKEY");
		    UDTRN.TITLE11="计量单位";    UDTRN.CONTENT11=packInfo.get("UOM");
		    UDTRN.TITLE12="毛重";    UDTRN.CONTENT12=GROSSWGT;
		    UDTRN.TITLE13="皮重";    UDTRN.CONTENT13=TAREWGT;
		    UDTRN.TITLE14="净重";    UDTRN.CONTENT14=NETWGT;
		    UDTRN.TITLE15="桶数";    UDTRN.CONTENT15=BARRELQTY;
		    UDTRN.TITLE16="备注";    UDTRN.CONTENT16=NOTE;
		    
		    UDTRN.Insert(context, conn, userid);

		    
			//----------------------------------------------------------------
			try	{	context.releaseConnection(conn); 	}	catch (Exception e1) {		}


			ServiceDataMap theOutDO = new ServiceDataMap();
			theOutDO.setAttribValue("RECEIPTKEY", RECEIPTKEY);

			serviceDataHolder.setReturnCode(1);
			serviceDataHolder.setOutputData(theOutDO);

		}
		catch (Exception e)
		{
			try
			{
				context.releaseConnection(conn);
			}	catch (Exception e1) {		}
			if ( e instanceof FulfillLogicException )
				throw (FulfillLogicException)e;
			else
		        throw new FulfillLogicException(e.getMessage());
		}



	}

//	private int createLpn(Context context,Connection conn,String UserID,String Lot,int LpnCount) throws Exception
//	{
//		String iCnt=XtSql.GetValue(context, conn
//				, "SELECT KEYCOUNT FROM ENTERPRISE.NCOUNTER WHERE KEYNAME=?", new String[]{Lot}, null);
//		if (iCnt==null)
//		{
//			XtSql.ExecSql(context, conn
//					, "INSERT INTO ENTERPRISE.NCOUNTER(KEYNAME,KEYCOUNT,ADDWHO,EDITWHO) VALUES(?,?,?,?)"
//					, new String[]{Lot,Integer.toString(LpnCount),UserID,UserID});
//			return 1;
//		}
//		else
//		{
//			XtSql.ExecSql(context, conn
//					, "UPDATE ENTERPRISE.NCOUNTER SET KEYCOUNT=KEYCOUNT+?,EDITWHO=?,EDITDATE=? WHERE KEYNAME=?"
//					, new String[]{Integer.toString(LpnCount),UserID,"@date",Lot});
//			return Integer.parseInt(iCnt)+1;
//		}
//	}

	private class LpnInfo
	{
		public String PACKKEY=null;
		public String UOM=null;
		public String LPN=null;
		public BigDecimal QTY=null;
		public String NOTE=null;
		public String GROSSWGT=null;
		public String TAREWGT=null;
		public String NETWGT=null;	
		public String GROSSWGTLABEL=null;
		public String TAREWGTLABEL=null;
		public String NETWGTLABEL=null;		
	}
	
	
}