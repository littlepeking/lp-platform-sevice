
package com.enhantec.framework.config.mybatisplus;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusProperties;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusPropertiesCustomizer;
import com.enhantec.framework.config.annotations.converter.CamelCase2UnderScoreConverter;
import lombok.AllArgsConstructor;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.wrapper.MapWrapper;
import org.apache.ibatis.reflection.wrapper.ObjectWrapper;
import org.apache.ibatis.reflection.wrapper.ObjectWrapperFactory;
import org.springframework.stereotype.Component;

import java.util.Map;


@Component
@AllArgsConstructor
class EHMybatisPlusConfigurationPropCustomizer implements MybatisPlusPropertiesCustomizer {

    EHMybatisConfiguration ehMybatisConfiguration;

    @Override
    public void customize(MybatisPlusProperties properties) {
        // Customize the MybatisPlusProperties object
        // Modify the properties according to your requirements
        EHMybatisConfiguration configuration = ehMybatisConfiguration;
        configuration.setObjectWrapperFactory(new EHMapWrapperFactory());
        configuration.setCallSettersOnNulls(true);
        properties.setConfiguration(configuration);

        // Customize globalConfig or other properties as needed
    }



    static class EHMapWrapperFactory implements ObjectWrapperFactory {
        @Override
        public boolean hasWrapperFor(Object object) {
            return object != null && object instanceof Map;
        }

        @Override
        public ObjectWrapper getWrapperFor(MetaObject metaObject, Object object) {
            return new EHMapWrapper(metaObject, (Map) object);
        }
    }

    static class EHMapWrapper extends MapWrapper {
        EHMapWrapper(MetaObject metaObject, Map<String, Object> map) {
            super(metaObject, map);
        }

        static CamelCase2UnderScoreConverter fieldNameConverter = new CamelCase2UnderScoreConverter();

        @Override
        public String findProperty(String name, boolean useCamelCaseMapping) {
            //useCamelCaseMapping need be configured in
            // application.xml => mybatis-plus.configuration.map-underscore-to-camel-case
            //if (useCamelCaseMapping)
            //{
                //For map result, Application always use Snake2CamelCaseFieldNameConverter to generate fieldName for any request as right now we cannot pass IFieldNameConverter by request
                //But when we convert fieldName to columnName we should consider if columnName should use default IFieldNameConverter or not, e.g. for Infor WMS, we cannot use snake column name for compatible purpose, so in that case we can pass IFieldNameConverter based on individual request.
                //return MybatisPlusConfig.getDefaultFieldNameConverter().convertColumnName2FieldName(name);

               return fieldNameConverter.convertColumnName2FieldName(name);
            //}
            //return name;
        }

    }

}
