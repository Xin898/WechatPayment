package com.xin.wechatpayment.core.bo;

import lombok.Data;

@Data
public class CancelRequestBo extends PayBaseRequestBo{
    private String paymentDealId;
    private String paymentDealNo;
}
