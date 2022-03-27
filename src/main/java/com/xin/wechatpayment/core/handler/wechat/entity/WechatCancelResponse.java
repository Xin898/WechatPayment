package com.xin.wechatpayment.core.handler.wechat.entity;

import lombok.Data;

@Data
public class WechatCancelResponse extends WechatBaseResponse{
    private String recall;
}
