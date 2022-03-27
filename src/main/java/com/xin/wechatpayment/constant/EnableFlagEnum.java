package com.xin.wechatpayment.constant;

/**
 * created by Xin 2022-03-37
 */
public enum EnableFlagEnum {
    DISABLE(0,"禁用"),
    ENABLE(1,"启用"),
    ;

    //成员变量
    private String message;

    private int index;

    //构造方法
    private EnableFlagEnum(int index, String name) {
        this.message = name;
        this.index = index;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
