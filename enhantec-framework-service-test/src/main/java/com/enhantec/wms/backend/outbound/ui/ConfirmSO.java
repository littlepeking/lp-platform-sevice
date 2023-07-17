
package com.enhantec.wms.backend.outbound.ui;

import com.enhantec.wms.backend.common.base.CodeLookup;
import com.enhantec.wms.backend.common.outbound.Orders;
import com.enhantec.wms.backend.framework.LegacyBaseService;import com.enhantec.framework.common.utils.EHContextHelper;import com.enhantec.wms.backend.framework.ServiceDataHolder;
import com.enhantec.wms.backend.utils.audit.Udtrn;
import com.enhantec.wms.backend.utils.common.*;

import com.enhantec.framework.common.utils.EHContextHelper;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class ConfirmSO extends LegacyBaseService {


    /**
     * --注册方法
     delete from SCPRDMST.wmsadmin.sproceduremap where THEPROCNAME='EHConfirmSO'
     insert into SCPRDMST.wmsadmin.sproceduremap (THEPROCNAME, THEDOMAIN, THEFUNCTIONTYPE, COMPOSITE, STARTTRANSACTION, ADDWHO, EDITWHO, PARAMETERS,LOAD,ISBATCH)
     values ('EHConfirmSO', 'com.enhantec.sce.outbound.order.ui', 'enhantec', 'ConfirmSO', 'TRUE', 'JOHN', 'JOHN'
     , 'sendDelimiter,ptcid,userid,taskId,databasename,appflag,recordType,server,ORDERKEY,ESIGNATUREKEY','0.10','0');
     */


    private static final long serialVersionUID = 1L;

    public ConfirmSO() {
    }

    public void execute(ServiceDataHolder serviceDataHolder) {
        String userid = EHContextHelper.getUser().getUsername();


        try {



            String ORDERKEY = serviceDataHolder.getInputDataAsMap().getString("ORDERKEY");
            String esignatureKey = serviceDataHolder.getInputDataAsMap().getString("ESIGNATUREKEY");


            HashMap<String, String>  ORDERSInfo = Orders.findByOrderKey( ORDERKEY, true);

            String isTransferOrderType = DBHelper.getValue( "SELECT EXT_UDF_STR5 FROM CODELKUP WHERE LISTNAME = 'ORDERTYPE' AND CODE = ?",
                    new Object[]{ORDERSInfo.get("TYPE")},String.class,"",false);

            /**
             * 转仓出库类型校验
             */
            if("Y".equals(isTransferOrderType)){
                checkTransferOrder(ORDERSInfo);
            }

            //1) 初始状态： 确认按钮为可编辑状态； 取消按钮为不可编辑状态；
            //2）点击确认按钮：弹出电子签名，验证成功后复核状态变更为已确认；
            // 确认按钮为可编辑状态； 取消按钮为可编辑状态；
            //3）再次点击确认按钮，弹出电子签名，验证陈宫后复核状态变更为已复核。
            // 确认按钮为不可编辑状态； 取消按钮为可编辑状态；
            //4） 点击取消确认，弹出电子签名，验证成功后复核状态变更为未确认；

            //0：未确认 1：已确认 2：已复核

            if(esignatureKey.indexOf(':')==-1) {
                String ISCONFIRMEDUSER = DBHelper.getValue( "SELECT SIGN FROM ESIGNATURE e WHERE SERIALKEY = ? ", new Object[]{
                        esignatureKey
                }, String.class, "复核人");

                //复核操作校验
                if (!UtilHelper.isEmpty(ORDERSInfo.get("ISCONFIRMEDUSER"))) {

                    if (ORDERSInfo.get("ISCONFIRMEDUSER").equals(ISCONFIRMEDUSER)) {
                        ExceptionHelper.throwRfFulfillLogicException("复核人和确认人不能为同一人");
                    } else {
                        //复核操作
                        DBHelper.executeUpdate( "UPDATE ORDERS SET ISCONFIRMEDUSER2 = ? , ISCONFIRMED = 2 WHERE ORDERKEY = ? ",
                                new Object[]{ISCONFIRMEDUSER, ORDERKEY});
                    }
                } else {
                    //确认操作
                    DBHelper.executeUpdate( "UPDATE ORDERS SET ISCONFIRMEDUSER = ? , ISCONFIRMED = 1 WHERE ORDERKEY = ? ",
                            new Object[]{ISCONFIRMEDUSER, ORDERKEY});
                }


                Udtrn UDTRN = new Udtrn();

                if (ORDERSInfo.get("ISCONFIRMED").equals("0")) {
                    UDTRN.EsignatureKey = esignatureKey;
                } else {//复核
                    UDTRN.EsignatureKey1 = esignatureKey;
                }

                UDTRN.FROMTYPE = ORDERSInfo.get("ISCONFIRMED").equals("0") ? "确认出库订单" : "复核出库订单";
                UDTRN.FROMTABLENAME = "ORDERS";
                UDTRN.FROMKEY = ORDERKEY;
                UDTRN.FROMKEY1 = "";
                UDTRN.FROMKEY2 = "";
                UDTRN.FROMKEY3 = "";
                UDTRN.TITLE01 = "出库订单号";
                UDTRN.CONTENT01 = ORDERKEY;
                UDTRN.TITLE02 = "确认状态";
                UDTRN.CONTENT02 = "Y";
                UDTRN.Insert( userid);
            }else{
                /**
                 * 增加RF的确认，这里想着可以不在这加，而放到RF端，签名后调用后台两次，暂时先这么写，回头确认一下
                 */
                String[] eSignatureKeys = esignatureKey.split(":");
                String eSignatureKey1=eSignatureKeys[0];
                String eSignatureKey2=eSignatureKeys[1];

                String isConfirmedUser1 = DBHelper.getValue( "SELECT SIGN FROM ESIGNATURE e WHERE SERIALKEY = ? ", new Object[]{
                        eSignatureKey1
                }, String.class, "确认人");

                String isConfirmedUser2 = DBHelper.getValue( "SELECT SIGN FROM ESIGNATURE e WHERE SERIALKEY = ? ", new Object[]{
                        eSignatureKey2
                }, String.class, "复核人");

                //复核操作
                DBHelper.executeUpdate( "UPDATE ORDERS SET ISCONFIRMEDUSER = ? ,ISCONFIRMEDUSER2 = ? , ISCONFIRMED = 2 WHERE ORDERKEY = ? ",
                        new Object[]{isConfirmedUser1,isConfirmedUser2, ORDERKEY});

                Udtrn UDTRN = new Udtrn();
                UDTRN.FROMTYPE = "确认并复核出库单";
                UDTRN.FROMTABLENAME = "ORDERS";
                UDTRN.FROMKEY = ORDERKEY;
                UDTRN.FROMKEY1 = "";
                UDTRN.FROMKEY2 = "";
                UDTRN.FROMKEY3 = "";
                UDTRN.TITLE01 = "出库订单号";
                UDTRN.CONTENT01 = ORDERKEY;
                UDTRN.TITLE02 = "确认状态";
                UDTRN.CONTENT02 = "Y";
                UDTRN.TITLE03 = "确认人";
                UDTRN.CONTENT03 = isConfirmedUser1;
                UDTRN.TITLE04 = "复核人";
                UDTRN.CONTENT04 = isConfirmedUser2;
                UDTRN.Insert( userid);
            }


        }catch (Exception e){
            
            if ( e instanceof FulfillLogicException)
                throw (FulfillLogicException)e;
            else
                throw new FulfillLogicException( e.getMessage());

        }finally {
            
        }
    }

    private void checkTransferOrder( HashMap<String,String> orderInfo){
        boolean isTransferGmp = "Y".equalsIgnoreCase(CodeLookup.getCodeLookupByKey("WHTRANFER",orderInfo.get("TOWAREHOUSE")).get("UDF1"));
        if(isTransferGmp) {
            List<HashMap<String, String>> orderDetails = Orders.findOrderDetailsByOrderKey( orderInfo.get("ORDERKEY"), false);
            if (orderDetails.size() != 0) {
                HashSet<String> lotTable06 = new HashSet<>();
                orderDetails.forEach(orderDetail -> {
                    lotTable06.add(orderDetail.get("LOTTABLE06"));
                    String sql = "SELECT SKU FROM " + orderInfo.get("TOWAREHOUSE") + ".SKU WHERE SKU = ? AND EXT_UDF_STR3 = ?";
                    List<HashMap<String, String>> toWareHouseSku = DBHelper.executeQuery( sql,
                            new Object[]{orderInfo.get("TOGMPSKU"), orderDetail.get("SKU")});
                    if (null == toWareHouseSku || toWareHouseSku.size() == 0) {
                        ExceptionHelper.throwRfFulfillLogicException("目标GMP仓库编码信息不存在");
                    }
                });
                if (lotTable06.size() > 1) {
                    ExceptionHelper.throwRfFulfillLogicException("此类出库单不支持多批同时转库");
                }
            }
        }
    }
}