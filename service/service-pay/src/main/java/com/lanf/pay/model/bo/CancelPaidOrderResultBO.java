package com.lanf.pay.model.bo;

import lombok.Data;

import java.io.Serializable;

@Data
public class CancelPaidOrderResultBO implements Serializable {


    private Boolean result ;
    /**
     * 错误信息
     */
    private String errorMsg ;

}
