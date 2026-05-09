package com.lanf.search.model.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.annotation.Version;

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
    /**
     * Elasticsearch 每个文档都有内部版本号组合：
     * _seq_no（自增序列号）和 _primary_term（主分片任期）。
     * 当使用 @Version 注解时，Spring Data Elasticsearch
     * 在更新请求中自动附加条件：if_seq_no = 当前version值
     * 和 if_primary_term = 当前version的高位值（具体封装由框架处理）。
     *
     * 如果更新时文档已被其他线程修改，ES 会返回版本冲突错误，
     * 框架将其转换为 OptimisticLockingFailureException。
     */
    @Version
    private Long version;

}
