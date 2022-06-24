package com.enhantec.common.controller;

import com.enhantec.common.utils.EHLocaleHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/common/locale")
@RequiredArgsConstructor
public class LocaleController {

    @PostMapping("/changeLocale/{locale}")
    public void changeLocale(@PathVariable String locale) {
        EHLocaleHelper.changeLocale(locale);
    }

}
