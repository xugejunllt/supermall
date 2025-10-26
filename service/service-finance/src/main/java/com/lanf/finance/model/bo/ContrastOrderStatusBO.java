package com.lanf.finance.model.bo;

import lombok.Data;

import java.io.Serializable;

@Data
public class ContrastOrderStatusBO implements Serializable {

    private boolean contrastResult = false;

    private boolean orderNotPay = false;


}
