package com.enhantec.wms.ui.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enhantec.framework.common.exception.EHApplicationException;
import com.enhantec.framework.common.service.impl.EHBaseServiceImpl;
import com.enhantec.wms.ui.common.model.CodeListModel;
import com.enhantec.wms.ui.common.service.CodeListService;
import com.enhantec.wms.ui.common.mapper.CodeListMapper;
import net.bytebuddy.implementation.bytecode.Throw;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
* @author johnw
* @description 针对表【CODELIST】的数据库操作Service实现
* @createDate 2024-04-01 23:19:17
*/
@Service
public class CodeListServiceImpl extends EHBaseServiceImpl<CodeListMapper, CodeListModel>
    implements CodeListService{

    public Page<Map<String,Object>> queryPageData(Page<Map<String,Object>> page, QueryWrapper qw){

        throw  new EHApplicationException("not implement yet");

    }


}




