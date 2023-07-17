package com.enhantec.wms.backend.utils.print;

import com.enhantec.framework.common.utils.EHContextHelper;
import com.enhantec.wms.backend.utils.common.DBHelper;
import com.enhantec.wms.backend.utils.common.UtilHelper;

import com.enhantec.framework.common.utils.EHContextHelper;

public class LabelConfig {

    public  static String getLabelTemplatePrefix( String labelName)throws Exception{

        String templatePrefix = DBHelper.getValue( " SELECT LABELDESCR FROM LabelConfig WHERE LabelName = ? ", new Object[]{labelName}, String.class, "标签配置" + labelName);

        //如果没有配置则直接使用标签名称作为模板的前缀名称
        String result =  !UtilHelper.isEmpty(templatePrefix) ? templatePrefix : labelName;

        return result;
    }

}
