package com.enhantec.common.utils;

import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class EHLocaleHelper implements MessageSourceAware {

    private static MessageSource messageSource;

    @Override
    public void setMessageSource(MessageSource messageSource) {
        EHLocaleHelper.messageSource = messageSource;
    }

    public static String getMsg(String msgKey, String... args) {
            return messageSource.getMessage(msgKey, args, LocaleContextHolder.getLocale());
    }

    @Deprecated
    public static void changeLocale(String language) {
        String[] s = language.split("_");
        LocaleContextHolder.setLocale(new Locale(s[0], s[1]));
    }
}
