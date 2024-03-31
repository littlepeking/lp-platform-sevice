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

package com.enhantec.framework.config.mybatisplus;

import com.enhantec.framework.common.utils.EHContextHelper;
import com.enhantec.framework.config.EHAppConfig;
import com.enhantec.framework.config.annotations.converter.EHFieldNameConversionType;
import com.enhantec.framework.config.annotations.converter.IFieldNameConverter;
import com.enhantec.framework.config.annotations.converter.NoConverter;
import com.enhantec.framework.config.annotations.converter.CamelCase2UnderScoreConverter;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;


@MapperScan("com.enhantec.**.mapper")
@Configuration
public class MybatisPlusConfig {


    public static boolean isMapCamelCase2Underscore(){

      return EHContextHelper.getBean(EHAppConfig.MybatisPlus.ConfigurationProps.class).isMapUnderscoreToCamelCase();

    }

    static CamelCase2UnderScoreConverter camelCase2UnderScoreConverter = new CamelCase2UnderScoreConverter();
    static NoConverter noConverter = new NoConverter();

    public static IFieldNameConverter getDefaultFieldNameConverter(){
       return isMapCamelCase2Underscore() ? camelCase2UnderScoreConverter: noConverter;
    }

    public static IFieldNameConverter getFieldNameConverterByType(EHFieldNameConversionType type){

        IFieldNameConverter fieldNameConverter = null;

        switch (type){
            case CAMELCASE2UNDERSCORE:
                fieldNameConverter = camelCase2UnderScoreConverter;
                        break;
            case NONE:
                fieldNameConverter = noConverter;
                break;
            default:
                getDefaultFieldNameConverter();
        }

        return fieldNameConverter;
    }

}
