package com.xin.wechatpayment.core.bo;

import lombok.Data;

@Data
public class GetAccessTokenResponseBo extends PayBaseResponseBo{
    private String accessToken;
    private String payUerId;
}
