package com.xin.wechatpayment.core.bo;

import lombok.Data;

@Data
public class TradeQueryRequestBo extends PayBaseRequestBo{
    private String paymentDealId;
    private String paymentDealNo;
}
