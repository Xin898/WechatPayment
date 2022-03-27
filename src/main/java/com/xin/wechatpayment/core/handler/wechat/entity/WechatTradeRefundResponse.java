package com.xin.wechatpayment.core.handler.wechat.entity;

import lombok.Data;

@Data
public class WechatTradeRefundResponse extends WechatBaseResponse{

    private String transactionId;

    private String outTradeNo;

    private String outRefundNo;

    private String refundId;

    private String refundFee;

    private String totalFee;

    private String cashFee;
}
