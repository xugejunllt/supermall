package com.lanf.order.model.bo;

import lombok.Data;

import java.io.Serializable;

@Data
public class ExpressPushLastResultDataBO implements Serializable {
    //完成时间
    private String time;
    //完成内容
    private String context;

}
