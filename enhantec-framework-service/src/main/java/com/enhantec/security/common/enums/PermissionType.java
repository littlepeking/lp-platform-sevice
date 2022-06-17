package com.enhantec.security.common.enums;

public enum PermissionType {

    Directory("D"), Permission("P");

    private String type;

    private PermissionType(String type) {
        this.type = type;
    }

    @Override
    public String toString(){
        return type;
    }
}