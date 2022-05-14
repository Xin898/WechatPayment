package com.xin.wechatpayment.framework.exception;

import com.xin.wechatpayment.constant.ErrorCodeEnum;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SimplePayException extends RuntimeException{
    private String resultCode;
    private Throwable e;

    public SimplePayException(String resultCode, String resultMessage) {
        super(resultMessage);
        this.resultCode = resultCode;
    }

    public SimplePayException(ErrorCodeEnum errorCodeEnum) {
        super(errorCodeEnum.getMessage());
        this.resultCode = errorCodeEnum.getCode();
    }

    public SimplePayException(int index, String resultMessage) {
        super(resultMessage);
        this.resultCode = String.valueOf(index);
    }

    public SimplePayException(String resultCode, String resultMessage, Throwable e) {
        super(resultMessage);
        this.resultCode = resultCode;
        this.e = e;
    }
}
