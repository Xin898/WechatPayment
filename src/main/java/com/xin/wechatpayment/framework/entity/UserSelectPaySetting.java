package com.xin.wechatpayment.framework.entity;

import lombok.Getter;
import lombok.Setter;

/*
 * Created by Xin
 */
@Getter
@Setter
public class UserSelectPaySetting {
    private String payCode;
    private String frontSubmitForm;
    private String appId;
    private String timeStamp;
    private String nonceStr;
    private String prepayId;
    private String signType;
    private String paySign;
}
