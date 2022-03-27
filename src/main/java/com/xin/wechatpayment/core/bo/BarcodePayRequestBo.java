package com.xin.wechatpayment.core.bo;

import lombok.Data;

/**
 * created by xin 2022-03-28
 */
@Data
public class BarcodePayRequestBo extends PayBaseRequestBo {
    private String authCode;
    private String description;
    private String subject;
    private Integer payAmount;
    private String paymentDealNo;
}
