package com.enhantec.wms.ui.common.service;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.enhantec.framework.common.service.EHBaseService;
import com.enhantec.framework.common.utils.DSConstants;
import com.enhantec.wms.ui.common.model.SkuModel;

/**
* @author johnw
* @description 针对表【SKU】的数据库操作Service
* @createDate 2024-04-03 17:30:39
*/

@DS(DSConstants.DS_DEFAULT)
public interface SkuService extends EHBaseService<SkuModel> {

}
