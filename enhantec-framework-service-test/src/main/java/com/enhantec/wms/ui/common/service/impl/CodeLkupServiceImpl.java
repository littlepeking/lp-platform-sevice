package com.enhantec.wms.ui.common.service.impl;

import com.enhantec.framework.common.service.impl.EHBaseServiceImpl;
import com.enhantec.wms.ui.common.model.CodeLookupModel;
import com.enhantec.wms.ui.common.service.CodeLkupService;
import com.enhantec.wms.ui.common.mapper.CodeLookupMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
* @author johnw
* @description 针对表【CODELKUP】的数据库操作Service实现
* @createDate 2024-04-01 23:44:41
*/
@Service
public class CodeLkupServiceImpl extends EHBaseServiceImpl<CodeLookupMapper, CodeLookupModel>
    implements CodeLkupService{

    public List<Map<String,String>> findActiveCodeByListName(@Param("listName") String listName){
        return getBaseMapper().findActiveCodeByListName(listName);
    }

}



