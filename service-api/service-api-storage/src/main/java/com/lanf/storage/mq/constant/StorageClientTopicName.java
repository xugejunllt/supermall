package com.lanf.storage.mq.constant;

public class StorageClientTopicName {

    /**
     * 发布预售库存
     */
    public static final String PUBLISH_STOCK_TOPIC = "PUBLISH_STOCK_TOPIC";

    /**
     * 回收预售库存
     */
    public static final String RECYCLE_STOCK_TOPIC = "RECYCLE_STOCK_TOPIC";

    /**
     * 构建ReconciliationOrderDetail 事件
     */

    public static final String BUILD_RECONCILIATION_ORDER_DETAIL_TOPIC = "BUILD_RECONCILIATION_ORDER_DETAIL_TOPIC";
    /**
     * ReconciliationOrderDetail 保存成功监听
     */
    public static final String RECONCILIATION_ORDER_SAVE_SUCCESS_NOTIFY_TOPIC = "RECONCILIATION_ORDER_SAVE_SUCCESS_NOTIFY_TOPIC";

    /**
     *更新 ReconciliationOrderDetailItem
     */
    public static final String UPDATE_RECONCILIATION_ORDER_DETAIL_ITEM_TOPIC = "UPDATE_RECONCILIATION_ORDER_DETAIL_ITEM_TOPIC";
    /**
     * 更新订单项标签
     */
    public  static final String UPDATE_ORDER_ITEM_TAG = "UPDATE_ORDER_ITEM_TAG";
    /**
     * 更新库存流水标签
     */
    public  static final String UPDATE_STOCK_FLOW_TAG = "UPDATE_STOCK_FLOW_TAG";



}
