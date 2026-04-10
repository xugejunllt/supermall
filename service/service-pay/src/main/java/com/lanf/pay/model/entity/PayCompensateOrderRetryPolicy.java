package com.lanf.pay.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 *
 * </p>
 *
 * @author jarven
 * @since 2026-04-07
 */
@Data
@TableName("pay_compensate_order_retry_policy")
public class PayCompensateOrderRetryPolicy extends BaseEntity {

private static final long serialVersionUID=1L;



    @ApiModelProperty(value = "重试次数（第几次重试）")
    private Integer retryLevel;

    @ApiModelProperty(value = "延迟秒数（从上次失败开始）")
    private Integer delaySeconds;

    @ApiModelProperty(value = "累计耗时（秒）")
    private Integer accumulatedSeconds;

    @ApiModelProperty(value = "描述，如“5秒后重试”")
    private String description;

    @ApiModelProperty(value = "是否启用0启用,1:禁用")
    private Integer isEnabled;




}
