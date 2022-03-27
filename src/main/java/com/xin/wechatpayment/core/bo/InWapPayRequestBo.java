package com.xin.wechatpayment.core.bo;

import lombok.Data;

@Data
public class InWapPayRequestBo extends PayBaseRequestBo{

    private String description;
    private String subject;
    private Integer payAmount;
    private String paymentDealNo;
}
