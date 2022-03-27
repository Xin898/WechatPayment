package com.xin.wechatpayment.core.bo;

import com.xin.wechatpayment.constant.PayCodeEnum;
import com.xin.wechatpayment.db.entity.PaymentSetting;
import lombok.Data;

@Data
public class PayBaseRequestBo {
    private String requestPayAppId;
    private String requestMerchantId;
    private String requestPayUserId;
    private PayCodeEnum payCodeEnum;
    private PaymentSetting paymentSetting;
}
