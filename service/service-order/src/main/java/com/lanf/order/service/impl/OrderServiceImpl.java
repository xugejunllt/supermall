package com.lanf.order.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.api.order.model.query.OrderDocumentQuery;
import com.lanf.api.order.model.vo.OrderDocumentVO;
import com.lanf.api.order.mq.constant.OrderClientTopicName;
import com.lanf.api.order.mq.message.AddSalesOutStockOrderMessage;
import com.lanf.api.order.mq.message.InOutStockOrderItem;
import com.lanf.api.order.mq.message.OrderOutBoundedMessage;
import com.lanf.api.order.mq.message.SignOrderMessage;
import com.lanf.api.pay.api.PayApiService;
import com.lanf.api.search.api.SearchApiService;
import com.lanf.api.search.model.query.OrderSearchQuery;
import com.lanf.api.search.model.vo.OrderSearchVO;
import com.lanf.common.utils.BeanCopyUtils;
import com.lanf.common.utils.DateUtils;
import com.lanf.common.utils.IStringUtils;
import com.lanf.common.utils.JsonUtils;
import com.lanf.constant.exception.BizException;
import com.lanf.constant.model.enums.order.OrderStatusEnum;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.constant.result.RpcResultParser;
import com.lanf.constant.utils.UserContext;
import com.lanf.logistics.api.LogisticsApiService;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.order.mapper.OrderMapper;
import com.lanf.order.model.bo.OrderIdAndUserId;
import com.lanf.order.model.dto.*;
import com.lanf.order.model.entity.OrderDO;
import com.lanf.order.model.entity.OrderItemDO;
import com.lanf.order.model.entity.OrderStatusTraceDO;
import com.lanf.order.model.query.AdminOrderSearchQuery;
import com.lanf.order.model.query.AppOrderSearchQuery;
import com.lanf.order.model.query.OrderPageQuery;
import com.lanf.order.model.vo.AdminOrderListVO;
import com.lanf.order.model.vo.OrderItemPageVO;
import com.lanf.order.model.vo.OrderListVO;
import com.lanf.order.model.vo.OrderPageVO;
import com.lanf.order.service.IOrderItemService;
import com.lanf.order.service.IOrderService;
import com.lanf.order.service.IOrderStatusTraceService;
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

    @Transactional
    @Override
    public void createOrder(CreateOrderDTO dto) {
        log.info("创建订单开始:{}", dto);

        OrderDO orderDO = OrderServiceUtils.buildOrderDO(dto);
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
            log.info("插入的订单信息{}",orderDO);
            //order_number 为唯一索引 作为兜底 避免重复下单
            this.save(orderDO);
        } catch (DuplicateKeyException e) {
            log.info("订单已存在");
            return;
        }
        log.info("插入的订单商品信息{}",orderItemDO);
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
        OrderDO orderDO = this.getById(orderId);

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
        AddSalesOutStockOrderMessage addSalesOutStockOrderMessage = buildAddSalesOutStockOrderMessage(orderId);

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
        orderStatusTraceService.addOrderStatusTrace(orderDO.getId(), orderDO.getStatus(),
                OrderStatusEnum.WAIT_OUTBOUND);
        rocketMqClient.sendMessage(OrderClientTopicName.SIGN_ORDER_EVENT_TOPIC, JsonUtils.
                toJsonString(addSalesOutStockOrderMessage));

    }

    private AddSalesOutStockOrderMessage buildAddSalesOutStockOrderMessage(Long orderId){
        List<OrderItemDO> orderItemDOList = orderItemService.lambdaQuery()
                .eq(OrderItemDO::getOrderId, orderId)
                .list();
        AddSalesOutStockOrderMessage addSalesOutStockOrderMessage = new AddSalesOutStockOrderMessage();
        List<InOutStockOrderItem> items = orderItemDOList.stream()
                .map(orderItemDO -> {
                    InOutStockOrderItem inOutStockOrderItem = new InOutStockOrderItem();
                    inOutStockOrderItem.setGoodsName(orderItemDO.getGoodsName());
                    inOutStockOrderItem.setSkuCode(orderItemDO.getSkuCode());
                    inOutStockOrderItem.setTotalQuantity(orderItemDO.getQuantity());
                    inOutStockOrderItem.setUnit(orderItemDO.getSkuName());
                    inOutStockOrderItem.setWarehouseId(orderItemDO.getWarehouseId());

                    return inOutStockOrderItem;
                }).collect(Collectors.toList());
        addSalesOutStockOrderMessage.setOrderId(orderId);
        addSalesOutStockOrderMessage.setItems( items);
        return addSalesOutStockOrderMessage;
    }

    @Override
    public void delivery(DeliveryDTO dto) {

        Long orderId = dto.getOrderId();
        OrderDO orderDO = this.getById(orderId);

        if (orderDO == null) {
            throw new BizException("订单不存在");
        }
//        Integer status = orderDO.getStatus();
//        if (status != 2) {
//            throw new BizException("订单状态异常");
//        }
        OrderDO orderDOUpdate = new OrderDO();
        orderDOUpdate.setId(orderId);
        orderDOUpdate.setStatus(null);
        this.updateById(orderDOUpdate);

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
    public void outStockFinish(Long orderId) {

        OrderDO orderDO = this.getById(orderId);
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
                .eq(OrderDO::getVersion, orderDO.getVersion())
                .set(OrderDO::getStatus, OrderStatusEnum.OUTBOUNDED.getCode())
                .set(OrderDO::getVersion, orderDO.getVersion() + 1)
                .update();
        if (!update){
            log.warn("订单状态更新失败");
           throw new MessageRetryConsumeException("订单状态更新失败");
        }
        orderStatusTraceService.addOrderStatusTrace(orderId,
                OrderStatusEnum.WAIT_OUTBOUND, OrderStatusEnum.OUTBOUNDED);
        /**
         * 发布订单出库成功事件
         */
        OrderOutBoundedMessage message = new OrderOutBoundedMessage();
        message.setOrderId(orderId);
        rocketMqClient.sendMessage( OrderClientTopicName.ORDER_OUT_BOUNDED_EVENT_TOPIC,
                JsonUtils.toJsonString(message));


    }

    @Override
    public void signFor(SignForDTO dto) {

        Long orderId = dto.getOrderId();
        OrderDO orderDO = this.getById(orderId);
        OrderStatusEnum status = orderDO.getStatus();
        if (!OrderStatusEnum.SHIPPED.equals(status)) {
            log.warn("订单状态异常");
            throw new BizException("订单状态异常");
        }
        /**
         * 订单更新信息
         */
        boolean update = this.lambdaUpdate().eq(OrderDO::getId, orderId)
                .eq(OrderDO::getStatus, OrderStatusEnum.SHIPPED.getCode())
                .eq(OrderDO::getVersion, orderDO.getVersion())
                .set(OrderDO::getStatus, OrderStatusEnum.WAIT_COMMENT.getCode())
                .set(OrderDO::getVersion, orderDO.getVersion() + 1)
                .update();
        if (!update) {
            log.error("订单状态更新异常");
            throw new BizException("订单状态更新异常");
        }
        /**
         * 发送订单签收消息
         */
        SignOrderMessage signOrderMessage = new SignOrderMessage();

        signOrderMessage.setOrderId(orderId);
        signOrderMessage.setSignTime(new Date());
        signOrderMessage.setAfterSaleDays(orderDO.getAfterSaleDays());
        signOrderMessage.setPayMoney(orderDO.getActualPayMoney());
        signOrderMessage.setMerchantId(orderDO.getTenantId());

        rocketMqClient.sendMessage(OrderClientTopicName.SIGN_ORDER_EVENT_TOPIC,
                JsonUtils.toJsonString(signOrderMessage));

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


}
