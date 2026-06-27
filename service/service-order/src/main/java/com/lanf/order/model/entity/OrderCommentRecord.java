package com.lanf.order.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import lombok.Data;

/**
 * <p>
 * 订单评论记录表
 * </p>
 *
 * @author jarven
 * @since 2026-06-27
 */
@Data
@TableName("order_comment_record")
public class OrderCommentRecord extends BaseEntity {
private static final long serialVersionUID=1L;

    private Long userId;

    private Long goodsId;

    private Long orderId;

    private Long tenantId;


}
