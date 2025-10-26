package com.lanf.system.model.entiry;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * <p>
 * 平台费率配置
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-20
 */
@Data
@TableName("platform_rate_config")
public class PlatformRateConfigDO extends BaseEntity {

private static final long serialVersionUID=1L;


    @ApiModelProperty(value = "类型 0:下单支付")
    private Integer type;

    @ApiModelProperty(value = "费率 百分比")
    private BigDecimal rate;




}
