package com.xin.wechatpayment.core.handler.wechat.entity;

import lombok.Data;

@Data
public class WechatJsapiPayResponse extends WechatBaseResponse{

    private String tradeType;

    private String prepayId;
}
