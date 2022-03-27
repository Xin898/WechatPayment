package com.xin.wechatpayment.core.bo;

import lombok.Data;

@Data
public class PayBaseRequestBo {
    private String requestPayAppId;
    private String requestMerchantId;
    private String requestPayUserId;
    private PayCodeEnum payCodeEnum;
    private PaymentSetting paymentSetting;
}
