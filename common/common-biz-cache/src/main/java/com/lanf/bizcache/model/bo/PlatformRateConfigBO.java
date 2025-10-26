package com.lanf.bizcache.model.bo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class PlatformRateConfigBO implements Serializable {

    //@ApiModelProperty(value = "类型 0:下单支付")
    private Integer type;

    //@ApiModelProperty(value = "费率 百分比")
    private BigDecimal rate;

}
