package com.lanf.finance.model.bo;

import lombok.Data;

import java.io.Serializable;

@Data
public class ContrastBillOrderStatusBO implements Serializable {

    //0:支付成功,1:取消退款,2:取消
    private Integer contrastBillOrderStatus;

}
