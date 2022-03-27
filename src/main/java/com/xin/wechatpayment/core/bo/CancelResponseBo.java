package com.xin.wechatpayment.core.bo;

import lombok.Data;

@Data
public class CancelResponseBo extends PayBaseResponseBo{
    private String retryFlag;//Y需要重试  N 不需要重试
    private String paymentDealId;
    private String paymentDealNo;
}
