package com.lanf.rocketmq.model.bo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;


@Data
public class ExpressPushLastResultBO implements Serializable {

    //快递物流状态
    private String state;
    //快递单号
    private String nu;
    //快递物流轨迹
    private List<ExpressPushLastResultDataBO> data;

}
