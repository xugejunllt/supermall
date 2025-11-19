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
 * 用户会员等级主表
 * </p>
 *
 * @author jarven
 * @since 2025-11-20
 */
@Data
@TableName("user_level")
public class UserLevelDO extends BaseEntity {

private static final long serialVersionUID=1L;

    @ApiModelProperty(value = "用户ID")
    private Long userId;

    @ApiModelProperty(value = "当前等级ID")
    private Long levelId;

    //等级
    private Integer level;

    @ApiModelProperty(value = "当前总成长值")
    private Integer growthValue;

    @ApiModelProperty(value = "乐观锁版本号")
    private Long version;




}
