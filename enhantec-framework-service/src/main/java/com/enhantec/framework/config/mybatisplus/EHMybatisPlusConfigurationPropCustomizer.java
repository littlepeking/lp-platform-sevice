
package com.enhantec.framework.config.mybatisplus;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusProperties;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusPropertiesCustomizer;
import com.enhantec.framework.common.utils.DBHelper;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.wrapper.MapWrapper;
import org.apache.ibatis.reflection.wrapper.ObjectWrapper;
import org.apache.ibatis.reflection.wrapper.ObjectWrapperFactory;
import org.springframework.stereotype.Component;

import java.util.Map;


@Component
class EHMybatisPlusConfigurationPropCustomizer implements MybatisPlusPropertiesCustomizer {

    @Override
    public void customize(MybatisPlusProperties properties) {
        // Customize the MybatisPlusProperties object
        // Modify the properties according to your requirements
        EHMybatisConfiguration configuration = new EHMybatisConfiguration();
        configuration.setObjectWrapperFactory(new EHMybatisPlusConfigurationPropCustomizer.MapWrapperFactory());
        configuration.setCallSettersOnNulls(true);
        properties.setConfiguration(configuration);

        // Customize globalConfig or other properties as needed
    }



    static class MapWrapperFactory implements ObjectWrapperFactory {
        @Override
        public boolean hasWrapperFor(Object object) {
            return object != null && object instanceof Map;
        }

        @Override
        public ObjectWrapper getWrapperFor(MetaObject metaObject, Object object) {
            return new EHMybatisPlusConfigurationPropCustomizer.MyMapWrapper(metaObject, (Map) object);
        }
    }

    static class MyMapWrapper extends MapWrapper {
        MyMapWrapper(MetaObject metaObject, Map<String, Object> map) {
            super(metaObject, map);
        }

        @Override
        public String findProperty(String name, boolean useCamelCaseMapping) {
            //useCamelCaseMapping need be configured in
            // application.xml => mybatis-plus.configuration.map-underscore-to-camel-case
            if (useCamelCaseMapping
                    //&& ((name.charAt(0) >= 'A' && name.charAt(0) <= 'Z'))
            ) {
                //For map result, Application always use default IFieldNameConverter fieldName for any request as right now we cannot pass IFieldNameConverter by request
                //Then we use default IFieldNameConverter to generate java Map data.
                //But when we convert fieldName to columnName we should consider if columnName use default IFieldNameConverter or not, e.g. for Infor WMS, we cannot use snake format for compatible purpose, then we can pass IFieldNameConverter based on individual request.
                return MybatisPlusConfig.getDefaultFieldNameConverter().convertColumnName2FieldName(name);
            }
            return name;
        }

    }

}
