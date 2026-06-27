package com.lanf.order.service.order.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.api.order.model.bo.AddressJson;
import com.lanf.api.order.model.bo.DiscountInfoJson;
import com.lanf.api.order.model.bo.ShippingInfoBO;
import com.lanf.api.order.model.dto.AllowOutboundDTO;
import com.lanf.api.order.model.dto.DeliveryDTO;
import com.lanf.api.order.model.enums.ShippingStatusEnum;
import com.lanf.api.order.model.query.AdminOrderSearchQuery;
import com.lanf.api.order.model.query.OrderDetailQuery;
import com.lanf.api.order.model.query.OrderDocumentQuery;
import com.lanf.api.order.model.vo.AdminOrderListVO;
import com.lanf.api.order.model.vo.OrderDetailForAdminVO;
import com.lanf.api.order.model.vo.OrderDocumentVO;
import com.lanf.api.order.model.vo.OrderItemVO;
import com.lanf.api.order.mq.message.*;
import com.lanf.api.pay.api.PayApiService;
import com.lanf.api.search.api.SearchApiService;
import com.lanf.api.search.model.query.OrderSearchQuery;
import com.lanf.api.search.model.vo.OrderSearchVO;
import com.lanf.api.storage.mq.message.SalesOutStockOrderFinishMessage;
import com.lanf.common.utils.BeanCopyUtils;
import com.lanf.common.utils.DateUtils;
import com.lanf.common.utils.IStringUtils;
import com.lanf.common.utils.JsonUtils;
import com.lanf.constant.exception.BizException;
import com.lanf.constant.model.enums.order.OrderStatusEnum;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.constant.mq.OrderTopicWithTag;
import com.lanf.constant.result.RpcResultParser;
import com.lanf.constant.utils.UserContext;
import com.lanf.logistics.api.LogisticsApiService;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.order.mapper.OrderMapper;
import com.lanf.order.model.bo.OrderIdAndUserId;
import com.lanf.order.model.dto.CreateOrderDTO;
import com.lanf.order.model.dto.OrderItemDTO;
import com.lanf.order.model.dto.SignForDTO;
import com.lanf.order.model.entity.*;
import com.lanf.order.model.enums.SubStatusEnum;
import com.lanf.order.model.query.AppOrderSearchQuery;
import com.lanf.order.model.query.OrderPageQuery;
import com.lanf.order.model.vo.OrderItemPageVO;
import com.lanf.order.model.vo.OrderListVO;
import com.lanf.order.model.vo.OrderPageVO;
import com.lanf.order.mq.constant.OrderMqTopicName;
import com.lanf.order.mq.message.BathAddShippingTrackMessage;
import com.lanf.order.mq.message.ShippingTrackMessage;
import com.lanf.order.service.order.IOrderItemService;
import com.lanf.order.service.order.IOrderService;
import com.lanf.order.service.order.IOrderStatusTraceService;
import com.lanf.order.service.order.OrderDetailCacheService;
import com.lanf.order.service.shipping.IExpressService;
import com.lanf.order.service.shipping.IShippingInfoService;
import com.lanf.order.service.shipping.IShippingTrackService;
import com.lanf.order.utils.OrderServiceUtils;
import com.lanf.rocketmq.exception.MessageRetryConsumeException;
import com.lanf.rocketmq.util.RocketMqClient;
import com.lanf.tcc.service.ITccOperationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * <p>
 * 订单表 服务实现类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-13
 */
@Slf4j
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, OrderDO> implements IOrderService {

    @Autowired
    private IOrderItemService orderItemService;

    @Autowired
    private RocketMqClient rocketMqClient;

    @Autowired
    private PayApiService payApiService;
    @Autowired
    private LogisticsApiService logisticsApiService;

    @Autowired
    private ITccOperationService tccOperationService;
    @Autowired
    private IOrderStatusTraceService orderStatusTraceService;
    @Autowired
    private SearchApiService searchApiService;
    @Qualifier("searchTaskExecutor")
    @Autowired
    private ThreadPoolTaskExecutor searchTaskExecutor;
    @Autowired
    private IExpressService expressService;
    @Autowired
    private IShippingInfoService shippingInfoService;

    @Autowired
    private IShippingTrackService shippingTrackService;

    @Autowired
    private OrderServiceUtils orderServiceUtils;

    @Autowired
    private OrderDetailCacheService orderDetailCacheService;

    @Transactional
    @Override
    public void createOrder(CreateOrderDTO dto) {

        OrderDO orderDO = orderServiceUtils.buildOrderDO(dto);
        //单笔下单时 只有一个商品
        OrderItemDTO orderItemDTO = dto.getOrderItems().get(0);
        OrderItemDO orderItemDO = BeanCopyUtils.copyBean(orderItemDTO, OrderItemDO.class);
        Date date = new Date();
        OrderStatusTraceDO orderStatusTraceDO = new OrderStatusTraceDO();
        orderStatusTraceDO.setOrderId(dto.getOrderId());
        orderStatusTraceDO.setFromStatus(null);
        orderStatusTraceDO.setToStatus(OrderStatusEnum.WAIT_PAY);
        orderStatusTraceDO.setCreateDate(DateUtils.format(date, DateUtils.DATE));
        orderStatusTraceDO.setUserId(dto.getUserId());
        orderStatusTraceDO.setTenantId(dto.getTenantId());
        orderStatusTraceDO.setRemark("用户下单");
        try {
            this.save(orderDO);
        } catch (DuplicateKeyException e) {
            log.info("订单已存在");
            return;
        }
        orderItemService.save(orderItemDO);
        orderStatusTraceService.save(orderStatusTraceDO);

    }


    @Override
    public List<Long> querySkuIdsByOrderId(Long orderId) {
        List<OrderItemDO> orderItemList = orderItemService.lambdaQuery()
                .eq(OrderItemDO::getOrderId, orderId)
                .select(OrderItemDO::getSkuId)
                .list();

        return orderItemList.stream()
                .map(OrderItemDO::getSkuId)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public void allowOutbound(AllowOutboundDTO dto) {

        Long orderId = dto.getOrderId();
        OrderDO orderDO = this.lambdaQuery()
                .eq(OrderDO::getId, orderId)
                .eq(OrderDO::getUserId, dto.getUserId())
                .one();

        if (orderDO == null) {
            log.error("订单不存在");
            throw new BizException("订单不存在");
        }
        if ( OrderStatusEnum.WAIT_OUTBOUND.equals(orderDO.getStatus())) {
            log.warn("订单已允许发货");
           return;
        }
        if ( !OrderStatusEnum.PAID.equals(orderDO.getStatus())) {
            log.warn("订单状态异常");
            throw new BizException("订单状态异常");
        }

        OrderWaitOutboundMessage outboundMessage = buildAddSalesOutStockOrderMessage(
                orderId, orderDO.getTenantId(),orderDO.getUserId());
        Date date = new Date();
        OrderStatusTraceDO orderStatusTraceDO = new OrderStatusTraceDO();
        orderStatusTraceDO.setOrderId(orderId);
        orderStatusTraceDO.setFromStatus(orderDO.getStatus());
        orderStatusTraceDO.setToStatus(OrderStatusEnum.WAIT_OUTBOUND);
        orderStatusTraceDO.setCreateDate(DateUtils.format(date, DateUtils.DATE));
        orderStatusTraceDO.setUserId(orderDO.getUserId());
        orderStatusTraceDO.setTenantId(orderDO.getTenantId());
        boolean update = this.lambdaUpdate()
                .eq(BaseEntity::getId, orderId)
                .eq(OrderDO::getVersion, orderDO.getVersion())
                .set(OrderDO::getStatus, OrderStatusEnum.WAIT_OUTBOUND)
                .set(OrderDO::getVersion, orderDO.getVersion() + 1)
                .update();
        if (!update) {
            log.warn("订单状态更新异常");
            throw new BizException("订单状态更新异常");
        }
        orderStatusTraceService.save(orderStatusTraceDO);
        rocketMqClient.sendOrderlyMessageWithTags(OrderTopicWithTag.ORDER_EVENT_TOPIC,
                OrderStatusEnum.WAIT_OUTBOUND.getTag(),JsonUtils.toJsonString(outboundMessage),
                orderDO.getId().toString());
        //发送物流跟踪信息
        BathAddShippingTrackMessage bathMessage = new BathAddShippingTrackMessage();
        bathMessage.setOrderId(orderDO.getId());
        bathMessage.setTenantId(orderDO.getTenantId());
        bathMessage.setUserId(orderDO.getUserId());
        List<ShippingTrackMessage> shippingTrackList = new ArrayList<>();
        ShippingTrackMessage trackMessage = new ShippingTrackMessage();
        trackMessage.setStatus(ShippingStatusEnum.WAREHOUSE_PROCESSING);
        trackMessage.setFinishTime(new Date());
        trackMessage.setFinishContent("订单正在仓库处理中");
        trackMessage.setFlowNo(IStringUtils.hashToUniqueString(orderDO.getId() + trackMessage.getFinishContent()));
        shippingTrackList.add(trackMessage);
        bathMessage.setShippingTrackList(shippingTrackList);
        rocketMqClient.sendMessage(OrderMqTopicName.BATH_ADD_SHIPPING_TRACK_TOPIC, JsonUtils.toJsonString(bathMessage));

    }

    private OrderWaitOutboundMessage buildAddSalesOutStockOrderMessage(Long orderId,Long tenantId,Long userId){
        List<OrderItemDO> orderItemDOList = orderItemService.lambdaQuery()
                .eq(OrderItemDO::getOrderId, orderId)
                .list();
        OrderWaitOutboundMessage addSalesOutStockOrderMessage = new OrderWaitOutboundMessage();
        List<InOutStockOrderItem> items = orderItemDOList.stream()
                .map(orderItemDO -> {
                    InOutStockOrderItem inOutStockOrderItem = new InOutStockOrderItem();
                    inOutStockOrderItem.setGoodsName(orderItemDO.getGoodsName());
                    inOutStockOrderItem.setSkuCode(orderItemDO.getSkuCode());
                    inOutStockOrderItem.setTotalQuantity(orderItemDO.getQuantity());
                    inOutStockOrderItem.setUnit(orderItemDO.getSkuName());
                    inOutStockOrderItem.setWarehouseId(orderItemDO.getWarehouseId());
                    inOutStockOrderItem.setTenantId(orderItemDO.getTenantId());
                    return inOutStockOrderItem;
                }).collect(Collectors.toList());

        addSalesOutStockOrderMessage.setOrderId(orderId);
        addSalesOutStockOrderMessage.setItems( items);
        addSalesOutStockOrderMessage.setTenantId(tenantId);
        addSalesOutStockOrderMessage.setUserId(userId);
        return addSalesOutStockOrderMessage;
    }

    @Transactional
    @Override
    public void delivery(DeliveryDTO dto) {

        Long orderId = dto.getOrderId();
        OrderDO orderDO = this.lambdaQuery()
                .eq(OrderDO::getUserId, dto.getUserId())
                .eq(OrderDO::getId, orderId)
                .one();
        if (orderDO == null) {
            log.error("订单不存在");
            throw new BizException("订单不存在");
        }
        if (OrderStatusEnum.SHIPPED.equals(orderDO.getStatus())){
            log.warn("订单已发货");
            throw new BizException("订单已发货");
        }
        if ( !OrderStatusEnum.OUTBOUNDED.equals(orderDO.getStatus())){
                log.warn("订单非待已出库状态");
                throw new BizException("订单非待已出库状态");
        }
        Long expressId = dto.getExpressId();
        ExpressDO expressDO = expressService.getById(expressId);
        if (expressDO == null) {
            log.error("物流公司不存在");
            throw new BizException("物流公司不存在");
        }
        ShippingInfoDO shippingInfoDO = new ShippingInfoDO();
        shippingInfoDO.setOrderId(orderId);
        shippingInfoDO.setUserId(orderDO.getUserId());
        shippingInfoDO.setLogisticsCompany(expressDO.getExpressCompany());
        shippingInfoDO.setLogisticsCode(expressDO.getCompanyCode());
        shippingInfoDO.setTrackingNumber(dto.getTrackingNumber());
        shippingInfoDO.setFromAddress(JsonUtils.toJsonString(dto.getFromAddressJson()));
        shippingInfoDO.setTenantId(orderDO.getTenantId());
        shippingInfoDO.setSubStatus(SubStatusEnum.PENDING);

        Date date = new Date();
        OrderStatusTraceDO orderStatusTraceDO = new OrderStatusTraceDO();
        orderStatusTraceDO.setOrderId(orderId);
        orderStatusTraceDO.setFromStatus(orderDO.getStatus());
        orderStatusTraceDO.setToStatus(OrderStatusEnum.SHIPPED);
        orderStatusTraceDO.setCreateDate(DateUtils.format(date, DateUtils.DATE));
        orderStatusTraceDO.setUserId(orderDO.getUserId());
        orderStatusTraceDO.setTenantId(orderDO.getTenantId());

        OrderShippedMessage orderShippedMessage = new OrderShippedMessage();
        orderShippedMessage.setOrderId(orderId);
        orderShippedMessage.setUserId(orderDO.getUserId());

        BathAddShippingTrackMessage message = new BathAddShippingTrackMessage();
        message.setOrderId(orderDO.getId());
        message.setTenantId(orderDO.getTenantId());
        message.setUserId(orderDO.getUserId());
        List<ShippingTrackMessage> shippingTrackList = new ArrayList<>();
        ShippingTrackMessage  shippingTrackMessage = new ShippingTrackMessage();
        shippingTrackMessage.setStatus(ShippingStatusEnum.WAREHOUSE_PROCESSING);
        shippingTrackMessage.setFinishTime(new Date());
        shippingTrackMessage.setFinishContent("你的订单由第三方卖家拣货完成,待出库交付快递");
        shippingTrackMessage.setFlowNo(IStringUtils.hashToUniqueString(orderDO.getId() +
                shippingTrackMessage.getFinishContent()));
        shippingTrackList.add(shippingTrackMessage);
        message.setShippingTrackList(shippingTrackList);


        boolean update = this.lambdaUpdate()
                .eq(BaseEntity::getId, orderId)
                .eq(OrderDO::getUserId, dto.getUserId())
                .eq(OrderDO::getVersion, orderDO.getVersion())
                .set(OrderDO::getStatus, OrderStatusEnum.SHIPPED)
                .set(OrderDO::getVersion, orderDO.getVersion() + 1)
                .update();
        if (!update) {
            log.warn("更新订单为已发货状态异常");
            throw new BizException("更新订单为已发货状态异常");
        }
        orderStatusTraceService.save(orderStatusTraceDO);
        shippingInfoService.save(shippingInfoDO);
        //发送订单已发货事件
        rocketMqClient.sendOrderlyMessageWithTags(OrderTopicWithTag.ORDER_EVENT_TOPIC,
                OrderStatusEnum.SHIPPED.getTag(),JsonUtils.toJsonString(orderShippedMessage),
                orderId.toString());
        //发送物流信息
        rocketMqClient.sendMessage(OrderMqTopicName.BATH_ADD_SHIPPING_TRACK_TOPIC, JsonUtils.toJsonString(message));


    }

    /**
     *
     * 订单列表搜索
     *
     */
    @Override
    public PageResult<OrderListVO> orderSearchQuery(AppOrderSearchQuery query) {

        OrderSearchQuery searchQuery = new OrderSearchQuery();
        searchQuery.setSearchWord(query.getSearchWord());
        searchQuery.setUserId(UserContext.getUserId());
        searchQuery.setPage(query.getPage());
        searchQuery.setPageSize(query.getPageSize());
        //1.从es获取订单id
        PageResult<OrderSearchVO> pageResult = RpcResultParser.
                parseResult(searchApiService.searchOrders(searchQuery));
        List<OrderSearchVO> records = pageResult.getRecords();
        if (IStringUtils.isEmpty(records)){

          return PageResult.emptyResult();

        }
        //2.根据订单id查询订单
        List<Long> orderIdList = records.stream().map(OrderSearchVO::getOrderId).collect(Collectors.toList());

        List<OrderDO> orderDOList = this.lambdaQuery()
                .eq(OrderDO::getUserId, UserContext.getUserId())
                .in(BaseEntity::getId, orderIdList)
                .list();
        //TODO: 2021/7/27 订单列表VO
        //完善返回结果

        return null;


    }

    @Override
    public PageResult<AdminOrderListVO> orderSearchQuery(AdminOrderSearchQuery query) {

        OrderSearchQuery searchQuery = new OrderSearchQuery();
        searchQuery.setSearchWord(query.getSearchWord());
        searchQuery.setPage(query.getPage());
        searchQuery.setPageSize(query.getPageSize());
        searchQuery.setOrderNumber(query.getOrderNumber());
        searchQuery.setTenantId(null);
        searchQuery.setOrderStatus(query.getOrderStatus());
        //1.从es获取订单id
        PageResult<OrderSearchVO> pageResult = RpcResultParser.
                parseResult(searchApiService.searchOrders(searchQuery));
        List<OrderSearchVO> records = pageResult.getRecords();
        if (IStringUtils.isEmpty(records)){

            return PageResult.emptyResult();

        }
        List<OrderIdAndUserId> orderIdAndUserIdList = new ArrayList<>();
        int sort = 0;
        for (OrderSearchVO record : records) {
            sort +=1;
            OrderIdAndUserId orderIdAndUserId = new OrderIdAndUserId();
            orderIdAndUserId.setOrderId(record.getOrderId());
            orderIdAndUserId.setUserId(record.getUserId());
            orderIdAndUserId.setSort( sort);
            orderIdAndUserIdList.add(orderIdAndUserId);
        }
        // 2. 为每一条记录创建一个并行查询任务
        List<CompletableFuture<AdminOrderListVO>> futures = orderIdAndUserIdList.stream().map(record -> {
            return CompletableFuture.supplyAsync(() -> {
                /**
                 * 查订单详细
                 * 查订单
                 */
                AdminOrderListVO adminOrderListVO = new AdminOrderListVO();
                //TODO: 2021/7/27 订单列表VO
                return adminOrderListVO;

            }, searchTaskExecutor); // 使用专用的搜索线程池
        }).collect(Collectors.toList());

        // 3. 等待所有任务完成，设置总超时时间为 2 秒
        try {
            // allOf 返回一个 CompletableFuture，当所有传入的 future 都完成时它才完成
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                    futures.toArray(new CompletableFuture[0])
            );

            // 阻塞等待，最多等 2 秒
            allFutures.get(2, TimeUnit.MILLISECONDS);

            // 3. 收集结果
            List<AdminOrderListVO> resultVos = futures.stream()
                    .map(CompletableFuture::join) // 此时任务已全部完成，join 不会阻塞
                    .collect(Collectors.toList());

            // 4. 封装分页结果返回
            PageResult<AdminOrderListVO> pageResult2 = new PageResult<>();
            pageResult2.setTotal(pageResult.getTotal());
            pageResult2.setRecords(resultVos);
            pageResult2.setSize(resultVos.size());

            return pageResult2;

        } catch (TimeoutException e) {

            log.warn("批量查询订单详情超时, 订单数量: {}", records.size(), e);
            // 取消所有未完成的任务，释放资源
            futures.forEach(f -> f.cancel(true));
            throw new BizException("查询超时，请稍后重试");

        } catch (Exception e) {
            log.warn("批量查询订单详情异常, 订单数量: {}", records.size(), e);
            throw new BizException("批量查询订单详情异常");
        }


    }


    @Override
    public void outStockFinish(SalesOutStockOrderFinishMessage message) {

        Long orderId = message.getOrderId();
        Long userId = message.getUserId();
        OrderDO orderDO = this.lambdaQuery().eq(OrderDO::getId, orderId)
                .eq(OrderDO::getUserId, userId)
                .one();
        OrderStatusEnum status = orderDO.getStatus();
        if (OrderStatusEnum.OUTBOUNDED.equals( status)){
            log.warn("订单已出库");
            return;
        }
        if ( !OrderStatusEnum.WAIT_OUTBOUND.equals( status)){
            log.error("订单状态异常");
            return;
        }
        boolean update = this.lambdaUpdate()
                .eq(OrderDO::getId, orderId)
                .eq(OrderDO::getUserId, userId)
                .eq(OrderDO::getVersion, orderDO.getVersion())
                .set(OrderDO::getStatus, OrderStatusEnum.OUTBOUNDED.getCode())
                .set(OrderDO::getVersion, orderDO.getVersion() + 1)
                .update();
        if (!update){
            log.warn("订单状态更新失败");
           throw new MessageRetryConsumeException("订单状态更新失败");
        }
        Date date = new Date();
        OrderStatusTraceDO orderStatusTraceDO = new OrderStatusTraceDO();
        orderStatusTraceDO.setOrderId(orderId);
        orderStatusTraceDO.setFromStatus(orderDO.getStatus());
        orderStatusTraceDO.setToStatus(OrderStatusEnum.OUTBOUNDED);
        orderStatusTraceDO.setCreateDate(DateUtils.format(date, DateUtils.DATE));
        orderStatusTraceDO.setUserId(userId);
        orderStatusTraceDO.setTenantId(orderDO.getTenantId());
        orderStatusTraceService.save(orderStatusTraceDO);
        /**
         * 发布订单出库成功事件
         */
        OrderOutBoundedMessage message2 = new OrderOutBoundedMessage();
        message2.setOrderId(orderId);
        rocketMqClient.sendOrderlyMessageWithTags(OrderTopicWithTag.ORDER_EVENT_TOPIC,
                OrderStatusEnum.OUTBOUNDED.getTag(), JsonUtils.toJsonString(message2),
                orderDO.getId().toString());
        //发送物流跟踪信息
        BathAddShippingTrackMessage bathMessage = new BathAddShippingTrackMessage();
        bathMessage.setOrderId(orderDO.getId());
        bathMessage.setTenantId(orderDO.getTenantId());
        bathMessage.setUserId(orderDO.getUserId());
        List<ShippingTrackMessage> shippingTrackList = new ArrayList<>();
        ShippingTrackMessage trackMessage = new ShippingTrackMessage();
        trackMessage.setStatus(ShippingStatusEnum.WAREHOUSE_PROCESSING);
        trackMessage.setFinishTime(new Date());
        trackMessage.setFinishContent("订单已出库");
        trackMessage.setFlowNo(IStringUtils.hashToUniqueString(orderDO.getId() + trackMessage.getFinishContent()));
        shippingTrackList.add(trackMessage);
        bathMessage.setShippingTrackList(shippingTrackList);
        rocketMqClient.sendMessage(OrderMqTopicName.BATH_ADD_SHIPPING_TRACK_TOPIC, JsonUtils.toJsonString(bathMessage));

    }

    @Transactional
    @Override
    public void signFor(SignForDTO dto) {

        Long orderId = dto.getOrderId();
        OrderDO orderDO = this.lambdaQuery().eq(OrderDO::getId, orderId)
                .eq(OrderDO::getUserId, UserContext.getUserId()).one();
        if (orderDO == null) {
            log.error("订单不存在");
            throw new BizException("订单不存在");
        }
        OrderStatusEnum status = orderDO.getStatus();
        if (OrderStatusEnum.RECEIVED.equals(status)) {
            log.warn("订单已签收");
            throw new BizException("订单已签收");
        }
        if ( !OrderStatusEnum.SHIPPED.equals(status)) {
            log.warn("订单非已发货状态");
            throw new BizException("订单非已发货状态");
        }

        Date date = new Date();
        OrderStatusTraceDO orderStatusTraceDO = new OrderStatusTraceDO();
        orderStatusTraceDO.setOrderId(orderId);
        orderStatusTraceDO.setFromStatus(orderDO.getStatus());
        orderStatusTraceDO.setToStatus(OrderStatusEnum.RECEIVED);
        orderStatusTraceDO.setCreateDate(DateUtils.format(date, DateUtils.DATE));
        orderStatusTraceDO.setUserId(UserContext.getUserId());
        orderStatusTraceDO.setTenantId(orderDO.getTenantId());
        orderStatusTraceDO.setRemark("用户签收订单");

        SignOrderMessage signOrderMessage = new SignOrderMessage();
        signOrderMessage.setOrderId(orderId);
        signOrderMessage.setSignTime( date);
        signOrderMessage.setAfterSaleDays(orderDO.getAfterSaleDays());
        signOrderMessage.setPayMoney(orderDO.getActualPayMoney());
        signOrderMessage.setTenantId(orderDO.getTenantId());


        /**
         * 订单更新信息
         */
        boolean update = this.lambdaUpdate().eq(OrderDO::getId, orderId)
                .eq(OrderDO::getUserId, UserContext.getUserId())
                .eq(BaseEntity::getId, orderDO.getId())
                .eq(OrderDO::getVersion, orderDO.getVersion())
                .set(OrderDO::getStatus, OrderStatusEnum.RECEIVED)
                .set(OrderDO::getVersion, orderDO.getVersion() + 1)
                .update();
        if (!update) {
            log.warn("订单状态更新异常");
            throw new BizException("订单状态更新异常");
        }
        orderStatusTraceService.save(orderStatusTraceDO);
        //发送订单签收事件
        rocketMqClient.sendOrderlyMessageWithTags(OrderTopicWithTag.ORDER_EVENT_TOPIC,
                OrderStatusEnum.RECEIVED.getTag(),JsonUtils.toJsonString(signOrderMessage),
                orderDO.getId().toString());
        //发送物流跟踪信息
        BathAddShippingTrackMessage bathMessage = new BathAddShippingTrackMessage();
        bathMessage.setOrderId(orderDO.getId());
        bathMessage.setTenantId(orderDO.getTenantId());
        bathMessage.setUserId(orderDO.getUserId());
        List<ShippingTrackMessage> shippingTrackList = new ArrayList<>();
        ShippingTrackMessage trackMessage = new ShippingTrackMessage();
        trackMessage.setStatus(ShippingStatusEnum.SIGNED);
        trackMessage.setFinishTime(new Date());
        trackMessage.setFinishContent("订单已签收");
        trackMessage.setFlowNo(IStringUtils.hashToUniqueString(orderDO.getId() + trackMessage.getFinishContent()));
        shippingTrackList.add(trackMessage);
        bathMessage.setShippingTrackList(shippingTrackList);
        rocketMqClient.sendMessage(OrderMqTopicName.BATH_ADD_SHIPPING_TRACK_TOPIC, JsonUtils.toJsonString(bathMessage));
    }

    @Override
    public OrderDocumentVO orderDocumentQuery(OrderDocumentQuery query) {

        OrderDO one = this.lambdaQuery()
                .eq(OrderDO::getId, query.getOrderId())
                .eq(OrderDO::getUserId, query.getUserId())
                .one();
        if (one == null){
            return null;
        }
        List<OrderItemDO> orderItemDOList = orderItemService.lambdaQuery()
                .eq(OrderItemDO::getOrderId, query.getOrderId())
                .eq(OrderItemDO::getUserId, query.getUserId()).list();
        List<String> goodsNames = orderItemDOList.stream().map(OrderItemDO::getGoodsName)
                .collect(Collectors.toList());

        OrderDocumentVO vo = new OrderDocumentVO();
        vo.setOrderId(one.getId());
        vo.setUserId(one.getUserId());
        vo.setOrderNumber(one.getOrderNumber());
        vo.setTenantId(one.getTenantId());
        vo.setOrderStatus(one.getStatus());
        vo.setCreateTime(one.getCreateTime());
        vo.setGoodsNames(goodsNames);


        return vo;
    }





    private void updateOrderCancel(Long orderId) {


        boolean result = this.lambdaUpdate().
                eq(BaseEntity::getId, orderId).
                set(OrderDO::getStatus, 6).
                update();
        if (!result) {
            throw new BizException("更新订单状态异常");
        }

    }

    @Override
    public void closeTimeOutNotPayOrder(Long orderId) {

        OrderDO orderDO = this.getById(orderId);
        if (orderDO == null) {

            throw new BizException("订单不存在");
        }
        OrderStatusEnum status = orderDO.getStatus();
        if (status.equals(OrderStatusEnum.PAID)) {
            //已付款，忽略
            log.info("订单已支付");
        } else {
            //订单待付款状态
            log.info("订单待付款，关闭订单");
            updateOrderCancel(orderId);

        }


    }

    @Override
    public PageResult<OrderPageVO> orderPageQuery(OrderPageQuery query) {


        IPage<OrderDO> page = new Page<>(query.getPage(), query.getPageSize());
        IPage<OrderDO> result =  this.lambdaQuery()
                .eq(OrderDO::getUserId, UserContext.getUserId())
                .in(!IStringUtils.isEmpty(query.getOrderIdList()),BaseEntity::getId, query.getOrderIdList())
                .in(query.getStatus()!=null && !query.getStatus().isEmpty(), OrderDO::getStatus, query.getStatus())
                .orderByDesc(BaseEntity::getId)
                .page(page);

        if (result.getRecords().isEmpty()){
            return PageResult.emptyResult();
        }
        
        List<OrderDO> orderList = result.getRecords();
        List<Long> orderIdList = orderList.stream().map(BaseEntity::getId).collect(Collectors.toList());
        
        List<OrderItemDO> orderItemDOList = orderItemService.lambdaQuery()
                .eq(OrderItemDO::getUserId, UserContext.getUserId())
                .in(OrderItemDO::getOrderId, orderIdList)
                .list();

        Map<Long, List<OrderItemDO>> orderItemMap = orderItemDOList.stream()
                .collect(Collectors.groupingBy(OrderItemDO::getOrderId));

        List<OrderPageVO> voList = orderList.stream().map(orderDO -> {
            OrderPageVO vo = BeanCopyUtils.copyBean(orderDO, OrderPageVO.class);
            
            List<OrderItemDO> items = orderItemMap.getOrDefault(orderDO.getId(), new ArrayList<>());
            List<OrderItemPageVO> itemVos = BeanCopyUtils.copyBeanList(items, OrderItemPageVO.class);
            vo.setOrderItemPageVOList(itemVos);
            
            return vo;
        }).collect(Collectors.toList());

        PageResult<OrderPageVO> resultVo = new PageResult<>();
        resultVo.setTotal(result.getTotal());
        resultVo.setSize(result.getSize());
        resultVo.setRecords(voList);

        return resultVo;
    }

    /**
     * Admin查询订单详情
     * 采用Cache-Aside模式，先查缓存未命中再回源数据库，并将结果回填缓存。
     * 设计亮点：
     * 1.缓存命中时直接返回，降低数据库查询压力
     * 2.缓存未命中时回源数据库，并将结果写入Redis，避免下次查询再次回源
     * 3.订单数据变更时由MQ监听器异步刷新缓存，保证最终一致性
     *
     * @param query 订单详情查询条件
     * @return 订单详情，订单不存在返回null
     */
    @Override
    public OrderDetailForAdminVO orderDetailForAdminQuery(OrderDetailQuery query) {

        query.setUserId(UserContext.getUserId());
        //1.先从缓存读取订单详情
        OrderDetailForAdminVO cached = orderDetailCacheService.getOrderDetailFromCache(query.getOrderId());
        if (cached != null) {
            log.info("订单详情缓存命中, orderId={}", query.getOrderId());
            return cached;
        }

        //2.缓存未命中，从数据库加载最新订单详情
        OrderDetailForAdminVO detail = loadOrderDetailFromDB(query);

        //3.将查询结果写入Redis缓存，供下次查询命中
        if (detail != null) {
            orderDetailCacheService.setOrderDetailToCache(query.getOrderId(), detail);
        }

        return detail;
    }






    @Override
    public OrderDetailForAdminVO loadOrderDetailFromDB(OrderDetailQuery query) {

        Long userId = query.getUserId();
        log.info("加载订单详情, userId={}", userId);
        OrderDO orderDO = this.lambdaQuery()
                .eq(OrderDO::getId, query.getOrderId())
                .eq(OrderDO::getUserId, userId)
                .one();
        if (orderDO == null) {
            return null;
        }

        ShippingInfoDO shippingInfoDO = shippingInfoService.lambdaQuery()
                .eq(ShippingInfoDO::getOrderId, query.getOrderId())
                .eq(ShippingInfoDO::getUserId, userId)
                .one();


        List<OrderItemDO> orderItemDOList = orderItemService.lambdaQuery()
                .eq(OrderItemDO::getOrderId, query.getOrderId())
                .eq(OrderItemDO::getUserId, userId)
                .list();

        OrderDetailForAdminVO detailForAdminVO = BeanCopyUtils.copyBean(orderDO, OrderDetailForAdminVO.class);
        if (orderDO.getPayType() != null){
            detailForAdminVO.setPayType(orderDO.getPayType().getCode());
        }
        String takeAddress = orderDO.getTakeAddress();
        if (takeAddress != null) {
            AddressJson takeAddressJson = JsonUtils.toObject(takeAddress, AddressJson.class);
            detailForAdminVO.setTakeAddressJson(takeAddressJson);
        }
        String discountInfo = orderDO.getDiscountInfo();
        if (discountInfo != null) {
            List<DiscountInfoJson> discountInfoJsonList = JsonUtils.toList(discountInfo, DiscountInfoJson.class);
            detailForAdminVO.setDiscountInfoBOS(discountInfoJsonList);
        }
        if (shippingInfoDO != null) {
            ShippingInfoBO shippingInfoBO = BeanCopyUtils.copyBean(shippingInfoDO, ShippingInfoBO.class);
            detailForAdminVO.setShippingInfoBO(shippingInfoBO);
        }
        List<OrderItemVO> orderItemVOS = BeanCopyUtils.copyBeanList(orderItemDOList, OrderItemVO.class);
        detailForAdminVO.setOrderItemVOList(orderItemVOS);

        return detailForAdminVO;
    }


}
