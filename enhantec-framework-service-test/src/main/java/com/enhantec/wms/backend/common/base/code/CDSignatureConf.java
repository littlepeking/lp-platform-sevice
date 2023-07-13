package com.enhantec.wms.backend.common.base.code;


import com.enhantec.wms.backend.framework.Context;
import com.enhantec.wms.backend.common.base.CodeLookup;

import java.sql.Connection;
import java.util.Map;

public class CDSignatureConf {
    /**
     * 全部接收签名配置
     */
    public static Map<String,String> getReceiveAllSignatureConf(Context context, Connection connection){
        return CodeLookup.getCodeLookupByKey(context,connection,"ESIGNATURE","RECEIVEALL");
    }

    /**
     * 是否在全部接收时复核ASN
     */
    public static boolean confirmAsnWhenReceiveAll(Context context,Connection connection){
        return "Y".equalsIgnoreCase(getReceiveAllSignatureConf(context,connection).get("UDF2"));
    }
}
