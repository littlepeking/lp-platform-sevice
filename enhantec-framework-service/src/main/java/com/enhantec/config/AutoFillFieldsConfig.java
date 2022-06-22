package com.enhantec.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.enhantec.security.common.model.EHUser;
import com.enhantec.security.common.service.EHUserDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;

@Slf4j
@Component
public class AutoFillFieldsConfig implements MetaObjectHandler {


    private EHUser getUser() {
        return (EHUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

//        String username;
//
//        if (principal instanceof EHUser) {
//            return (EHUser) principal;
//        } else {
//            username = principal.toString();
//            return EHUser.builder().username(username).build();
//        }

    }

    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime date = LocalDateTime.now();
        this.strictInsertFill(metaObject, "addDate", () -> date, LocalDateTime.class);
        this.strictInsertFill(metaObject, "addWho", () -> getUser().getId(), String.class);
        this.strictInsertFill(metaObject, "editDate", () -> date, LocalDateTime.class);
        this.strictInsertFill(metaObject, "editWho", () -> getUser().getId(), String.class);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("start update fill ....");
//        this.strictUpdateFill(metaObject, "editDate", () -> LocalDateTime.now(), LocalDateTime.class);
//        this.strictUpdateFill(metaObject, "editWho", () -> getUser().getId(), String.class);
        //From MyBatisPlus: MetaObjectHandler提供的默认方法的策略均为:如果属性有值则不覆盖,如果填充值为null则不填充
        // So we have to call update method directly here.
        this.setFieldValByName("editDate",LocalDateTime.now(),metaObject);
        this.setFieldValByName("editWho",getUser().getId(),metaObject);

    }
}