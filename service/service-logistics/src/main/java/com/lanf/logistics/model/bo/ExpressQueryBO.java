package com.lanf.logistics.model.bo;

import lombok.Data;

import java.io.Serializable;

@Data
public class ExpressQueryBO implements Serializable {

    //快递单号
    private String expressNumber;
}
