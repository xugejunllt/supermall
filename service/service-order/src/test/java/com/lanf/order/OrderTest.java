package com.lanf.order;

import com.lanf.api.order.model.enums.ShippingStatusEnum;
import com.lanf.api.order.model.query.OrderDetailQuery;
import com.lanf.api.order.model.vo.OrderDetailForAdminVO;
import com.lanf.api.order.model.vo.ShippingTrackContentVO;
import com.lanf.api.order.model.vo.ShippingTrackVO;
import com.lanf.common.utils.JsonUtils;
import com.lanf.constant.model.enums.order.OrderStatusEnum;
import com.lanf.constant.utils.UserContext;
import com.lanf.order.model.entity.OrderDO;
import com.lanf.order.model.entity.ShippingTrackDO;
import com.lanf.order.service.order.IOrderService;
import com.lanf.order.service.shipping.IShippingTrackService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 订单服务单元测试
 */
@Slf4j
@SpringBootTest
public class OrderTest {

    @Autowired
    private IShippingTrackService shippingTrackService;

    @Autowired
    private IOrderService orderService;

    private static final Long TEST_USER_ID = 1509742215767920640L;
    private static final Long TEST_ORDER_ID = 888888L;
    private static final Long TEST_TENANT_ID = 1L;

    @BeforeEach
    public void setUp() {
        UserContext.setUserId(TEST_USER_ID);
        UserContext.setTenantId(TEST_TENANT_ID);
    }

    @AfterEach
    public void tearDown() {
        UserContext.clear();
    }

    /**
     * 测试 orderDetailForAdminQuery 方法
     * 验证并行查询订单详情和物流轨迹
     */
    @Test
    public void testOrderDetailForAdminQuery() {
        // 1. 插入订单数据
        OrderDO orderDO = new OrderDO();
        orderDO.setId(TEST_ORDER_ID);
        orderDO.setUserId(TEST_USER_ID);
        orderDO.setShopId(1L);
        orderDO.setShopName("测试店铺");
        orderDO.setOrderNumber("TEST_ORDER_001");
        orderDO.setTotalMoney(new BigDecimal("100.00"));
        orderDO.setActualPayMoney(new BigDecimal("90.00"));
        orderDO.setDiscountAmount(new BigDecimal("10.00"));
        orderDO.setStatus(OrderStatusEnum.PAID);
        orderDO.setAfterSaleDays(7);
        orderDO.setTenantId(TEST_TENANT_ID);
        orderDO.setVersion(0L);
        orderService.save(orderDO);

        // 2. 插入物流轨迹数据
        Date now = new Date();
        ShippingTrackDO track = new ShippingTrackDO();
        track.setId(100L);
        track.setOrderId(TEST_ORDER_ID);
        track.setUserId(TEST_USER_ID);
        track.setStatus(ShippingStatusEnum.ORDER_PLACED);
        track.setFinishTime(now);
        track.setFinishContent("订单已提交");
        track.setTenantId(TEST_TENANT_ID);
        track.setFlowNo("FLOW_100");
        track.setCreateTime(now);
        track.setUpdateTime(now);
        track.setIsDeleted(0);
        shippingTrackService.save(track);

        // 3. 调用查询方法
        OrderDetailQuery query = new OrderDetailQuery();
        query.setOrderId(TEST_ORDER_ID);
        OrderDetailForAdminVO result = orderService.orderDetailForAdminQuery(query);

        // 4. 验证结果
        assertNotNull(result, "订单详情不应为空");
        assertEquals(TEST_ORDER_ID, result.getUserId(), "用户ID应匹配");
        assertEquals("测试店铺", result.getShopName(), "店铺名称应匹配");
        assertEquals("TEST_ORDER_001", result.getOrderNumber(), "订单编号应匹配");
        assertEquals(new BigDecimal("100.00"), result.getTotalMoney(), "订单金额应匹配");
        assertEquals(new BigDecimal("90.00"), result.getActualPayMoney(), "实付金额应匹配");

        // 5. 验证物流轨迹
        assertNotNull(result.getTrackVOList(), "物流轨迹列表不应为空");
        assertEquals(1, result.getTrackVOList().size(), "应有1个物流状态分组");
        assertEquals(ShippingStatusEnum.ORDER_PLACED, result.getTrackVOList().get(0).getStatus(), "状态应为ORDER_PLACED");
        assertEquals(1, result.getTrackVOList().get(0).getTrackContentVOList().size(), "应有1条物流记录");
        assertEquals("订单已提交", result.getTrackVOList().get(0).getTrackContentVOList().get(0).getFinishContent(), "内容应匹配");

        log.info("orderDetailForAdminQuery 测试结果: {}", JsonUtils.toJsonString(result));

        // 6. 清理数据
        orderService.removeById(TEST_ORDER_ID);
        shippingTrackService.lambdaUpdate().eq(ShippingTrackDO::getOrderId, TEST_ORDER_ID).remove();
    }

    /**
     * 测试 findShippingTrack 方法
     * 验证：
     * 1. 按 status 分组
     * 2. 相同 status 的 content 按 finishTime 降序
     * 3. 最终列表按 ShippingStatusEnum code 降序
     */
    @Test
    public void testFindShippingTrack() {
        Long orderId = 999999999L;
        Long userId = 100L;
        Long tenantId = 1L;



        // 准备测试数据
        Date now = new Date();
        Date earlier = new Date(now.getTime() - 3600 * 1000); // 1小时前
        Date earliest = new Date(now.getTime() - 7200 * 1000); // 2小时前

        // ORDER_PLACED (code=0) - 2条记录
        ShippingTrackDO track1 = createShippingTrackDO(1L, orderId, userId, ShippingStatusEnum.ORDER_PLACED,
                earlier, "订单已提交-较早", tenantId);
        ShippingTrackDO track2 = createShippingTrackDO(2L, orderId, userId, ShippingStatusEnum.ORDER_PLACED,
                now, "订单已提交-最新", tenantId);

        // WAREHOUSE_PROCESSING (code=1) - 2条记录
        ShippingTrackDO track3 = createShippingTrackDO(3L, orderId, userId, ShippingStatusEnum.WAREHOUSE_PROCESSING,
                earliest, "仓库处理中-较早", tenantId);
        ShippingTrackDO track4 = createShippingTrackDO(4L, orderId, userId, ShippingStatusEnum.WAREHOUSE_PROCESSING,
                earlier, "仓库处理中-较新", tenantId);

        // SIGNED (code=5) - 1条记录
        ShippingTrackDO track5 = createShippingTrackDO(5L, orderId, userId, ShippingStatusEnum.SIGNED,
                now, "订单已签收", tenantId);

        // 插入数据
        shippingTrackService.save(track1);
        shippingTrackService.save(track2);
        shippingTrackService.save(track3);
        shippingTrackService.save(track4);
        shippingTrackService.save(track5);

        // 执行查询
        List<ShippingTrackVO> result = shippingTrackService.findShippingTrack(orderId);

        // 验证结果
        assertNotNull(result);
        assertEquals(3, result.size(), "应该有3个不同的status分组");

        // 验证按 code 降序：SIGNED(code=5) > WAREHOUSE_PROCESSING(code=1) > ORDER_PLACED(code=0)
        assertEquals(ShippingStatusEnum.SIGNED, result.get(0).getStatus());
        assertEquals(ShippingStatusEnum.WAREHOUSE_PROCESSING, result.get(1).getStatus());
        assertEquals(ShippingStatusEnum.ORDER_PLACED, result.get(2).getStatus());

        // 验证 SIGNED 分组内有1条记录
        assertEquals(1, result.get(0).getTrackContentVOList().size());
        assertEquals("订单已签收", result.get(0).getTrackContentVOList().get(0).getFinishContent());

        // 验证 WAREHOUSE_PROCESSING 分组内按 finishTime 降序
        List<ShippingTrackContentVO> warehouseContent = result.get(1).getTrackContentVOList();
        assertEquals(2, warehouseContent.size());
        assertEquals("仓库处理中-较新", warehouseContent.get(0).getFinishContent());
        assertEquals("仓库处理中-较早", warehouseContent.get(1).getFinishContent());

        // 验证 ORDER_PLACED 分组内按 finishTime 降序
        List<ShippingTrackContentVO> placedContent = result.get(2).getTrackContentVOList();
        assertEquals(2, placedContent.size());
        assertEquals("订单已提交-最新", placedContent.get(0).getFinishContent());
        assertEquals("订单已提交-较早", placedContent.get(1).getFinishContent());

        log.info("findShippingTrack 测试结果: {}", result);


        // 清理测试数据
        shippingTrackService.lambdaUpdate()
                .eq(ShippingTrackDO::getOrderId, orderId)
                .remove();
    }

    private ShippingTrackDO createShippingTrackDO(Long id, Long orderId, Long userId,
                                                     ShippingStatusEnum status, Date finishTime,
                                                     String finishContent, Long tenantId) {
        ShippingTrackDO track = new ShippingTrackDO();
        track.setId(id);
        track.setOrderId(orderId);
        track.setUserId(userId);
        track.setStatus(status);
        track.setFinishTime(finishTime);
        track.setFinishContent(finishContent);
        track.setTenantId(tenantId);
        track.setFlowNo("FLOW_" + id);
        track.setCreateTime(new Date());
        track.setUpdateTime(new Date());
        track.setIsDeleted(0);
        return track;
    }


}
