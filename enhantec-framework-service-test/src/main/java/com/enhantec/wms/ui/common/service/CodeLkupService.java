package com.enhantec.wms.ui.common.service;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.enhantec.framework.common.service.EHBaseService;
import com.enhantec.framework.common.utils.DSConstants;
import com.enhantec.wms.ui.common.model.CodeLookupModel;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
* @author johnw
* @description 针对表【CODELKUP】的数据库操作Service
* @createDate 2024-04-01 23:44:41
*/

@DS(DSConstants.DS_DEFAULT)
public interface CodeLkupService extends EHBaseService<CodeLookupModel> {

    List<Map<String,Object>> findActiveCodeByListName(@Param("listName") String listName);

}
