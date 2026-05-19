package com.lanf.order.model.bo;

import com.lanf.api.user.model.vo.AddressListVO;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

@Data
public class BuildBathCreateOrderBO implements Serializable {


    private Long mainOrderId;
    private Long userId;

    private AddressListVO addressListVO;
    /**
     * key:购物车id value:仓库id
     */
    private Map<Long,Long> warehouseIdMap;



}
