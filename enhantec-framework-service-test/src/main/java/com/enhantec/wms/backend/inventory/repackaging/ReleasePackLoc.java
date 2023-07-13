package com.enhantec.wms.backend.inventory.repackaging;

import com.enhantec.wms.backend.common.outbound.Orders;
import com.enhantec.wms.backend.framework.LegacyBaseService;
import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.utils.audit.Udtrn;
import com.enhantec.wms.backend.utils.common.*;

import java.sql.Connection;
import java.util.HashMap;

/**
 --注册方法

 delete from SCPRDMST.wmsadmin.sproceduremap where THEPROCNAME = 'EHReleasePackLoc';
 insert into SCPRDMST.wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION, ADDWHO, EDITWHO, PARAMETERS,LOAD,ISBATCH)
 values ('EHReleasePackLoc', 'com.enhantec.sce.inventory.repackaging', 'enhantec', 'ReleasePackLoc','TRUE','JOHN','JOHN'
 , 'sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,ORDERKEY,ORDERLINENUMBER,ESIGNATUREKEY','0.10','0');

 **/

public class ReleasePackLoc extends LegacyBaseService {

    private static final long serialVersionUID = 1L;

    public ReleasePackLoc()
    {
    }

    public void execute(ServiceDataHolder serviceDataHolder)
    {
        String userid = context.getUserID();

        Connection conn = null;

        try
        {
            conn = context.getConnection();
            final String orderKey = serviceDataHolder.getInputDataAsMap().getString("ORDERKEY");
            final String orderLineNumber = serviceDataHolder.getInputDataAsMap().getString("ORDERLINENUMBER");
            final String esignatureKey= serviceDataHolder.getInputDataAsMap().getString("ESIGNATUREKEY");

            if (UtilHelper.isEmpty(orderKey)) throw new Exception("订单号不能为空");
            if (UtilHelper.isEmpty(orderLineNumber)) throw new Exception("订单行号不能为空");
            if (UtilHelper.isEmpty(esignatureKey)) throw new Exception("电子签名不能为空");

            HashMap<String,String> orderDetailHashMap = Orders.findOrderDetailByKey(context,conn,orderKey,orderLineNumber,true);

            String repackReceiptKey = orderDetailHashMap.get("SUSR1");

            String rePackOrderKey = orderDetailHashMap.get("SUSR2");

            String currentPackLoc = orderDetailHashMap.get("SUSR3");


            if(!UtilHelper.isEmpty(repackReceiptKey)) ExceptionHelper.throwRfFulfillLogicException("已存在分装收货单，请先取消分装后，再释放分装间");
            if(!UtilHelper.isEmpty(rePackOrderKey)) ExceptionHelper.throwRfFulfillLogicException("分装在执行过程中，不允许取消");

            // SUSR1 分装入库单号
            // SUSR2 分装出库单号
            //SUSR3 当前正在使用的分装间
            // SUSR4 已分装完成的单据列表, 格式:  收货批次1|分装入库单号1|分装出库单号1 ; 收货批次2|分装入库单号2分装出库单号2;
            DBHelper.executeUpdate(context,conn,"UPDATE ORDERDETAIL SET SUSR3 = null WHERE ORDERKEY = ? AND ORDERLINENUMBER = ? ",new Object[]{
                    orderKey ,  orderLineNumber });


            Udtrn UDTRN = new Udtrn();

            if(esignatureKey.indexOf(':')==-1){
                //取样自动生成
                UDTRN.EsignatureKey=esignatureKey;
            }else {
                //复核
                String[] eSignatureKeys = esignatureKey.split(":");
                UDTRN.EsignatureKey=eSignatureKeys[0];
                UDTRN.EsignatureKey1=eSignatureKeys[1];
            }

            UDTRN.EsignatureKey = esignatureKey;
            UDTRN.FROMTYPE = "释放分装间";
            UDTRN.FROMTABLENAME = "ORDERDETAIL";
            UDTRN.FROMKEY = currentPackLoc;
            UDTRN.FROMKEY1 = orderKey + orderLineNumber;
            UDTRN.FROMKEY2 = "";
            UDTRN.FROMKEY3 = "";
            UDTRN.TITLE01 = "出库单号及行号";
            UDTRN.CONTENT01 = orderKey + orderLineNumber;
            UDTRN.TITLE02 = "分装间";
            UDTRN.CONTENT02 = currentPackLoc;
            UDTRN.Insert(context, conn, userid);

          
        }
        catch (Exception e)
        {
            if ( e instanceof FulfillLogicException)
                throw (FulfillLogicException)e;
            else
                throw new FulfillLogicException(e.getMessage());
        }finally {
            try	{	context.releaseConnection(conn); }	catch (Exception e1) {		}
        }



    }

}