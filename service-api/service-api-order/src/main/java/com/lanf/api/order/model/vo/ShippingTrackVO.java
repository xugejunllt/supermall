package com.lanf.api.order.model.vo;

import com.lanf.api.order.model.enums.ShippingStatusEnum;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ShippingTrackVO implements Serializable {

    /**
     * 物流状态
     */
    private ShippingStatusEnum status;


    private List<ShippingTrackContentVO> trackContentVOList;


}
