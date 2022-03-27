package com.xin.wechatpayment.core.handler.wechat.entity;

import lombok.Data;

@Data
public class WechatMicroPayResponse extends WechatBaseResponse{

    private String openid;

    private String isSubscribe;

    private String tradeType;

    private String bankType;

    private String feeType;

    private String totalFee;

    private String settlementTotalFee;

    private String couponFee;

    private String cashFeeType;

    private String cashFee;

    private String transactionId;

    private String outTradeNo;

    private String attach;

    private String timeEnd;

    private String promotionDetail;
}
