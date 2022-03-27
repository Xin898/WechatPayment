package com.xin.wechatpayment.core.bo;

import lombok.Data;

/**
 * created by xin 2022-03-28
 */
@Data
public class BarcodePayResponseBo extends PayBaseResponseBo{
    private String paymentDealId;
    private String payUserId;
    private Integer payAmount;
    private String paymentDealNo;
    private String payTime;
}
