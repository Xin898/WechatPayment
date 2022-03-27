package com.xin.wechatpayment.core.handler.wechat.entity;

import lombok.Data;

import java.io.Serializable;

/**
 *  Wechat 授权信息
 */
@Data
public class WechatAuthResponse implements Serializable {

    private String accessToken;

    private String expiresIn;

    private String refreshToken;

    private String openid;

    private String scope;

    private String errcode;

    private String errmsg;
}
