/*******************************************************************************
 *                                     NOTICE
 *
 *             THIS SOFTWARE IS THE PROPERTY OF AND CONTAINS CONFIDENTIAL
 *             INFORMATION. AND SHALL NOT BE DISCLOSED WITHOUT PRIOR
 *             WRITTEN PERMISSION OF AUTHOR. LICENSED CUSTOMERS MAY COPY AND
 *             ADAPT THIS SOFTWARE FOR THEIR OWN USE IN ACCORDANCE WITH
 *             THE TERMS OF THEIR SOFTWARE LICENSE AGREEMENT.
 *             ALL OTHER RIGHTS RESERVED BY AUTHOR.
 *
 *             (c) COPYRIGHT 2022. ALL RIGHTS RESERVED.
 *
 *             Author: John Wang
 *             Email: john.wang_ca@hotmail.com
 * 
 *******************************************************************************/



package com.enhantec.wms.ui.common.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.enhantec.framework.common.model.PageParams;
import com.enhantec.framework.common.utils.EHPaginationHelper;
import com.enhantec.framework.common.utils.EHTranslationHelper;
import com.enhantec.framework.config.TransFieldConfig;
import com.enhantec.framework.config.annotations.TransField;
import com.enhantec.framework.config.annotations.converter.EHFieldNameConversionType;
import com.enhantec.wms.ui.common.model.CodeLookupModel;
import com.enhantec.wms.ui.common.service.CodeLkupService;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.enhantec.wms.Constants.WMS_UI_SERVICE;

@RestController
@RequestMapping(WMS_UI_SERVICE+"/common/code-lookup")
@RequiredArgsConstructor
public class CodeLookupController {

    private final CodeLkupService codeLkupService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/findActiveCodeByListName/{listName}")
    public List<Map<String,Object>> findActiveCodeByListName(@PathVariable String listName){
     //   return codeLkupService.findActiveCodeByListName(listName);

//        List<CodeLookupModel> res = codeLkupService.list(Wrappers.lambdaQuery(CodeLookupModel.class)
//                .select(
//                        CodeLookupModel::getSerialKey,
//                        CodeLookupModel::getCode,
//                        CodeLookupModel::getDescription,
//                        CodeLookupModel::getLongValue,
//                        CodeLookupModel::getShortValue)
//                        .eq(CodeLookupModel::getListName, listName)
//                        .eq(CodeLookupModel::getActive,1)

        List<Map<String,Object>> list = codeLkupService.findActiveCodeByListName(listName);

        return EHTranslationHelper.translate(list,
                new ArrayList<>(){{
                    add(new  TransFieldConfig(
                            "serialKey",
                            "description",
                            "CODELKUP",
                            "DESCRIPTION")
                    );
                    add(new  TransFieldConfig(
                            "serialKey",
                            "longValue",
                            "CODELKUP",
                            "LONG_VALUE")
                    );
                    add(new  TransFieldConfig(
                            "serialKey",
                            "shortValue",
                            "CODELKUP",
                            "SHORT")
                    );
                }

            }
        );

    }


}
