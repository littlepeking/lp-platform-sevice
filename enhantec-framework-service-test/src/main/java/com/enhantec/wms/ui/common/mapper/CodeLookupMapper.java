package com.enhantec.wms.ui.common.mapper;
import org.apache.ibatis.annotations.Param;

import com.enhantec.framework.common.mapper.EHBaseMapper;
import com.enhantec.wms.ui.common.model.CodeLookupModel;

import java.util.*;

/**
* @author johnw
* @description 针对表【CODELKUP】的数据库操作Mapper
* @createDate 2024-04-01 23:44:41
* @Entity com.enhantec.wms.ui.common.CodeLkup
*/
public interface CodeLookupMapper extends EHBaseMapper<CodeLookupModel> {

    //MapKey is required is not necessary here
    List<Map<String,Object>> findActiveCodeByListName(@Param("listName") String listName);


}




