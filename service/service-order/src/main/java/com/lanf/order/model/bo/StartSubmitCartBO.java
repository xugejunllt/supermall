package com.lanf.order.model.bo;

import com.lanf.api.goods.model.dto.ClearCartDTO;
import com.lanf.order.model.dto.BathCreateOrderDTO;
import lombok.Data;

import java.io.Serializable;

@Data
public class StartSubmitCartBO implements Serializable {

    private BathCreateOrderDTO bathCreateOrderDTO;
    private ClearCartDTO clearCartDTO;

}
