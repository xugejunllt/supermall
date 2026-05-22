package com.lanf.order.model.bo;

import com.lanf.order.model.enums.Express100StatusEnum;
import com.lanf.order.model.enums.ShippingStatusEnum;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class AddShippingTrackBO implements Serializable {


    private String flowNo;
    /**
     * 物流状态
     */
    private ShippingStatusEnum status;
    /**
     * 三方物流基础轨迹状态
     */
    private Express100StatusEnum baseTrackStatus;

    /**
     * 当前完成时间
     */
    private Date finishTime;

    /**
     * 完成内容
     */
    private String finishContent;



}
