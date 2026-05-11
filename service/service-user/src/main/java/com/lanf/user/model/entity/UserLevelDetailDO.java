package com.lanf.user.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import lombok.Data;

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

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 事件名称
     */
    private String eventName;

    /**
     * 事件code
     */
    private String eventCode;

    /**
     * 业务ID，如订单号
     */
    private String bizId;

    /**
     * 变动前等级
     */
    private Integer beforeLevel;

    /**
     * 变动后等级
     */
    private Integer afterLevel;

    /**
     * 使用的权益列表
     */
    private String levelPrivileges;

    /**
     * 变动的成长值
     */
    private Integer growthValue;

    /**
     * 变动前的总成长值
     */
    private Integer afterTotal;

    /**
     * 变动后的总成长值
     */
    private Integer currentTotal;

}
