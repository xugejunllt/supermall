package com.lanf.system.model.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class ShopVO implements Serializable {

    private Long id;

    private String name;

    private String headUrl;

    private Long businessId;
}
