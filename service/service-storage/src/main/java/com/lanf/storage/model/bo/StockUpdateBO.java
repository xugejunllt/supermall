package com.lanf.storage.model.bo;

import lombok.Data;

import java.io.Serializable;

@Data
public class StockUpdateBO implements Serializable {


    private Long  id;


    /**
     * 锁住库存
     */
    private Integer lockStock;

    private Long version;




}
