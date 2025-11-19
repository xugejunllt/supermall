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
 * 会员等级配置表
 * </p>
 *
 * @author jarven
 * @since 2025-11-20
 */
@Data
@TableName("user_level_config")
public class UserLevelConfigDO extends BaseEntity {

private static final long serialVersionUID=1L;



    @ApiModelProperty(value = "等级- 从1开始递增")
    private Integer level;

    @ApiModelProperty(value = "等级名称，如VIP1")
    private String name;

    @ApiModelProperty(value = "等级图标")
    private String icon;

    @ApiModelProperty(value = "该等级成长值")
    private Integer growthValue;

    @ApiModelProperty(value = "权益列表JSON配置")
    private String levelPrivileges;




}
