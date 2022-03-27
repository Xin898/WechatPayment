package com.xin.wechatpayment.core.bo;

import lombok.Data;

@Data
public class RefundRequestBo extends PayBaseRequestBo{
    private Integer refundAmount;
    private String refundPaymentDealNo;
    private String paymentDealNo;
    private String paymentDealId;
}
