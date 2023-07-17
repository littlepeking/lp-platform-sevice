package com.enhantec.wms.backend.qa;


import com.enhantec.framework.common.utils.EHContextHelper;
import com.enhantec.wms.backend.utils.common.LegacyDBHelper;
import com.enhantec.wms.backend.framework.LegacyBaseService;
import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.framework.ServiceDataMap;
import com.enhantec.wms.backend.utils.common.*;

import com.enhantec.framework.common.utils.EHContextHelper;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class CreateRelease  extends LegacyBaseService {


/**
 * @author ：John
 * @description：创建放行单
 */

    /**
     --注册方法  创建放行单
     delete from wmsadmin.sproceduremap where THEPROCNAME='EHCreateRelease'
     insert into wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION, ADDWHO, EDITWHO, PARAMETERS,LOAD,ISBATCH)
     values ('EHCreateRelease', 'com.enhantec.sce.qa', 'enhantec', 'CreateRelease', 'TRUE',  'JOHN',  'JOHN',
     'sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,LOTTABLE06','0.10','0');
     */


    private static final long serialVersionUID = 1L;



    public void execute(ServiceDataHolder serviceDataHolder)
    {


        String userid = EHContextHelper.getUser().getUsername();  //当用户
        
        try
        {
            String LOTTABLE06= serviceDataHolder.getInputDataAsMap().getString("LOTTABLE06"); //传入批次
            if (UtilHelper.isEmpty(LOTTABLE06)) ExceptionHelper.throwRfFulfillLogicException("所选生成放行单的批号不能为空");

            //放行单号
            String RELEASEKEY= LegecyUtilHelper.To_Char(IdGenerationHelper.getNCounter( "QA_RELEASE"),10);  //获取新的请检单号

            String STORERKEY= DBHelper.getValue( "SELECT UDF1 FROM CODELKUP WHERE LISTNAME=? AND CODE=?", new String[]{"SYSSET","STORERKEY"}, ""); //取仓库默认货主
            
            //取批次的相关信息
            HashMap<String,String> FromLot= DBHelper.getRecord( "SELECT A.BUSR4 SKUTYPE, A.SKU , LA.ELOTTABLE02, LA.ELOTTABLE04, LA.ELOTTABLE03 QUALITYSTATUS,LA.ELOTTABLE01,LA.ELOTTABLE19,LA.ELOTTABLE20,LA.ELOTTABLE07, " +
              " LA.ELOTTABLE09 SUPPLIERLOT," +
              " LA.ELOTTABLE08 AS SUPPLIERCODE," +
              " FORMAT(ELOTTABLE05, 'yyyy-MM-dd HH:mm:ss') AS ELOTTABLE05, FORMAT(ELOTTABLE11,'yyyy-MM-dd HH:mm:ss') AS ELOTTABLE11 , A.DESCR" +
              " FROM SKU A, V_LOTATTRIBUTE LA " +
              " WHERE A.SKU = LA.SKU " +
              " AND LA.STORERKEY=? AND LA.LOTTABLE06=?", new String[]{STORERKEY,LOTTABLE06});
            if(FromLot.size()==0) ExceptionHelper.throwRfFulfillLogicException("找不到该批号");

            String SUPPLIERNAME = " ";

            if(!UtilHelper.isEmpty(FromLot.get("ELOTTABLE08"))) {

                SUPPLIERNAME = DBHelper.getValue( "SELECT s.COMPANY SUPPLIERNAME FROM STORER s WHERE s.[TYPE] = 5 AND s.STORERKEY = ? ", new String[]{FromLot.get("ELOTTABLE08")}," ");

            }

            //取库存的信息
            HashMap<String,String> record= DBHelper.getRecord( "SELECT COUNT(1) AS CNT,SUM(QTY) AS QTY FROM LOTXLOCXID A,V_LOTATTRIBUTE B"
                        + " WHERE A.LOT=B.LOT AND QTY>0 AND B.LOTTABLE06=?", new Object[]{LOTTABLE06},"库存明细");

                String barrelNum = record.get("CNT"); //取库存桶数
                String totalQty = record.get("QTY");



            //创建请检单
            HashMap<String,String> mRELEASE=new HashMap<String,String>();
            mRELEASE.put("ADDWHO", userid);  //创建人
            mRELEASE.put("EDITWHO", userid);  //更新人

            mRELEASE.put("RELEASEKEY", RELEASEKEY);  //放行号
            mRELEASE.put("LOTTABLE06", LOTTABLE06);  //批次号
            mRELEASE.put("STORERKEY", STORERKEY);   //货主
            mRELEASE.put("SKU", FromLot.get("SKU"));   //物料代码
            mRELEASE.put("DESCR", FromLot.get("DESCR"));   //物料名称
            mRELEASE.put("FROMQUALITYSTATUS", FromLot.get("QUALITYSTATUS"));  //原质量状态
            mRELEASE.put("QUALITYSTATUS", FromLot.get("QUALITYSTATUS"));  //质量状态
            mRELEASE.put("INITIALRELEASE", "0");  //是否复测放行

            mRELEASE.put("FROMLOTTABLE05", "@date|"+FromLot.get("ELOTTABLE05"));   //原复测期
            mRELEASE.put("FROMLOTTABLE11", "@date|"+FromLot.get("ELOTTABLE11"));   //原有效期
            mRELEASE.put("LOTTABLE05", "@date|"+FromLot.get("ELOTTABLE05"));   //复测期默认值
            mRELEASE.put("LOTTABLE11", "@date|"+FromLot.get("ELOTTABLE11"));   //有效期默认值
            mRELEASE.put("ELOTTABLE02", FromLot.get("ELOTTABLE02"));   //保税状态
            mRELEASE.put("ELOTTABLE01", FromLot.get("ELOTTABLE01"));   //账册号
            mRELEASE.put("ELOTTABLE07", FromLot.get("ELOTTABLE07"));    //
            mRELEASE.put("ELOTTABLE09", FromLot.get("SUPPLIERLOT"));    //
            mRELEASE.put("FROMELOTTABLE07", FromLot.get("ELOTTABLE07"));    //
            mRELEASE.put("FROMELOTTABLE09", FromLot.get("SUPPLIERLOT"));    //
            mRELEASE.put("FROMELOTTABLE01", FromLot.get("ELOTTABLE01"));    //
            mRELEASE.put("FROMELOTTABLE02", FromLot.get("ELOTTABLE02"));    //
            mRELEASE.put("FROMELOTTABLE19", FromLot.get("ELOTTABLE19"));    //
            mRELEASE.put("FROMELOTTABLE20", FromLot.get("ELOTTABLE20"));    //
            mRELEASE.put("ELOTTABLE19", FromLot.get("ELOTTABLE19"));    //
            mRELEASE.put("ELOTTABLE20", FromLot.get("ELOTTABLE20"));    //
            mRELEASE.put("ELOTTABLE04", FromLot.get("ELOTTABLE04"));    //
            mRELEASE.put("FROMELOTTABLE04", FromLot.get("ELOTTABLE04"));    //

            mRELEASE.put("PIECE", barrelNum);   //桶数
            mRELEASE.put("QUANTITY", totalQty);   //数量

            mRELEASE.put("SUPPLIERNAME", SUPPLIERNAME);  // 供应商名称

            mRELEASE.put("SUPPLIERLOT", FromLot.get("SUPPLIERLOT"));   //厂家批号

            mRELEASE.put("SKUTYPE", FromLot.get("SKUTYPE"));  // 物料类型

            //取放行检测内容项目
            LegacyDBHelper.ExecInsert( "RELEASE", mRELEASE);  //放行单写入数据库

            //创建反馈对象

            //创建反馈对象
            ServiceDataMap theOutDO = new ServiceDataMap();
            theOutDO.setAttribValue("RELEASEKEY", RELEASEKEY);
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

