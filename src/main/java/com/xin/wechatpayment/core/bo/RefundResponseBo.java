package com.xin.wechatpayment.core.bo;

import lombok.Data;

@Data
public class RefundResponseBo extends PayBaseResponseBo{
    private Integer refundAmount;
    private String refundPaymentDealNo; //支付系统退款订单号
    private String refundPaymentDealId; //第三方支付退款流水号
    private String refundTime;
}
