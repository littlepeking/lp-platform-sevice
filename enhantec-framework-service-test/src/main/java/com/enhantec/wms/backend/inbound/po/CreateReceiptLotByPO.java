package com.enhantec.wms.backend.inbound.po;

import com.enhantec.framework.common.utils.EHContextHelper;
import com.enhantec.wms.backend.utils.common.LegacyDBHelper;
import com.enhantec.wms.backend.common.base.CodeLookup;
import com.enhantec.wms.backend.framework.WMSBaseService;
import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.framework.ServiceDataMap;
import com.enhantec.wms.backend.utils.audit.Udtrn;
import com.enhantec.wms.backend.utils.common.*;

import java.util.List;
import java.util.Map;import java.util.HashMap;


public class CreateReceiptLotByPO  extends WMSBaseService
{



    /**
     * JOHN 20201010按日期 生成采购批次检查记录
     --注册方法

     DELETE FROM SCPRDMST.wmsadmin.sproceduremap WHERE COMPOSITE='CreateReceiptLotByPO';
     insert into SCPRDMST.wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION,  ADDWHO, EDITWHO, PARAMETERS,LOAD,ISBATCH)
     values ('CreateReceiptLotByPO', 'com.enhantec.sce.inbound.po', 'enhantec', 'CreateReceiptLotByPO', 'TRUE',  'JOHN',  'ALLAN' , 'sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server, PoKey,Sku,FromLot,ForceNew,Uom,TotalBarrel,Qty,elottable07,elottable11,ESIGNATUREKEY,MANUFACTURERDATE,RETESTDATE,TRANSCHECK,FILECHECK,PACKCHECK','0.10','0');

     */


    private static final long serialVersionUID = 1L;

    public void execute(ServiceDataHolder serviceDataHolder)
    {

        String userid = EHContextHelper.getUser().getUsername();

        

        try
        {
            String ReceiptLot=null;
            String ResultType=null;
            String FROMKEY=null;
            String STATUS=null;
            String SKUDESCR="";
            String Uom="";
            String Qty="";
            String TotalBarrel="";
            String elottable07="";
            String elottable11="";
            //String MUOM="";


            String PoKey= serviceDataHolder.getInputDataAsMap().getString("PoKey");
            String Sku= serviceDataHolder.getInputDataAsMap().getString("Sku");
            String FromLot= serviceDataHolder.getInputDataAsMap().getString("FromLot");
            String ESIGNATUREKEY= serviceDataHolder.getInputDataAsMap().getString("ESIGNATUREKEY");
            //String BatchDate=processData.getInputDataMap().getString("BatchDate");
            String ForceNew= serviceDataHolder.getInputDataAsMap().getString("ForceNew");
            Uom= serviceDataHolder.getInputDataAsMap().getString("Uom");
            Qty= serviceDataHolder.getInputDataAsMap().getString("Qty");
            TotalBarrel= serviceDataHolder.getInputDataAsMap().getString("TotalBarrel");
            elottable07= serviceDataHolder.getInputDataAsMap().getString("elottable07");
            elottable11= serviceDataHolder.getInputDataAsMap().getString("elottable11");
            //MANUFACTURERDATE,RETESTDATE,TRANSCHECK,FILECHECK,PACKCHECK -------- add by Allan
            String MANUFACTURERDATE= serviceDataHolder.getInputDataAsMap().getString("MANUFACTURERDATE");//生产日期
            String RETESTDATE= serviceDataHolder.getInputDataAsMap().getString("RETESTDATE");//复验期
            String TRANSCHECK= serviceDataHolder.getInputDataAsMap().getString("TRANSCHECK");//运输条件
            String FILECHECK= serviceDataHolder.getInputDataAsMap().getString("FILECHECK");//随货文件
            String PACKCHECK= serviceDataHolder.getInputDataAsMap().getString("PACKCHECK");//包装状况
            String PACKCOUNTCHECK = serviceDataHolder.getInputDataAsMap().getString("PACKCOUNTCHECK");
            String abnormalitymesg = serviceDataHolder.getInputDataAsMap().getString("abnormalitymesg");
            String abnormality = serviceDataHolder.getInputDataAsMap().getString("abnormality");
            String checkresult = serviceDataHolder.getInputDataAsMap().getString("checkresult");
            String expirydatecheck = serviceDataHolder.getInputDataAsMap().getString("expirydatecheck");
            String supplieritem = serviceDataHolder.getInputDataAsMap().getString("supplieritem");
            String qualifiedproducer = serviceDataHolder.getInputDataAsMap().getString("qualifiedproducer");
            String QUALIFIEDSUPPLIER = serviceDataHolder.getInputDataAsMap().getString("QUALIFIEDSUPPLIER");
            String ISCOMMONPROJECT = serviceDataHolder.getInputDataAsMap().getString("ISCOMMONPROJECT");
            String PROJECTCODE = serviceDataHolder.getInputDataAsMap().getString("PROJECTCODE");

            //---------
            UtilHelper.toDate(elottable11,"yyyyMMdd",true,"有效期格式或日期错误，格式应为YYYYMMDD");
            UtilHelper.toDate(MANUFACTURERDATE,"yyyyMMdd",true,"生产日期格式或日期错误，格式应为YYYYMMDD");
            UtilHelper.toDate(RETESTDATE,"yyyyMMdd",true,"复验期格式或日期错误，格式应为YYYYMMDD");


			/*
PRERECEIPTCHECK	收货检查表
 Table表	 Field字段	TYPE	必填	Description描述
PRERECEIPTCHECK	SERIALKEY	NUMBER	Y	序列号
PRERECEIPTCHECK	WHSEID	NVARCHAR2 (30)	Y	仓库
PRERECEIPTCHECK	FROMTYPE	NVARCHAR2 (125)	Y	来源类型-- 1：采购订单
PRERECEIPTCHECK	FROMKEY	NVARCHAR2 (255)	Y	来源单号
PRERECEIPTCHECK	FROMLINENO	NVARCHAR2 (30)	N	来源行号（可为空）
PRERECEIPTCHECK	FROMSKU	NVARCHAR2 (255)	Y	来源物料
PRERECEIPTCHECK	FROMSKUDESCR	NVARCHAR2 (255)	Y	来源物料名称
PRERECEIPTCHECK	FROMLOT	NVARCHAR2 (255)		厂家来源批次（厂家批次）
PRERECEIPTCHECK	RECEIPTLOT	NVARCHAR2 (255)		收货批次
PRERECEIPTCHECK	SUPPLIERCODE	NVARCHAR2 (255)		供应商检查-供应商
PRERECEIPTCHECK	MANUFACTURERCODE	NVARCHAR2 (255)		供应商检查-生产厂家
PRERECEIPTCHECK	STATUS	NVARCHAR2 (10)		状态=RECCHKSTAT
PRERECEIPTCHECK	PROCESSINGMODE	NVARCHAR2 (10)		处理方式=PROCEMODE
PRERECEIPTCHECK	ADDDATE	DATE		创建时间
PRERECEIPTCHECK	ADDWHO	NVARCHAR2 (30)		创建人
PRERECEIPTCHECK	EDITDATE	DATE		编辑时间
PRERECEIPTCHECK	EDITWHO	NVARCHAR2 (30)		编辑人


LISTNAME	RECCHKSTAT
代码	说明
CODE	DESCRIPTION
0	未检查
1	检查通过
9	检查未通过
2	已收货
91	检查未通过已填写处理方式
92	检查未通过已处理


			 */
            int iPoKey=PoKey.indexOf('-');
            if (iPoKey<=0) throw new Exception("PO号应该带行号,请联系系统管理员");
            String POLINENUMBER=PoKey.substring(iPoKey+1);
            PoKey=PoKey.substring(0,iPoKey);

            Map<String,String> mPO= DBHelper.getRecord(
                    "SELECT A.SUPPLIER,B.qty-ISNULL(B.receivedqty,0) as AVAILABLEQTY,A.ISINTERFACEPO FROM WMS_PO A,WMS_PO_DETAIL B  " +
                            "WHERE A.POKEY=? AND B.POLINENUMBER=? AND B.STATUS<?  AND A.POKEY =B.POKEY "
                    ,new String[]{PoKey,POLINENUMBER,"9"});
            mPO.put("NAMEALPHA",QUALIFIEDSUPPLIER);
            if (mPO.isEmpty())
                throw new FulfillLogicException("采购订单(%1),物料代码(%2)无有效记录",PoKey,Sku);


            String STORERKEY= DBHelper.getStringValue( "select UDF1 from Codelkup where ListName=? and Code=?", new String[]{"SYSSET","STORERKEY"}, null);
            String WAREHOUSECODE= DBHelper.getStringValue( "select UDF1 from Codelkup where ListName=? and Code=?", new String[]{"SYSSET","WAREHOUSE"}, null);
            SKUDESCR= DBHelper.getStringValue( "SELECT DESCR FROM SKU WHERE STORERKEY=? AND SKU=?", new String[]{STORERKEY,Sku}, "");

            String CurDate= DBHelper.getStringValue( " select FORMAT(getdate(), 'yyMMdd')", new String[]{}, null);
            String CurLot= DBHelper.getStringValue( "select RECEIPTLOT from PRERECEIPTCHECK where RECEIPTLOT like '"+WAREHOUSECODE+CurDate+"%' and STATUS=? and FROMSKU=? and FROMLOT=? AND POSUPPLIERCODE=?", new String[]{"0",Sku,FromLot,mPO.get("SUPPLIER")}, null);
            if (CurLot!=null)
            {
                ResultType="Load";
                ReceiptLot=CurLot;
            }
            if (ResultType==null)
            {
                String OldLot= DBHelper.getStringValue( "select RECEIPTLOT from PRERECEIPTCHECK where STATUS=? and FROMSKU=? and FROMLOT=? AND POSUPPLIERCODE=? order by RECEIPTLOT", new String[]{"0",Sku,FromLot,mPO.get("SUPPLIER")}, null);
                if (OldLot!=null)
                {
                    if (!LegecyUtilHelper.CheckYesNo(ForceNew))
                    {
                        ResultType="Warn";
                        ReceiptLot=OldLot;
                    }
                }
            }

            ServiceDataMap theOutDO = new ServiceDataMap();

            if (ResultType==null)
            {
                STATUS="0";
                FROMKEY=PoKey+"-"+POLINENUMBER;
                //MUOM=mPO.get("MUOM");

                //if (!mPO.get("PUOM").equals(Uom)) throw new Exception("当前计量单位("+Uom+")与采购计量单位("+mPO.get("PUOM")+")不一致");

                 STATUS= CodeLookup.getCodeLookupValue("RECCHKRES",checkresult,"UDF1","收货结果");
                ReceiptLot= IdGenerationHelper.createReceiptLot(Sku);
                Map<String,String> PRERECEIPTCHECK=new HashMap<String,String>();
                PRERECEIPTCHECK.put("WHSEID", "@user");
                PRERECEIPTCHECK.put("ADDWHO", userid);
                PRERECEIPTCHECK.put("EDITWHO", userid);
                PRERECEIPTCHECK.put("FROMTYPE",mPO.get("ISINTERFACEPO") );//1 jde 0 excel
                PRERECEIPTCHECK.put("FROMKEY", PoKey);
                PRERECEIPTCHECK.put("FROMLINENO", POLINENUMBER);
                PRERECEIPTCHECK.put("FROMSKU", Sku);
                PRERECEIPTCHECK.put("FROMSKUDESCR", SKUDESCR);
                PRERECEIPTCHECK.put("SKU", Sku);
                PRERECEIPTCHECK.put("FROMLOT", FromLot);
                PRERECEIPTCHECK.put("RECEIPTLOT", ReceiptLot);
                PRERECEIPTCHECK.put("STATUS", STATUS);
                PRERECEIPTCHECK.put("UOM", Uom);
                PRERECEIPTCHECK.put("QTY", LegecyUtilHelper.Nz(Qty, null));//PRERECEIPTCHECK中的预期量（重复值）
                PRERECEIPTCHECK.put("TotalBarrel", LegecyUtilHelper.Nz(TotalBarrel, null));
                PRERECEIPTCHECK.put("POSUPPLIERCODE", mPO.get("SUPPLIER"));
                PRERECEIPTCHECK.put("POSUPPLIERNAME", mPO.get("NAMEALPHA"));
                //MANUFACTURERDATE,RETESTDATE,TRANSCHECK,FILECHECK,PACKCHECK -------- add by Allan
                //生产日期  复验期 运输条件 随货文件 包装


                PRERECEIPTCHECK.put("TRANSCHECK", TRANSCHECK);
                PRERECEIPTCHECK.put("FILECHECK", FILECHECK);
                PRERECEIPTCHECK.put("PACKCHECK", PACKCHECK);
                PRERECEIPTCHECK.put("abnormalitymesg", abnormalitymesg);
                PRERECEIPTCHECK.put("abnormality", abnormality);
                PRERECEIPTCHECK.put("checkresult", checkresult);
                PRERECEIPTCHECK.put("expirydatecheck", expirydatecheck);
                PRERECEIPTCHECK.put("supplieritem", supplieritem);
                PRERECEIPTCHECK.put("qualifiedproducer", qualifiedproducer);
                PRERECEIPTCHECK.put("PROJECTCODE", PROJECTCODE);
                PRERECEIPTCHECK.put("ISCOMMONPROJECT", ISCOMMONPROJECT);
                PRERECEIPTCHECK.put("PACKCOUNTCHECK", PACKCOUNTCHECK);
                PRERECEIPTCHECK.put("ELOTTABLE07", elottable07);//型号
                PRERECEIPTCHECK.put("ELOTTABLE22", mPO.get("ISINTERFACEPO"));//型号
                if(!UtilHelper.isEmpty(elottable11)) {
                    PRERECEIPTCHECK.put("ELOTTABLE11", "@date|" + elottable11 + " 11:00:00");//有效期
                }
                if(!UtilHelper.isEmpty(MANUFACTURERDATE)) PRERECEIPTCHECK.put("MANUFACTURERDATE", "@date|" +MANUFACTURERDATE+ " 11:00:00");
                    if(!UtilHelper.isEmpty(RETESTDATE)) PRERECEIPTCHECK.put("RETESTDATE", "@date|" +RETESTDATE+ " 11:00:00");
                //PRERECEIPTCHECK POSUPPLIERNAME NVARCHAR2 (255)  采购单供应商名称
                //PRERECEIPTCHECK POMANUFACTURERNAME NVARCHAR2 (255)  采购单生产商名称

                LegacyDBHelper.ExecInsert( "PRERECEIPTCHECK", PRERECEIPTCHECK);

                ResultType="New";


                Udtrn UDTRN=new Udtrn();
                String[] eSignatureKeys = ESIGNATUREKEY.split(":");
                String eSignatureKey1=eSignatureKeys[0];
                String eSignatureKey2=eSignatureKeys[1];
                String isConfirmedUser1 = DBHelper.getStringValue( "SELECT SIGN FROM ESIGNATURE e WHERE SERIALKEY = ? ", new Object[]{
                        eSignatureKey1
                }, "确认人");

                String isConfirmedUser2 = DBHelper.getStringValue( "SELECT SIGN FROM ESIGNATURE e WHERE SERIALKEY = ? ", new Object[]{
                        eSignatureKey2
                }, "复核人");
                UDTRN.FROMTYPE="采购收货检查-创建批次";
                UDTRN.FROMTABLENAME="PRERECEIPTCHECK";
                UDTRN.FROMKEY=ReceiptLot;
                UDTRN.FROMKEY1=PoKey;
                UDTRN.FROMKEY2=POLINENUMBER;
                UDTRN.FROMKEY3="";
                UDTRN.TITLE01="收货批次";    UDTRN.CONTENT01=ReceiptLot;
                UDTRN.TITLE02="采购单号";    UDTRN.CONTENT02=PoKey;
                UDTRN.TITLE03="物料代码";    UDTRN.CONTENT03=Sku;
                UDTRN.TITLE04="物料描述";    UDTRN.CONTENT04=SKUDESCR;
                UDTRN.TITLE05="厂家来源批次";    UDTRN.CONTENT05=FromLot;
                UDTRN.TITLE06="计量单位";    UDTRN.CONTENT06=Uom;
                UDTRN.TITLE07="预计到货数量";    UDTRN.CONTENT07=Qty;
                UDTRN.TITLE08="预计到货桶数";    UDTRN.CONTENT08=TotalBarrel;
                UDTRN.TITLE09="型号";    UDTRN.CONTENT09=elottable07;
                UDTRN.TITLE10="有效期";    UDTRN.CONTENT10=elottable11;
                //MANUFACTURERDATE,RETESTDATE,TRANSCHECK,FILECHECK,PACKCHECK -------- add by Allan
                //生产日期  复验期 运输条件 随货文件 包装
                UDTRN.TITLE11="生产日期";    UDTRN.CONTENT11=MANUFACTURERDATE;
                UDTRN.TITLE12="复验期";    UDTRN.CONTENT12=RETESTDATE;
                UDTRN.TITLE13="运输条件";    UDTRN.CONTENT13=TRANSCHECK;
                UDTRN.TITLE14="随货文件";    UDTRN.CONTENT14=FILECHECK;
                UDTRN.TITLE15="包装";    UDTRN.CONTENT15=PACKCHECK;
                UDTRN.TITLE16 = "确认人";
                UDTRN.CONTENT16 = isConfirmedUser1;
                UDTRN.TITLE17 = "复核人";
                UDTRN.CONTENT17 = isConfirmedUser2;
                UDTRN.Insert( userid);

                theOutDO.setAttribValue("FROMKEY"+Integer.toString(1), PoKey+"-"+POLINENUMBER);
                theOutDO.setAttribValue("FROMQTY"+Integer.toString(1), mPO.get("AVAILABLEQTY"));

            }else{

                List<Map<String,String>> r1= DBHelper.executeQuery( "select a.ELOTTABLE07,FORMAT(a.ELOTTABLE11, 'yyyyMMdd') ELOTTABLE11, a.FROMKEY,a.FROMLINENO,a.STATUS,a.UOM,a.TOTALBARREL,a.QTY, b.qty-ISNULL(b.receivedqty,0) as AVAILABLEQTY from prereceiptcheck a,WMS_PO_DETAIL b where a.fromkey=b.pokey and a.fromlineno=b.polinenumber " +
                        " and a.RECEIPTLOT=? order by a.serialkey", new String[]{ReceiptLot});


                for(int i1=0;i1<r1.size();i1++)
                {
                    Map<String,String> m1=r1.get(i1);
                    if (FROMKEY==null)
                    {
                        FROMKEY=m1.get("FROMKEY");
                        STATUS=m1.get("STATUS");
                        Uom=m1.get("UOM");
                        Qty=m1.get("QTY");
                        TotalBarrel=m1.get("TOTALBARREL");
                        elottable07=m1.get("ELOTTABLE07");
                        elottable11=m1.get("ELOTTABLE11");
                    }
                    theOutDO.setAttribValue("FROMKEY"+Integer.toString(i1+1), m1.get("FROMKEY")+"-"+m1.get("FROMLINENO"));
                    theOutDO.setAttribValue("FROMQTY"+Integer.toString(i1+1), m1.get("AVAILABLEQTY"));
                }

            }




            theOutDO.setAttribValue("ReceiptLot", ReceiptLot);

            theOutDO.setAttribValue("STATUS", STATUS);
            theOutDO.setAttribValue("SKUDESCR", SKUDESCR);
            theOutDO.setAttribValue("UOM", Uom);//主计量单位 --Comment add by John
            theOutDO.setAttribValue("TOTALBARREL", TotalBarrel);
            theOutDO.setAttribValue("QTY", Qty);//预期到货量 --Comment add by John
            theOutDO.setAttribValue("ResultType", ResultType);
            theOutDO.setAttribValue("ELOTTABLE07", elottable07);//型号
            theOutDO.setAttribValue("ELOTTABLE11", elottable11);//有效期

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