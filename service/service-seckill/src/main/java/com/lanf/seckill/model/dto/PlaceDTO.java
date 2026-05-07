package com.lanf.seckill.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class PlaceDTO implements Serializable {


    private Long seckillItemId;

    private Long activityId;

    private String token;

    private Long userId;

}
