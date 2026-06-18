package com.lanf.api.order.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class ShippingTrackContentVO implements Serializable {

    /**
     * 当前完成时间
     */
    private Date finishTime;

    /**
     * 完成内容
     */
    private String finishContent;

}
