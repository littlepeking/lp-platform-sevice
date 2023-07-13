package com.enhantec.wms.backend.outbound.ship;

import com.enhantec.wms.backend.common.base.IDNotes;
import com.enhantec.wms.backend.common.inventory.LotxLocxId;
import com.enhantec.wms.backend.common.outbound.PickDetail;
import com.enhantec.wms.backend.framework.LegacyBaseService;import com.enhantec.wms.backend.framework.Context;import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.outbound.utils.OrderValidationHelper;
import com.enhantec.wms.backend.utils.audit.Udtrn;
import com.enhantec.wms.backend.utils.common.*;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;


/**
 --注册方法

 DELETE FROM SCPRDMST.wmsadmin.sproceduremap WHERE THEPROCNAME = 'EHShipById'
 insert into SCPRDMST.wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION,  ADDWHO,  EDITWHO, PARAMETERS,LOAD,ISBATCH)
 values ('EHShipById', 'com.enhantec.sce.outbound.order.ship', 'enhantec', 'ShipById', 'FALSE', 'JOHN',  'JOHN', 'sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,fromid,esignaturekey','0.10','0');


 */

public class ShipById extends LegacyBaseService {

    @Override
    public void execute(ServiceDataHolder serviceDataHolder) {

        Context context = null;
        Connection conn = null;

        try {

            String userid = context.getUserID();

            conn = context.getConnection();

            String fromId = serviceDataHolder.getInputDataAsMap().getString("fromid");

            boolean isBoxId = IDNotes.isBoxId(context, conn, fromId);

            String orderKey = "";


        /*
        0	正常        1	已发放        3	处理中        4	已结        5	拣货量
        6	已包装        7	已分类        8	装载量        9	发货量        */
        //
        //            if (pickDetailInfo.get("STATUS").equals("9")) {
        //                ExceptionHelper.throwRfFulfillLogicException("容器条码不能重复发货");
        //            }
        //            if (!(pickDetailInfo.get("STATUS").equals("5")
        //                    || pickDetailInfo.get("STATUS").equals("6")
        //                    || pickDetailInfo.get("STATUS").equals("7")
        //                    || pickDetailInfo.get("STATUS").equals("8"))) {
        //                ExceptionHelper.throwRfFulfillLogicException("容器条码未拣货不能发运");
        //            }

            if(isBoxId) {

                List<HashMap<String, String>> pickDetailInfos = LotxLocxId.findPickedIdsByParentId(context,conn,fromId);

                OrderValidationHelper.checkOrderTypeAndQualityStatusByPickDetailKey(context,conn,pickDetailInfos.get(0).get("PICKDETAILKEY"));

                orderKey = pickDetailInfos.get(0).get("ORDERKEY");

                for( HashMap<String, String> pickDetailInfo: pickDetailInfos){
                    shipSingleLpn(context,conn,pickDetailInfo);
                }


            }else {

                HashMap<String, String> pickDetailInfo = PickDetail.findPickedLpn(context, conn, fromId, true);

                OrderValidationHelper.checkOrderTypeAndQualityStatusByPickDetailKey(context,conn,pickDetailInfo.get("PICKDETAILKEY"));

                orderKey = pickDetailInfo.get("ORDERKEY");

                shipSingleLpn(context,conn,pickDetailInfo);

            }




            String esignaturekey = serviceDataHolder.getInputDataAsMap().getString("esignaturekey");
            Udtrn UDTRN = new Udtrn();
            UDTRN.EsignatureKey = esignaturekey;
            UDTRN.FROMTYPE = "按容器条码发运";
            UDTRN.FROMTABLENAME = "PICKDETAIL";
            UDTRN.FROMKEY = fromId;
            UDTRN.FROMKEY1 = "";
            UDTRN.FROMKEY2 = "";
            UDTRN.FROMKEY3 = "";
            UDTRN.TITLE01 = "订单号";
            UDTRN.CONTENT01 = orderKey;
            UDTRN.TITLE02 = "容器条码/箱号";
            UDTRN.CONTENT02 = fromId;
            conn = context.getConnection();
            UDTRN.Insert(context, conn, context.getUserID());


        }catch (Exception e)
            {
                try
                {
                    context.releaseConnection(conn);
                }	catch (Exception e1) {		}
                if ( e instanceof FulfillLogicException)
                    throw (FulfillLogicException)e;
                else
                    throw new FulfillLogicException(e.getMessage());
            }
        finally {
//            if (context.theSQLMgr.isActive()) {
//                context.theSQLMgr.transactionCommit();
//            }
            try	{	context.releaseConnection(conn); }	catch (Exception e1) {		}
        }
    }

    private void shipSingleLpn(Context context, Connection conn, HashMap<String, String> pickDetailInfo ) throws Exception {

        conn = context.getConnection();
        DBHelper.executeUpdate(context,conn,"UPDATE DROPID SET STATUS = '0' WHERE DROPID = ? ", new Object[]{pickDetailInfo.get("ID")});

        //conn = context.getConnection();
        HashMap<String,String> shippedIdNotesHashMap = IDNotes.decreaseWgtById(context,conn,new BigDecimal(pickDetailInfo.get("QTY")), pickDetailInfo.get("ID"));
        if(UtilHelper.decimalStrCompare(shippedIdNotesHashMap.get("NETWGT"), "0")>0)
            ExceptionHelper.throwRfFulfillLogicException("待发运的容器条码/箱号"+ pickDetailInfo.get("ID")+"发运时扣库存异常(不为零)，发运失败");
        //校验发运的IDNOTES库存余额应为0并移至历史表，否则报错
        IDNotes.archiveIDNotes(context, conn, shippedIdNotesHashMap);

        //TODO:通过修改拣货明细状态的方式发运，看是否还有事务问题
        ServiceDataHolder inboundDo = new ServiceDataHolder();

        inboundDo.getInputDataAsMap().setAttribValue("ttm", "N");
        inboundDo.getInputDataAsMap().setAttribValue("fromid", pickDetailInfo.get("ID"));
        inboundDo.getInputDataAsMap().setAttribValue("fromloc", "");
        inboundDo.getInputDataAsMap().setAttribValue("toid", "");
        inboundDo.getInputDataAsMap().setAttribValue("toloc", "");
        inboundDo.getInputDataAsMap().setAttribValue("action", "SHIP");
        inboundDo.getInputDataAsMap().setAttribValue("confirm", "");
        inboundDo.getInputDataAsMap().setAttribValue("carrierid", "");
        inboundDo.getInputDataAsMap().setAttribValue("cartongroup", "");
        inboundDo.getInputDataAsMap().setAttribValue("cartontype", "");
        inboundDo.getInputDataAsMap().setAttribValue("islabelsshipped", "N");
//
//        context.theEXEDataObjectStack.stackList.clear();
//        context.theEXEDataObjectStack.stackList.add(inboundDo);

       // todo ship: super.execute(inboundDo);
    }


}
