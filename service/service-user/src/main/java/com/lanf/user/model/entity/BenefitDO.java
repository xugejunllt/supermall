package com.lanf.user.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import lombok.Data;

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

    /**
     * 权益code
     */
    private String code;

    /**
     * 权益名称
     */
    private String name;

    /**
     * 状态：0待使用  1.使用中 2.废弃
     * 状态流程规则 0->1 1>0 0->2 1>2
     */
    private Integer status;
    
    /**
     * 权益描述
     */
    private String benefitDesc;



}
