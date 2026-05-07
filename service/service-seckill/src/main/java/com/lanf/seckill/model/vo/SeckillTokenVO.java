package com.lanf.seckill.model.vo;


import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class SeckillTokenVO implements Serializable {

    @ApiModelProperty(value = "秒杀令牌")
    private String token;

    @ApiModelProperty(value = "动态下单链接")
    private String orderUrl;
}
