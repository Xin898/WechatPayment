package com.xin.wechatpayment.core.bo;

import com.xin.wechatpayment.constant.ErrorCodeEnum;
import lombok.Data;

@Data
public class PayBaseResponseBo {

    private String resultCode;
    private String resultMessage;

    public PayBaseResponseBo(){
        this.resultCode= String.valueOf(ErrorCodeEnum.DEFUALT_ERROR.getIndex());
        this.resultMessage= ErrorCodeEnum.DEFUALT_ERROR.getMessage();
    }

    public void buildResultCodeAndResultMessage(String resultCode,String resultMessage){
        this.resultCode=resultCode;
        this.resultMessage=resultMessage;
    }

    public boolean isSuccess(){
        if(ErrorCodeEnum.SUCCESS.getCode().equals(resultCode)){
            return true;
        }
        return false;
    }
}
