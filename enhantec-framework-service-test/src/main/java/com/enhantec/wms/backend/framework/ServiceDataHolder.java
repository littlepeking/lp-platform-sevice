package com.enhantec.wms.backend.framework;

public class ServiceDataHolder {

    public ServiceDataHolder(){
    }

    public ServiceDataHolder(Object inputData){
        this.inputData = inputData;
    }

    private Object inputData;

    private Object outputData;

    private int returnCode;

    public Object getInputData() {
        return inputData;
    }

    public ServiceDataMap getInputDataAsMap() {
        return (ServiceDataMap) inputData;
    }

    public void setInputData(Object inputData) {
        this.inputData = inputData;
    }

    public Object getOutputData(){
        return outputData;
    }

    public ServiceDataMap getOutputDataAsMap(){
            return (ServiceDataMap) outputData;
    }

    public void setOutputData(Object outputData) {
        this.outputData = outputData;
    }


    public void setReturnCode(int returnCode) {

        this.returnCode = returnCode;

    }

    public Object getReturnCode(){
        return returnCode;
    }

}
