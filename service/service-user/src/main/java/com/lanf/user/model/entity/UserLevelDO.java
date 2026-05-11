package com.lanf.user.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.lanf.mybatis.base.BaseEntity;
import lombok.Data;

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

    /**
     * userId 作唯一索引 避免重复插入
     */

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 当前等级ID
     */
    private Long levelId;

    /**
     * 等级
     */
    private Integer level;

    /**
     * 当前总成长值
     */
    private Integer growthValue;

    /**
     * 乐观锁版本号
     */

    private Long version;




}
