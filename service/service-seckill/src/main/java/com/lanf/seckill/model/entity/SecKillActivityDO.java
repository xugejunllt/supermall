package com.lanf.seckill.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 秒杀活动表
 * </p>
 *
 * @author jarven
 * @since 2026-05-07
 */
@Data
@TableName("sec_kill_activity")
public class SecKillActivityDO extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "活动名称")
    private String name;


}
