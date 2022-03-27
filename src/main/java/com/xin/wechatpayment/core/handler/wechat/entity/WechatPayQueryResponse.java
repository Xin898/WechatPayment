package com.xin.wechatpayment.core.handler.wechat.entity;

import lombok.Data;

@Data
public class WechatPayQueryResponse extends WechatBaseResponse{

    private String openid;

    private String isSubscribe;

    private String tradeType;

    private String tradeState;

    private String bankType;

    private String totalFee;

    private String feeType;

    private String cashFee;

    private String cashFeeType;

    private String couponFee;

    private String couponCount;

    private String transactionId;

    private String outTradeNo;

    private String attach;

    private String timeEnd;

    private String tradeStateDesc;
}
