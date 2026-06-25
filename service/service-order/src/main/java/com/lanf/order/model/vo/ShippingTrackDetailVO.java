package com.lanf.order.model.vo;

import com.lanf.api.order.model.bo.ShippingInfoBO;
import com.lanf.api.order.model.vo.ShippingTrackVO;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ShippingTrackDetailVO implements Serializable {

    private ShippingInfoBO shippingInfoBO;

    private List<ShippingTrackVO> trackVOList;


}
