package com.enhantec.wms.ui.common.service;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.enhantec.framework.common.service.EHBaseService;
import com.enhantec.framework.common.utils.DSConstants;
import com.enhantec.wms.ui.common.model.StorerModel;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author johnw
* @description 针对表【STORER】的数据库操作Service
* @createDate 2024-04-04 19:21:00
*/

@DS(DSConstants.DS_DEFAULT)
public interface StorerService extends EHBaseService<StorerModel> {

}
