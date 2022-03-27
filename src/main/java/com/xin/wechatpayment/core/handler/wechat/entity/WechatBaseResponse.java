package com.xin.wechatpayment.core.handler.wechat.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class WechatBaseResponse implements Serializable {

    private String returnCode;

    private String returnMsg;

    private String appid;

    private String mchId;

    private String deviceInfo;

    private String nonceStr;

    private String sign;

    private String resultCode;

    private String errCode;

    private String errCodeDes;

    private static final String SUCCESS = "SUCCESS";

    private static final String FAIL="FAIL";

    public boolean isReturnCodeSuccess() {
        return SUCCESS.equalsIgnoreCase(returnCode);
    }

    public boolean isResultCodeSuccess() {
        return SUCCESS.equalsIgnoreCase(resultCode);
    }

    public boolean isResultCodeFail(){return FAIL.equalsIgnoreCase(resultCode); }

}
