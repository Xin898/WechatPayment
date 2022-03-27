package com.xin.wechatpayment.core.bo;

import com.xin.wechatpayment.core.bo.PayBaseResponseBo;
import lombok.Data;


@Data
public class TradeQueryResponseBo extends PayBaseResponseBo {
    private String paymentDealId;
    private String payUserId;
    private Integer payAmount;
    private String paymentDealNo;
    private String payTime;
    /**
     * SUCCESS—支付成功
     * REFUND—转入退款
     * NOTPAY 未支付
     * USERPAYING-支付中
     * PAYERROR-支付失败
     * CLOSED-已经关闭
     * CANCEL-撤销
     */
    private String tradeState; //
}
