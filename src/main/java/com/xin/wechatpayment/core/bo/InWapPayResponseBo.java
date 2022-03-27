package com.xin.wechatpayment.core.bo;

import lombok.Data;

@Data
public class InWapPayResponseBo extends PayBaseResponseBo{
    private String frontSubmitForm;
    private String appId;
    private String timeStamp;
    private String nonceStr;
    private String prepayId;
    private String signType;
    private String paySign;
}
