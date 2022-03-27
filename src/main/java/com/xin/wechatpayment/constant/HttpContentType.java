package com.xin.wechatpayment.constant;

/*
 * created by Xin 27-03-2022
 */
public enum HttpContentType {
    TEXTHTML("text/html; charset=utf-8"),
    FORMURLENCODED("application/x-www-form-urlencoded; charset=utf-8"),
    APPLICATIONJSON("application/json; charset=utf-8"),
    ;

    private String type;

    HttpContentType(String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }
}
