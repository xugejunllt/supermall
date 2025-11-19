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
 * 成长值明细表
 * </p>
 *
 * @author jarven
 * @since 2025-11-20
 */
@Data
@TableName("user_level_detail")
public class UserLevelDetailDO extends BaseEntity {

private static final long serialVersionUID=1L;

    private Long userId;

    @ApiModelProperty(value = "事件名称")
    private String eventName;

    @ApiModelProperty(value = "事件code")
    private String eventCode;

    @ApiModelProperty(value = "业务ID，如订单号")
    private String bizId;

    private Integer beforeLevel;

    private Integer afterLevel;

    //使用的权益列表
    private String levelPrivileges;

    @ApiModelProperty(value = "变动的成长值")
    private Integer growthValue;

    @ApiModelProperty(value = "变动前的总成长值")
    private Integer afterTotal;

    @ApiModelProperty(value = "变动后的总成长值")
    private Integer currentTotal;

}
