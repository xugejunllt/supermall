package com.lanf.storage.model.bo;

import lombok.Data;

import java.io.Serializable;

@Data
public class StockUpdateBO implements Serializable {


    private Long  id;



    private Long version;

    /**
     * 可使用库存
     */
    private Integer usableStock;


}
