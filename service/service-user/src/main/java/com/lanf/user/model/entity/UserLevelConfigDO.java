package com.lanf.user.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import lombok.Data;

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



    /**
     * 等级 - 从1开始递增
     */
    private Integer level;

    /**
     * 等级名称，如VIP1
     */
    private String name;

    /**
     * 等级图标
     */
    private String icon;

    /**
     * 该等级成长值
     */
    private Integer growthValue;

    /**
     * 权益列表JSON配置
     */
    private String levelPrivileges;




}
