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



package com.enhantec.framework.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.enhantec.framework.security.common.model.EHUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
public class AutoFillFieldsConfig implements MetaObjectHandler {


    private EHUser getUser() {

        if(SecurityContextHolder.getContext()!=null && SecurityContextHolder.getContext().getAuthentication()!=null) {

            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            if (principal instanceof EHUser) {
                return (EHUser) principal;
            } else {
                //might be anonymousUser
                //String username = principal.toString();
                return null;
            }
        }else {
            return null;
        }

    }

    public void insertFill(MetaObject metaObject) {
        LocalDateTime date = LocalDateTime.now();
        this.strictInsertFill(metaObject, "addDate", () -> date, LocalDateTime.class);
        this.strictInsertFill(metaObject, "addWho", () -> getUser() ==null ? "sys" : getUser().getUsername(), String.class);
        this.strictInsertFill(metaObject, "editDate", () -> date, LocalDateTime.class);
        this.strictInsertFill(metaObject, "editWho", () -> getUser() ==null ? "sys" : getUser().getUsername(), String.class);
        //Init version column to 1.
        this.setFieldValByName("version",1,metaObject);

    }

    @Override
    public void updateFill(MetaObject metaObject) {

        log.info("start update fill ....");
        //Have to check if getUser() == null as it might call from changePassword, at that time, there is no user in context.
        if(getUser()!=null) {
            //this.strictUpdateFill(metaObject, "editDate", () -> LocalDateTime.now(), LocalDateTime.class);
            //this.strictUpdateFill(metaObject, "editWho", () -> getUser().getId(), String.class);
            //From MyBatisPlus: MetaObjectHandler提供的默认方法的策略均为:如果属性有值则不覆盖,如果填充值为null则不填充
            // So we have to call update method directly here.
            this.setFieldValByName("editDate", LocalDateTime.now(), metaObject);
            this.setFieldValByName("editWho", getUser() ==null ? "sys" : getUser().getUsername(), metaObject);
        }

    }
}