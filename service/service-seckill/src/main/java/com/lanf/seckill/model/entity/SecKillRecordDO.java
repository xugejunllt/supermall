package com.lanf.seckill.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 秒杀记录表
 * </p>
 *
 * @author jarven
 * @since 2026-05-09
 */
@Data
@TableName("sec_kill_record")
public class SecKillRecordDO extends BaseEntity {

private static final long serialVersionUID=1L;


    @ApiModelProperty(value = "用户ID")
    private Long userId;

    @ApiModelProperty(value = "秒杀商品ID")
    private Long secKillItemId;

    @ApiModelProperty(value = "秒杀的库存数量")
    private Integer stockQuantity;
    private Long tenantId;





}
