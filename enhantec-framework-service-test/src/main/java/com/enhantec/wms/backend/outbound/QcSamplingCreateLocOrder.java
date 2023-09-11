package com.enhantec.wms.backend.outbound;

import com.enhantec.framework.common.utils.EHContextHelper;
import com.enhantec.wms.backend.utils.common.LegacyDBHelper;
import com.enhantec.wms.backend.common.base.code.CDSysSet;
import com.enhantec.wms.backend.framework.WMSBaseService;
import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.framework.ServiceDataMap;
import com.enhantec.wms.backend.utils.audit.Udtrn;
import com.enhantec.wms.backend.utils.common.*;

import java.util.HashMap;
import java.util.Map;

public class QcSamplingCreateLocOrder  extends WMSBaseService
{



    /**
     * JOHN 20200202按日期 获取采购批次号
     --注册方法
     DELETE FROM wmsadmin.sproceduremap WHERE THEPROCNAME = 'QcSamplingCreateLocOrder'
     insert into wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION, ADDDATE, ADDWHO, EDITDATE, EDITWHO, PARAMETERS,LOAD,ISBATCH)
     values ('QcSamplingCreateLocOrder', 'com.enhantec.sce.outbound.order', 'enhantec', 'QcSamplingCreateLocOrder', 'TRUE', SYSDATE, 'JOHN', SYSDATE, 'JOHN', 'sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,'||
     'LOTTABLE06,ORDERTYPE,ESIGNATUREKEY','0.10','0');

     */


    private static final long serialVersionUID = 1L;

    public QcSamplingCreateLocOrder()
    {
    }

    public void execute(ServiceDataHolder serviceDataHolder)
    {
        //public RrDateTime currentDate;
        //this.currentDate = UtilHelper.getCurrentDate();


        String userid = EHContextHelper.getUser().getUsername();

        


        String LOTTABLE06= serviceDataHolder.getInputDataAsMap().getString("LOTTABLE06");
        //String PROJECTCODE=processData.getInputDataMap().getString( "PROJECTCODE");
        //String ORDERTYPE=processData.getInputDataMap().getString( "ORDERTYPE");
        String ESIGNATUREKEY= serviceDataHolder.getInputDataAsMap().getString("ESIGNATUREKEY");
        String OrderKey="";
        String uom="";
        String sku="";
        String packKey="";
        String skuDescr="";
        String total1="0";
        String total3="0";
        String ORDERTYPE = CDSysSet.getSampleOrderType();//原料取样出库
        boolean orderExists =false;
        Map<String,String> samplePackInfo=null;
        try
        {
            String STORERKEY= DBHelper.getStringValue( "select udf1 from codelkup where listname=? and code=?", new String[]{"SYSSET","STORERKEY"}, "");
            //String LGPROJECT=XtSql.GetValue( "select udf1 from codelkup where listname=? and code=?", new String[]{"SYSSET","LGPROJECT"}, "");

            String lot= DBHelper.getStringValue( "select a.lot LOT from lotxlocxid a,v_lotattribute b where a.lot=b.lot and a.qty>0 and a.STORERKEY = ? and b.lottable06=? ", new String[]{STORERKEY, LOTTABLE06},"");
            if (UtilHelper.isEmpty(lot)) throw new Exception("当前批次在系统中无库存");


            Map<String,String> res= DBHelper.getRecord("select c.PACKKEY, c.PACKUOM3  UOM, s.SKU SKU, s.DESCR SKUDESCR from v_lotattribute a, pack c, SKU s where s.SKU = a.SKU and c.PACKKEY = s.PACKKEY and a.STORERKEY = ? and a.lot=?", new String[]{STORERKEY, lot});

            uom = res.get("UOM");
            sku = res.get("SKU");
            packKey = res.get("PACKKEY");
            skuDescr = res.get("SKUDESCR");

            samplePackInfo= DBHelper.getRecord("select c.PACKKEY, c.PACKUOM3 UOM, s.SKU SKU, s.DESCR SKUDESCR " +
                    " from v_lotattribute a, pack c, SKU s " +
                    " where s.SKU = a.SKU and c.PACKKEY = s.SUSR6 and a.STORERKEY = ? and a.lot=?",new Object[]{STORERKEY, lot},sku+"的取样包装配置",true);


            String OldOrderKey= DBHelper.getStringValue( "select orderkey from orders where STORERKEY = ? and REFERENCENUM=? and type = ? and status<'90'", new String[]{STORERKEY, LOTTABLE06, ORDERTYPE}, "");
            //if (!XtUtils.isNull(OldOrderKey)) throw new Exception("当前批次有未关闭的在库取样单("+OldOrderKey+")");

            if (LegecyUtilHelper.isNull(OldOrderKey)) {
                OrderKey = LegacyDBHelper.GetNCounterBill( "ORDER");
                Map<String,String> Fields = new HashMap<String,String>();
                Fields.put("AddWho", userid);
                Fields.put("EditWho", userid);
                Fields.put("type", ORDERTYPE);
                Fields.put("ohtype", ORDERTYPE);
                Fields.put("status", "06");
                Fields.put("orderkey", OrderKey);
                Fields.put("externorderkey", "WMS"+OrderKey);
                Fields.put("REFERENCENUM", LOTTABLE06);
                Fields.put("storerkey", STORERKEY);
                //Fields.put("SUSR1", XtUtils.FormatUdf("PROJECTCODE", LGPROJECT, 30));
                //Fields.put("notes", PROJECTCODE);
                LegacyDBHelper.ExecInsert( "orders", Fields);

                Udtrn UDTRN = new Udtrn();
                UDTRN.EsignatureKey = ESIGNATUREKEY;
                UDTRN.FROMTYPE = "在库取样-原料取样出库";
                UDTRN.FROMTABLENAME = "ORDERS";
                UDTRN.FROMKEY = LOTTABLE06;
                UDTRN.FROMKEY1 = OrderKey;
                UDTRN.FROMKEY2 = "";
                UDTRN.FROMKEY3 = "";
                UDTRN.TITLE01 = "取样批次";
                UDTRN.CONTENT01 = LOTTABLE06;
                UDTRN.TITLE02 = "出库单号";
                UDTRN.CONTENT02 = OrderKey;
                //UDTRN.TITLE03="项目";    UDTRN.CONTENT03=PROJECTCODE;
                UDTRN.Insert( userid);
            }else{
                orderExists = true;
                OrderKey = OldOrderKey;
//                List<Map<String,String>> mapList =XtSql.GetRecordMap( "select ORIGINALQTY,OPENQTY,UOM from ORDERDETAIL where orderkey=?",new String[]{OrderKey});
//
//                UOMConverter uomConverter =  new UOMConverter();
//                String finalPackKey = packKey;
//                Function<Map<String,String>, BigDecimal> calcORIGINALQTY = e-> {
//                    try {
//                        return uomConverter.UOMQty2StdQty(finalPackKey, e.get("UOM"), new BigDecimal(e.get("ORIGINALQTY")));
//                    } catch (Exception exception) {
//                        return BigDecimal.ZERO;
//                    }
//                };
//
//                Function<Map<String,String>, BigDecimal> calcOPENQTY = e-> {
//                    try {
//                        return uomConverter.UOMQty2StdQty(finalPackKey, e.get("UOM"), new BigDecimal(e.get("OPENQTY")));
//                    } catch (Exception exception) {
//                        return BigDecimal.ZERO;
//                    }
//                };
//
//                if(!mapList.isEmpty()){
//                    total1 = String.valueOf(mapList.stream().map(calcORIGINALQTY).reduce(BigDecimal.ZERO, (BigDecimal subtotal, BigDecimal element) -> subtotal.add(element)));
//                    total2 = String.valueOf(mapList.stream().map(calcOPENQTY).reduce(BigDecimal.ZERO, (BigDecimal subtotal, BigDecimal element) -> subtotal.add(element)));
//                }
                total1= DBHelper.getStringValue( "SELECT SUM(CONVERT(decimal(11,5), SUSR1)) from ORDERDETAIL where orderkey=?",new String[]{OrderKey},"0");
                total3= DBHelper.getStringValue( "select sum(ORIGINALQTY) from ORDERDETAIL where orderkey=?",new String[]{OrderKey},"0");

            }


        }
        catch (Exception e)
        {
            if ( e instanceof FulfillLogicException )
                throw (FulfillLogicException)e;
            else
                throw new FulfillLogicException(e.getMessage());
        }finally {
            
        }

        //减少系统库存
        ServiceDataMap theOutDO = new ServiceDataMap();
        theOutDO.setAttribValue("ORDERKEY", OrderKey);
        theOutDO.setAttribValue("SKU", sku);
        theOutDO.setAttribValue("SKUDESCR", skuDescr);
        theOutDO.setAttribValue("UOM", uom);
        theOutDO.setAttribValue("TOTAL1", total1);
        theOutDO.setAttribValue("TOTAL3", total3);
        theOutDO.setAttribValue("SAMPLEUOM", samplePackInfo.get("UOM"));
        theOutDO.setAttribValue("orderExists",  orderExists ? 1:0);

        serviceDataHolder.setOutputData(theOutDO);
        serviceDataHolder.setReturnCode(1);


    }
}