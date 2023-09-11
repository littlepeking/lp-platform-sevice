package com.enhantec.wms.backend.qa;

import com.enhantec.wms.backend.utils.common.LegacyDBHelper;
import com.enhantec.wms.backend.common.base.SKU;
import com.enhantec.wms.backend.framework.WMSBaseService;
import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.framework.ServiceDataMap;
import com.enhantec.wms.backend.utils.audit.AuditService;
import com.enhantec.wms.backend.utils.audit.Udtrn;
import com.enhantec.wms.backend.utils.common.*;

import com.enhantec.framework.common.utils.EHContextHelper;
import java.util.Map;
import java.util.HashMap;

public class AddReleaseRecord extends WMSBaseService {


/**
 * @author ：John
 * @description：创建放行记录
 */

    /**
     --注册方法  创建放行记录
     delete from scprdmst.wmsadmin.sproceduremap where THEPROCNAME='EHAddReleaseRecord'
     insert into scprdmst.wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION, ADDWHO, EDITWHO, PARAMETERS,LOAD,ISBATCH)
     values ('EHAddReleaseRecord', 'com.enhantec.sce.qa', 'enhantec', 'AddReleaseRecord', 'TRUE',  'JOHN',  'JOHN',
     'sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,JsonStr,EsignatureKey','0.10','0');
     */


    private static final long serialVersionUID = 1L;



    public void execute(ServiceDataHolder serviceDataHolder)
    {

        String userid = EHContextHelper.getUser().getUsername();  //当用户
        
        try
        {

            String jsonStr = serviceDataHolder.getInputDataAsMap().getString("JsonStr");
            String eSignatureKey = serviceDataHolder.getInputDataAsMap().getString("EsignatureKey");

            Map<String,String> mapJson = UtilHelper.jsonToMap(jsonStr);

            String lottable06 = mapJson.get("LOTTABLE06");
            String eLottable05 = UtilHelper.getString(mapJson.get("ELOTTABLE05"));//复测期，停止发运期
            String eLottable11 = UtilHelper.getString(mapJson.get("ELOTTABLE11"));//有效期
            if(!UtilHelper.isEmpty(eLottable05) && !UtilHelper.isEmpty(eLottable11) && eLottable05.compareTo(eLottable11) > 0){
                throw new Exception("复测期不能大于有效期");
            }

            if (UtilHelper.isEmpty(lottable06)) ExceptionHelper.throwRfFulfillLogicException("所选生成放行单的批号不能为空");

            //放行单号
            String releaseKey = LegecyUtilHelper.To_Char(IdGenerationHelper.getNCounter( "QA_RELEASE"),10);  //获取新的请检单号

            String STORERKEY= DBHelper.getStringValue( "SELECT UDF1 FROM CODELKUP WHERE LISTNAME=? AND CODE=?", new String[]{"SYSSET","STORERKEY"}, ""); //取仓库默认货主


            Map<String,String> skuHashMap = SKU.findById(mapJson.get("SKU"),true);

          //  String SUPPLIERNAME ="";
          /*  if(!UtilHelper.isEmpty(mapJson.get("ELOTTABLE08"))) {

                SUPPLIERNAME = XtSql.GetValue(context                        "SELECT s.COMPANY SUPPLIERNAME FROM STORER s WHERE s.[TYPE] = 5 AND s.STORERKEY = ? ", new String[]{mapJson.get("ELOTTABLE08")}," ");

            }*/

            //取库存的信息
            Map<String,String> record= DBHelper.getRecord( "SELECT COUNT(1) AS CNT,SUM(QTY) AS QTY FROM LOTXLOCXID A,V_LOTATTRIBUTE B"
                        + " WHERE A.LOT=B.LOT AND QTY>0 AND B.LOTTABLE06=?", new Object[]{lottable06},"库存明细");

            String barrelNum = record.get("CNT"); //桶数
            String totalQty = record.get("QTY");//数量



            //创建请检单
            Map<String,String> mRELEASE=new HashMap<String,String>();
            mRELEASE.put("ADDWHO", userid);  //创建人
            mRELEASE.put("EDITWHO", userid);  //更新人

            mRELEASE.put("RELEASEKEY", releaseKey);  //放行号
            mRELEASE.put("LOTTABLE06", lottable06);  //批次号
            mRELEASE.put("STORERKEY", STORERKEY);   //货主
            mRELEASE.put("SKU", mapJson.get("SKU"));   //物料代码
            mRELEASE.put("DESCR", skuHashMap.get("DESCR"));   //物料名称
            mRELEASE.put("FROMQUALITYSTATUS", mapJson.get("FROMELOTTABLE03"));  //原质量状态
            mRELEASE.put("QUALITYSTATUS", mapJson.get("ELOTTABLE03"));  //质量状态
            mRELEASE.put("INITIALRELEASE", mapJson.get("ISRETESTRELEASE"));  //是否复测放行

            mRELEASE.put("ELOTTABLE01", mapJson.get("ELOTTABLE01"));   //账册号
            mRELEASE.put("ELOTTABLE02", mapJson.get("ELOTTABLE02"));   //保税状态
            mRELEASE.put("ELOTTABLE04", mapJson.get("ELOTTABLE04"));
            mRELEASE.put("LOTTABLE05", "@date|"+mapJson.get("ELOTTABLE05"));   //复测期默认值
            mRELEASE.put("ELOTTABLE07", mapJson.get("ELOTTABLE07"));    //
            mRELEASE.put("ELOTTABLE09", mapJson.get("ELOTTABLE09"));    //
            mRELEASE.put("LOTTABLE11", "@date|"+mapJson.get("ELOTTABLE11"));   //有效期默认值
            mRELEASE.put("ELOTTABLE19", mapJson.get("ELOTTABLE19"));    //
            mRELEASE.put("ELOTTABLE20", mapJson.get("ELOTTABLE20"));    // 供应商名称
            mRELEASE.put("MANUFACTURERNAME", mapJson.get("ELOTTABLE14"));    // 生产商名称
            mRELEASE.put("FROMELOTTABLE01", mapJson.get("FROMELOTTABLE01"));    //
            mRELEASE.put("FROMELOTTABLE02", mapJson.get("FROMELOTTABLE02"));    //
            mRELEASE.put("FROMELOTTABLE04", mapJson.get("FROMELOTTABLE04"));    //
            mRELEASE.put("FROMLOTTABLE05", "@date|"+mapJson.get("FROMELOTTABLE05"));   //原复测期
            mRELEASE.put("FROMELOTTABLE07", mapJson.get("FROMELOTTABLE07"));    //
            mRELEASE.put("FROMELOTTABLE09", mapJson.get("FROMELOTTABLE09"));    //
            mRELEASE.put("FROMLOTTABLE11", "@date|"+mapJson.get("FROMELOTTABLE11"));   //原有效期
            mRELEASE.put("FROMELOTTABLE19", mapJson.get("FROMELOTTABLE19"));    //
            mRELEASE.put("FROMELOTTABLE20", mapJson.get("FROMELOTTABLE20"));    //

            mRELEASE.put("PIECE", barrelNum);   //桶数
            mRELEASE.put("QUANTITY", totalQty);   //数量


            mRELEASE.put("SUPPLIERLOT", mapJson.get("ELOTTABLE09"));   //厂家批号

            mRELEASE.put("SKUTYPE", skuHashMap.get("BUSR4"));  // 物料类型
            mRELEASE.put("NOTES", mapJson.get("NOTES"));  // 物料类型
            mRELEASE.put("STATUS", "1");  // 放行状态
            //取放行检测内容项目
            LegacyDBHelper.ExecInsert( "RELEASE", mRELEASE);  //放行单写入数据库

            //更新库存批次对应的质量状态
            DBHelper.executeUpdate( "UPDATE ENTERPRISE.ELOTATTRIBUTE SET ELOTTABLE13=ELOTTABLE13 + ?  WHERE ELOT=? "
                    , new Object[]{mapJson.get("ISRETESTRELEASE"),lottable06});
         /*   //elottable21 记录首次放行质量状态
            String elotTable21=DBHelper.getStringValue("select ELOTTABLE21 from ENTERPRISE.ELOTATTRIBUTE where ELOT=?",new Object[]{
                    lottable06)
            },"批属性");
            if (UtilHelper.isEmpty(elotTable21)){
                DBHelper.executeUpdate( "UPDATE ENTERPRISE.ELOTATTRIBUTE SET EDITWHO=?, EDITDATE=?,ELOTTABLE21=? WHERE ELOT=? "
                        , new Object[]{userid),UtilHelper.getCurrentSqlDate(),mapJson.get("ELOTTABLE03")),lottable06)});
            }*/

            Udtrn udtrn=new Udtrn();
            udtrn.EsignatureKey=eSignatureKey;
            udtrn.FROMTYPE="QA放行";
            udtrn.FROMTABLENAME="RELEASE";
            udtrn.FROMKEY=lottable06; //批号
            udtrn.FROMKEY1=releaseKey;
            udtrn.FROMKEY2="";
            udtrn.FROMKEY3="";
            udtrn.TITLE01="收货批次";    udtrn.CONTENT01=lottable06;
            udtrn.TITLE02="放行单号";    udtrn.CONTENT02=releaseKey;
            udtrn.TITLE03="质量状态";    udtrn.CONTENT03= mapJson.get("ELOTTABLE03");
            udtrn.TITLE04="保税状态";    udtrn.CONTENT04= mapJson.get("ELOTTABLE02");
            udtrn.TITLE05="账册号";    udtrn.CONTENT05= mapJson.get("ELOTTABLE01");
            udtrn.TITLE06="偏差号";    udtrn.CONTENT06= mapJson.get("ELOTTABLE19");
            udtrn.TITLE07="变更号";    udtrn.CONTENT07= mapJson.get("ELOTTABLE20");
            udtrn.TITLE08="生产批号";    udtrn.CONTENT08= mapJson.get("ELOTTABLE07");
            udtrn.TITLE09="物料批号/供应商批号";    udtrn.CONTENT09= mapJson.get("ELOTTABLE09");
            AuditService.doAudit(udtrn);

            //创建反馈对象
            ServiceDataMap theOutDO = new ServiceDataMap();
            theOutDO.setAttribValue("RELEASEKEY", releaseKey);
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

