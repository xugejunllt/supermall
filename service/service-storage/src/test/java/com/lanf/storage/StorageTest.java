package com.lanf.storage;

import com.lanf.api.order.mq.message.OrderCreateSuccessMessage;
import com.lanf.api.order.mq.message.OrderOutBoundedMessage;
import com.lanf.rocketmq.model.message.CancelOrderEventMessage;
import com.lanf.storage.mq.listener.event.OrderCancelEventListener;
import com.lanf.storage.mq.listener.event.OrderCreateSuccessEventListener;
import com.lanf.storage.mq.listener.event.OrderOutBoundedEventListener;
import com.lanf.storage.task.StockReconciliationScanOrderStatusTraceTask;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 订单服务单元测试
 */
@Slf4j
@SpringBootTest
public class StorageTest {

    @Autowired
    private OrderCreateSuccessEventListener  orderCreateSuccessEventListener;
    @Autowired
    private OrderCancelEventListener cancelEventListener;
    @Autowired
    private OrderOutBoundedEventListener orderOutBoundedEventListener;
    @Autowired

    private StockReconciliationScanOrderStatusTraceTask statusTraceTask;

    @Test
    public void orderCreateSuccessTest(){
        OrderCreateSuccessMessage message = new OrderCreateSuccessMessage();
        message.setOrderId(1506381049133928449L);
        message.setUserId(1503575413396475904L);
        orderCreateSuccessEventListener.onMessage(message);
    }
    @Test
    public void orderCancelTest(){
        CancelOrderEventMessage message = new CancelOrderEventMessage();
        message.setOrderId(1507099578812469248L);
        cancelEventListener.onMessage(message);
    }
    @Test
    public void orderOutBoundedTest(){
        OrderOutBoundedMessage message  = new OrderOutBoundedMessage();
        message.setOrderId(1520881967984414720L);
        orderOutBoundedEventListener.onMessage(message);
    }
    @Test
    public void  shortStockReconciliationScanTaskTest() throws InterruptedException {
        statusTraceTask.shortStockReconciliationScanTask();
        Thread.sleep(100000000L);
    }
    @Test
    public void  longStockReconciliationScanTask() throws InterruptedException {
        statusTraceTask.longStockReconciliationScanTask();
        Thread.sleep(100000000L);
    }
}
