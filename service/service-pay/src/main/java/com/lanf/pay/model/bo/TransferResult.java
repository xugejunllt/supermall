package com.lanf.pay.model.bo;

import lombok.Data;

import java.io.Serializable;

@Data
public class TransferResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private Boolean transferSuccess;


    private String errorMsg;





}
