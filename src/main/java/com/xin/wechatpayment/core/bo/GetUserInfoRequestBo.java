package com.xin.wechatpayment.core.bo;

import lombok.Data;

@Data
public class GetUserInfoRequestBo extends PayBaseRequestBo{
    private String accessToken;
}
