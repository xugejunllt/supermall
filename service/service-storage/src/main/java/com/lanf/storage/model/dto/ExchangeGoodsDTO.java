package com.lanf.storage.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class ExchangeGoodsDTO implements Serializable {

    /**
     * 售后单id
     */
    private Long afterSalesId;

    private Integer inOutStatus;

}
