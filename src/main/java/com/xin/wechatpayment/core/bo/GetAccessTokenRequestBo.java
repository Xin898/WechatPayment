package com.xin.wechatpayment.core.bo;

import lombok.Data;

/**
 * created by xin 2022-03-28
 */
@Data
public class GetAccessTokenRequestBo extends PayBaseRequestBo{
    private String authCode;
}
