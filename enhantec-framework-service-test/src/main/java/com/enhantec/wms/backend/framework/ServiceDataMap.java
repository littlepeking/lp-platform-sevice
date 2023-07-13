package com.enhantec.wms.backend.framework;

import java.util.HashMap;

public class ServiceDataMap {


    public ServiceDataMap(){
    }

    public ServiceDataMap(HashMap data){
        this.data = data;
    }

    private HashMap data;


    public HashMap getData() {
        return data;
    }

    public void setData(HashMap data) {
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
