package com.lanf.user.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 权益表

 * </p>
 *
 * @author jarven
 * @since 2025-11-19
 */
@Data
@TableName("benefit")
public class BenefitDO extends BaseEntity {

private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "权益code")
    private String code;

    @ApiModelProperty(value = "权益名称")
    private String name;

    @ApiModelProperty(value = "0待使用  1.使用中 2.废弃")
    //状态流程规则 0->1 1>0 0->2 1>2
    private Integer status;
    //权益描述

    private String benefitDesc;



}
