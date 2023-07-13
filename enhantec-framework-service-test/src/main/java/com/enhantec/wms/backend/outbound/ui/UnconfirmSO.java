
package com.enhantec.wms.backend.outbound.ui;

import com.enhantec.wms.backend.common.outbound.Orders;
import com.enhantec.wms.backend.framework.LegacyBaseService;
import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.utils.audit.Udtrn;
import com.enhantec.wms.backend.utils.common.*;

import java.sql.Connection;
import java.util.HashMap;

public class UnconfirmSO extends LegacyBaseService {


    /**
     * --注册方法
     delete from SCPRDMST.wmsadmin.sproceduremap where THEPROCNAME='EHUnconfirmSO'
     insert into SCPRDMST.wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION, ADDWHO, EDITWHO, PARAMETERS,LOAD,ISBATCH)
     values ('EHUnconfirmSO', 'com.enhantec.sce.outbound.order.ui', 'enhantec', 'UnconfirmSO', 'TRUE', 'JOHN', 'JOHN'
     , 'sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,ORDERKEY,ESIGNATUREKEY','0.10','0');
     */

    private static final long serialVersionUID = 1L;

    public UnconfirmSO() {
    }

    public void execute(ServiceDataHolder serviceDataHolder) {
        String userid = context.getUserID();
        Connection conn = null;

        try {

            conn = context.getConnection();

            String ORDERKEY = serviceDataHolder.getInputDataAsMap().getString("ORDERKEY");
            String esignatureKey = serviceDataHolder.getInputDataAsMap().getString("ESIGNATUREKEY");

            HashMap<String, String>  orderInfo =  Orders.findByOrderKey(context,conn,ORDERKEY,true);

            //00	空订单			不存在明细
            //02	外部创建			订单已导入系统
            //04	内部创建			订单已在客户端创建
            //06	未分配			在明细项上运行分配，但未分配任何内容
            //08	已转换			订单已经自越库转换
            //09	未开始			此时无处理结果
            //-1	未知
            if(orderInfo.get("STATUS").equals("95")) ExceptionHelper.throwRfFulfillLogicException("订单已发货完成，不允许取消确认");

            // String isFromInterface = CodeLookup.getCodeLookupValue(context,conn,"RECEIPTYPE",orderInfo.get("TYPE"),"UDF4","收获类型");
            //  if("Y".equalsIgnoreCase(isFromInterface)) ExceptionHelper.throwRfFulfillLogicException("接口发送的出库指令不允许取消确认");

            DBHelper.executeUpdate(context, conn, "UPDATE ORDERS SET ISCONFIRMEDUSER ='', ISCONFIRMEDUSER2 = '', ISCONFIRMED = 0  WHERE ORDERKEY = ? ",
                    new Object[]{ ORDERKEY });


            Udtrn UDTRN=new Udtrn();
            UDTRN.EsignatureKey=esignatureKey;
            UDTRN.FROMTYPE="取消确认出库订单";
            UDTRN.FROMTABLENAME="ORDERS";
            UDTRN.FROMKEY=ORDERKEY;
            UDTRN.FROMKEY1="";
            UDTRN.FROMKEY2="";
            UDTRN.FROMKEY3="";
            UDTRN.TITLE01="出库单号";    UDTRN.CONTENT01=ORDERKEY;
            UDTRN.TITLE02="确认状态";    UDTRN.CONTENT02="N";
            UDTRN.Insert(context, conn, userid);






        }catch (Exception e){
            try	{	context.releaseConnection(conn); }	catch (Exception e1) {		}
            if ( e instanceof FulfillLogicException)
                throw (FulfillLogicException)e;
            else
                throw new FulfillLogicException( e.getMessage());

        }finally {
            try	{	context.releaseConnection(conn); }	catch (Exception e1) {		}
        }
    }
}