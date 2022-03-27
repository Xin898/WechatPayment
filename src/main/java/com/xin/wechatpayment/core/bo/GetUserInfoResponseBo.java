package com.xin.wechatpayment.core.bo;

import lombok.Data;

@Data
public class GetUserInfoResponseBo extends PayBaseResponseBo{

    private String payUserId;//第三方支付上用户的唯一标识
    private String userName;
    private String userSex;//M-男 F-女
    private String idCardNo;
    private String birth;
    private String mobile;
}
