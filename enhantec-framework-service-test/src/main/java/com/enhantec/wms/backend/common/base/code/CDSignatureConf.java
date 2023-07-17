package com.enhantec.wms.backend.common.base.code;


import com.enhantec.framework.common.utils.EHContextHelper;
import com.enhantec.wms.backend.common.base.CodeLookup;

import com.enhantec.framework.common.utils.EHContextHelper;
import java.util.Map;

public class CDSignatureConf {
    /**
     * 全部接收签名配置
     */
    public static Map<String,String> getReceiveAllSignatureConf(){
        return CodeLookup.getCodeLookupByKey("ESIGNATURE","RECEIVEALL");
    }

    /**
     * 是否在全部接收时复核ASN
     */
    public static boolean confirmAsnWhenReceiveAll(){
        return "Y".equalsIgnoreCase(getReceiveAllSignatureConf().get("UDF2"));
    }
}
