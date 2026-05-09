package com.lanf.search.model.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

@Data
@Document(indexName = "order_index", createIndex = false)
public class OrderDocument {

    // 字段名称常量定义
    public static final String ORDER_ID = "orderId";
    public static final String USER_ID = "userId";
    public static final String ORDER_NO = "orderNo";
    public static final String TENANT_ID = "tenantId";
    public static final String ORDER_STATUS = "orderStatus";
    public static final String CREATE_TIME = "createTime";
    public static final String GOODS_NAME = "goodsName";

    @Id
    // 订单id
    private Long orderId;

    // 用户id (通常作为分片键或关联查询键)
    private Long userId;

    // 订单编号 (业务唯一标识，通常使用 keyword 类型精确匹配)
    @Field(type = FieldType.Keyword)
    private String orderNumber;

    // 租户id
    private Long tenantId;

    private Integer orderStatus;

    /**
     * 订单创建时间（毫秒级时间戳）
     * 使用 Long 类型存储，便于范围查询和排序
     */
    @Field(type = FieldType.Long)
    private Long createTime;
    /**
     * 一笔订单 对应多个商品
     */
    private List<String> goodsName;
}
