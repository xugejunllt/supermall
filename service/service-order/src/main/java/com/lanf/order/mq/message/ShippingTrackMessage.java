package com.lanf.order.mq.message;

import com.lanf.order.model.enums.Express100StatusEnum;
import com.lanf.order.model.enums.ShippingStatusEnum;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public  class ShippingTrackMessage implements Serializable {

        /**
         * 物流状态 (对应 ShippingStatusEnum code)
         */
        private ShippingStatusEnum status;

        /**
         * 三方物流基础轨迹状态 (对应 Express100StatusEnum code)
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