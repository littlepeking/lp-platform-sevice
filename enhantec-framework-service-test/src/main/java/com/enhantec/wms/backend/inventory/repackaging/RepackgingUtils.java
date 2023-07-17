package com.enhantec.wms.backend.inventory.repackaging;

import com.enhantec.wms.backend.common.outbound.Orders;
import com.enhantec.framework.common.utils.EHContextHelper;
import com.enhantec.wms.backend.utils.common.UtilHelper;

import com.enhantec.framework.common.utils.EHContextHelper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

public class RepackgingUtils {

    public static List<HashMap<String,String>> getLeftLPNListFromStr(String leftLPNStr) {


        List<HashMap<String,String>> list =new ArrayList<>();
        if(!UtilHelper.isEmpty(leftLPNStr)){

            String[] lpnInfoStrArray =  leftLPNStr.split(";");
            for(int i=0;i<lpnInfoStrArray.length;i++){
                String lpnInfoStr = lpnInfoStrArray[i];
                String[] lpnFields = lpnInfoStr.split(Pattern.quote("|"));
                String lpn = lpnFields[0];
                String leftQty = lpnFields[1];
                String uom = lpnFields[2];
                HashMap<String,String> lpnInfo = new HashMap<>();
                lpnInfo.put("ID",lpn);
                lpnInfo.put("LEFTQTY",leftQty);
                lpnInfo.put("UOM",uom);
                list.add(lpnInfo);
            }

        }

        return list;
    }

    public static boolean isInRepackProcess( String orderKey, String orderLineNumber){

        HashMap<String,String> orderDetailHashMap = Orders.findOrderDetailByKey(orderKey,orderLineNumber,true);

        //如果repackOrderKey不为空，说明单据已经创建但未自动完成后续操作，需要手工执行。
        return !UtilHelper.isEmpty(orderDetailHashMap.get("SUSR2"));

    }

}
