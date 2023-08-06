package com.enhantec.wms.backend.framework;

import java.util.HashMap;
import java.util.Map;

public class ServiceDataMap {


    public ServiceDataMap(){
    }

    public ServiceDataMap(Map data){
        this.data = data;
    }

    private Map data;


    public Map getData() {
        return data;
    }

    public void setData(Map data) {
        this.data = data;
    }


    public String getString(String paramName){
        return data.get(paramName) != null ? data.get(paramName).toString() : null;
    }

    public void setAttribValue(String paramName, Object val){
        if(data==null){
            data = new HashMap();
        }
        data.put(paramName,val);
    }
}
