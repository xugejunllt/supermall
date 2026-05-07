package com.lanf.seckill.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.seckill.model.enums.SeckillActivityStatusEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * <p>
 * 秒杀活动表
 * </p>
 *
 * @author jarven
 * @since 2026-05-07
 */
@Data
@TableName("seckill_activity")
public class SeckillActivityDO extends BaseEntity {

private static final long serialVersionUID=1L;



    @ApiModelProperty(value = "活动名称")
    private String name;

    @ApiModelProperty(value = "活动开始时间")
    private Date startTime;

    @ApiModelProperty(value = "活动结束时间")
    private Date endTime;

    private SeckillActivityStatusEnum status;

    private Long merchantId;




}
