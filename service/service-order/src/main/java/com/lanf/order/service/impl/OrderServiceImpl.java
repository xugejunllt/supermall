package com.lanf.order.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.client.pay.api.PayApiService;
import com.lanf.client.pay.model.query.TradeOrderBathQuery;
import com.lanf.client.pay.model.vo.OrderTradeVO;
import com.lanf.client.pay.model.vo.TradeOrderBathVO;
import com.lanf.common.utils.BeanCopyUtils;
import com.lanf.common.utils.BigDecimalUtil;
import com.lanf.common.utils.JsonUtils;
import com.lanf.constant.enums.FrozenStatusEnum;
import com.lanf.constant.exception.BizException;
import com.lanf.logistics.api.LogisticsApiService;
import com.lanf.logistics.model.vo.LogisticsTrackStatusVO;
import com.lanf.logistics.model.vo.LogisticsTrackVO;
import com.lanf.logistics.model.vo.LogisticsVO;
import com.lanf.messagemanager.client.service.ISendMqMessageService;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.mybatis.base.PageResult;
import com.lanf.order.mapper.OrderMapper;
import com.lanf.order.model.bo.CancelOrderBO;
import com.lanf.order.model.bo.CancelOrderOrderStatusBO;
import com.lanf.order.model.dto.*;
import com.lanf.order.model.entity.OrderDO;
import com.lanf.order.model.entity.OrderItemDO;
import com.lanf.order.model.enums.OrderStatusEnum;
import com.lanf.order.model.query.ContrastBillOrderQuery;
import com.lanf.order.model.query.OrderPageQuery;
import com.lanf.order.model.query.OrderPageQuery2;
import com.lanf.order.model.vo.*;
import com.lanf.order.mq.OrderClientTopicName;
import com.lanf.order.mq.message.SignOrderMessage;
import com.lanf.order.service.IOrderItemService;
import com.lanf.order.service.IOrderService;
import com.lanf.order.utils.OrderServiceUtils;
import com.lanf.rocketmq.model.TopicName;
import com.lanf.rocketmq.util.RocketMqClient;
import com.lanf.security.utils.UserUtils;
import com.lanf.system.api.SystemService;
import com.lanf.system.model.vo.ShopVO;
import com.lanf.tcc.service.ITccOperationService;
import lombok.extern.slf4j.Slf4j;
import org.dromara.hmily.annotation.HmilyTCC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
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
    private SystemService systemService;
    @Autowired
    private PayApiService payApiService;
    @Autowired
    private LogisticsApiService logisticsApiService;

    @Autowired
    private ISendMqMessageService sendMqMessageService;
    @Autowired
    private ITccOperationService tccOperationService;


    @Override
    @HmilyTCC(confirmMethod = "confirmCreateOrder", cancelMethod = "cancelCreateOrder")
    public void createOrder(CreateOrderDTO dto) {


    }


    @Transactional
    public void confirmCreateOrder(CreateOrderDTO dto) {
        log.info("创建订单开始:{}", dto);

        OrderDO orderDO = OrderServiceUtils.buildOrderDO(dto);
        //单笔下单时 只有一个商品
        OrderItemDTO orderItemDTO = dto.getOrderItems().get(0);
        OrderItemDO orderItemDO = BeanCopyUtils.copyBean(orderItemDTO, OrderItemDO.class);
        try {
            //order_number 为唯一索引 作为兜底 避免重复下单
            this.save(orderDO);
        } catch (DuplicateKeyException e) {
            log.info("订单已存在");
            return;
        }
        orderItemService.save(orderItemDO);

    }

    public void cancelCreateOrder(CreateOrderDTO dto) {

        log.info("cancelCreateOrder");
    }

    @Override
    public void cancelOrder(CancelOrderBO dto) {

        log.info("cancelOrder[{}]", dto);
        OrderDO orderDO = this.getById(dto.getOrderId());
        if (orderDO == null) {
            throw new BizException("订单不存在");
        }
        if (FrozenStatusEnum.FROZEN.getCode().equals(orderDO.getFrozen())) {
            throw new BizException("订单已冻结");
        }
        if (!OrderStatusEnum.isCancelable(orderDO.getStatus().getCode())) {
            log.error("订单状态异常status:[{}]", orderDO.getStatus());
            throw new BizException("订单状态异常");
        }
        /**
         * DB操作
         *
         */
        String bizKey = buildCancelOrderBizKey(dto.getBizKeySuffix());
        CancelOrderOrderStatusBO cancelOrderBO = new CancelOrderOrderStatusBO();
        cancelOrderBO.setCurrentOrderStatus(orderDO.getStatus());
        tccOperationService.tryOperation(bizKey, JsonUtils.toJsonString(cancelOrderBO));
        boolean update = this.lambdaUpdate()
                .eq(OrderDO::getId, orderDO.getId())
                .eq(OrderDO::getFrozen, FrozenStatusEnum.NORMAL.getCode())
                .eq(OrderDO::getStatus, orderDO.getStatus())
                .eq(OrderDO::getVersion, orderDO.getVersion())
                .set(OrderDO::getStatus, OrderStatusEnum.CANCELLED)
                .set(OrderDO::getFrozen, FrozenStatusEnum.FROZEN.getCode())
                .set(OrderDO::getVersion, orderDO.getVersion() + 1)
                .update();
        if (!update) {
            log.error("订单状态更新异常");
            throw new BizException("订单状态更新异常");
        }

    }

    @Override
    public void confirmCancelOrder(CancelOrderBO dto) {

        log.info("confirmCancelOrder[{}]", JsonUtils.toJsonString(dto));
        OrderDO orderDO = this.getById(dto.getOrderId());
        if (orderDO == null) {
            log.error("订单不存在");
            throw new BizException("订单不存在");
        }

        String bizKey = buildCancelOrderBizKey(dto.getBizKeySuffix());

        boolean operation = tccOperationService.confirmOperation(bizKey);
        if (!operation) {
            log.info("confirm已执行");
            return;
        }
        /**
         *  解冻
         */
        boolean update = this.lambdaUpdate()
                .eq(BaseEntity::getId, orderDO.getId())
                .eq(OrderDO::getStatus, OrderStatusEnum.CANCELLED)
                .eq(OrderDO::getFrozen, FrozenStatusEnum.FROZEN.getCode())
                .eq(OrderDO::getVersion, orderDO.getVersion())
                .set(OrderDO::getVersion, orderDO.getVersion() + 1)
                .set(OrderDO::getFrozen, FrozenStatusEnum.NORMAL.getCode())
                .update();
        if (!update) {
            log.error("订单状态更新异常");
            throw new BizException("订单状态更新异常");
        }
    }

    @Override
    public void cancelCancelOrder(CancelOrderBO dto) {
        log.info("cancelCancelOrder{}", dto);
        String bizKey = buildCancelOrderBizKey(dto.getBizKeySuffix());
        String parameter = tccOperationService.getParameter(bizKey);
        CancelOrderOrderStatusBO cancelOrderBO = JsonUtils.toObject(parameter, CancelOrderOrderStatusBO.class);
        OrderDO orderDO = this.getById(dto.getOrderId());
        if (orderDO == null) {
            log.error("订单不存在");
            throw new BizException("订单不存在");
        }
        boolean operation = tccOperationService.cancelOperation(bizKey);
        if (!operation) {
            log.info("cancel已执行");
            return;
        }
        /**
         * 解冻 并回滚到 try执行之前的状态
         */
        boolean update = this.lambdaUpdate()
                .eq(BaseEntity::getId, orderDO.getId())
                .eq(OrderDO::getStatus, OrderStatusEnum.CANCELLED)
                .eq(OrderDO::getFrozen, FrozenStatusEnum.FROZEN.getCode())
                .eq(OrderDO::getVersion, orderDO.getVersion())
                .set(OrderDO::getStatus, cancelOrderBO.getCurrentOrderStatus())
                .set(OrderDO::getVersion, orderDO.getVersion() + 1)
                .set(OrderDO::getFrozen, FrozenStatusEnum.NORMAL.getCode())
                .update();
        if (!update) {
            log.error("订单状态更新异常");
            throw new BizException("订单状态更新异常");
        }


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

    private String buildCancelOrderBizKey(String bizKeySuffix) {

        return "cancelOrder:" + bizKeySuffix;
    }

    @Transactional
    @Override
    public void orderPaySuccess(Long orderId) {
        OrderDO orderDO = this.lambdaQuery().eq(OrderDO::getId, orderId).one();
        if (orderDO == null) {
            throw new BizException("订单不存在");
        }

        OrderStatusEnum status = orderDO.getStatus();
        if (OrderStatusEnum.WAIT_PAY.equals( status)) {
            log.info("订单状态更新异常");
            throw new BizException("订单状态更新异常");
        }
        OrderDO orderDOUpdate = new OrderDO();
        orderDOUpdate.setId(orderDO.getId());
        orderDOUpdate.setStatus(OrderStatusEnum.PAID);

        this.updateById(orderDOUpdate);
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
        if ( !OrderStatusEnum.PAID.equals(orderDO.getStatus())) {
            log.warn("订单状态异常");
            throw new BizException("订单状态异常");
        }
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
        z

    }

    @Override
    public void delivery(DeliveryDTO dto) {

        Long orderId = dto.getOrderId();
        OrderDO orderDO = this.getById(orderId);

        if (orderDO == null) {
            throw new BizException("订单不存在");
        }
        Integer status = orderDO.getStatus();
        if (status != 2) {
            throw new BizException("订单状态异常");
        }
        OrderDO orderDOUpdate = new OrderDO();
        orderDOUpdate.setId(orderId);
        orderDOUpdate.setStatus(3);
        this.updateById(orderDOUpdate);

    }

    @Override
    public List<OrderVO> queryByOrderId(List<Long> orderIdList) {
        List<OrderDO> orderDOList = this.lambdaQuery().in(BaseEntity::getId, orderIdList).list();


        List<OrderItemDO> orderItemDOList = orderItemService.lambdaQuery().in(OrderItemDO::getOrderId, orderIdList).list();

        Map<Long, List<OrderItemDO>> orderItemMap = new HashMap<>();

        for (OrderItemDO orderItemDO : orderItemDOList) {

            Long orderId = orderItemDO.getOrderId();
            List<OrderItemDO> orderItemDOList1 = orderItemMap.get(orderId);
            if (orderItemDOList1 == null) {
                orderItemDOList1 = new ArrayList<>();
                orderItemMap.put(orderId, orderItemDOList1);
            }
            orderItemDOList1.add(orderItemDO);

        }


        List<OrderVO> salesOutStockOrderAddVOList = new ArrayList<>(orderDOList.size());
        for (OrderDO a : orderDOList) {
            Long id = a.getId();
            Integer expectOutQuantity = 0;
            OrderVO vo = new OrderVO();
            salesOutStockOrderAddVOList.add(vo);
            List<OrderItemVO> inOutStockOrderItemDTOList = new ArrayList<>();
            vo.setInOutStockOrderItemDTOList(inOutStockOrderItemDTOList);
            vo.setOrderId(a.getId());
            vo.setShopId(a.getShopId());
            vo.setOrderStatus(a.getStatus());
            List<OrderItemDO> orderItemDOList1 = orderItemMap.get(id);
            for (OrderItemDO b : orderItemDOList1) {

                OrderItemVO itemVO = new OrderItemVO();
                BeanCopyUtils.copy(b, itemVO);
                itemVO.setUnit(b.getSkuName());

                itemVO.setGoodsTitle(b.getGoodsTitle());
                itemVO.setTotalMoney(BigDecimalUtil.multiply(new BigDecimal(b.getQuantity()), b.getUnitPrice()));
                inOutStockOrderItemDTOList.add(itemVO);
                expectOutQuantity += b.getQuantity();
            }
            //当前订单商品总数量
            vo.setTotalQuantity(expectOutQuantity);
        }

        return salesOutStockOrderAddVOList;
    }

    @Override
    public void outStockFinish(Long orderId) {

        OrderDO orderDO = this.getById(orderId);
        Integer status = orderDO.getStatus();
        if (status != 1) {
            throw new BizException("订单状态异常");
        }
        OrderDO orderDOUpdate = new OrderDO();
        orderDOUpdate.setId(orderId);
        orderDOUpdate.setStatus(2);
        this.updateById(orderDOUpdate);

    }

    @Override
    public void signFor(SignForDTO dto) {

        Long orderId = dto.getOrderId();
        OrderDO orderDO = this.getById(orderId);
        Integer status = orderDO.getStatus();
        if (!OrderStatusEnum.SHIPPED.getCode().equals(status)) {
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
        signOrderMessage.setMerchantId(orderDO.getMerchantId());

        rocketMqClient.sendMessage(OrderClientTopicName.SIGN_ORDER_EVENT_TOPIC,
                JsonUtils.toJsonString(signOrderMessage));

    }

    @Override
    public PageResult<OrderPageVO> orderPage(OrderPageQuery query) {


        IPage<OrderDO> page = new Page<>(query.getPage(), query.getPageSize());
        IPage<OrderDO> pageResult = this.lambdaQuery().
                eq(query.getStatus() != null, OrderDO::getStatus, query.getStatus()).
                eq(OrderDO::getUserId, UserUtils.getUserId()).
                orderByDesc(BaseEntity::getUpdateTime)
                .page(page);

        List<OrderDO> records = pageResult.getRecords();

        if (records.isEmpty()) {

            return PageResult.emptyResult(OrderPageVO.class);
        }
        List<Long> shopIdList = records.stream().map(OrderDO::getShopId).collect(Collectors.toList());
        List<ShopVO> shopVOList = systemService.shopQuery(shopIdList).getData();

        Map<Long, ShopVO> shopVOMap = shopVOList.stream()
                .collect(Collectors.toMap(ShopVO::getId, Function.identity()));

        List<Long> idList = records.stream().map(BaseEntity::getId).collect(Collectors.toList());
        List<OrderItemDO> orderItemDOList = orderItemService.lambdaQuery().in(OrderItemDO::getOrderId, idList).list();
        Map<Long, List<OrderItemDO>> orderMap = new HashMap<>();
        for (OrderItemDO it : orderItemDOList) {

            Long orderId = it.getOrderId();
            List<OrderItemDO> orderItemDOList1 = orderMap.get(orderId);
            if (orderItemDOList1 == null) {
                orderItemDOList1 = new ArrayList<>();
                orderMap.put(orderId, orderItemDOList1);
            }
            orderItemDOList1.add(it);

        }
        //构建返回信息
        List<OrderPageVO> orderPageVOList = new ArrayList<>();
        records.forEach(a -> {

            Long id = a.getId();
            List<OrderItemDO> orderItemDOList1 = orderMap.get(id);
            List<OrderPageVO> v2 = new ArrayList<>();
            Long shopId1 = a.getShopId();
            Long shopId = null;
            String shopName = null;
            String statusDesc = null;
            Integer status = a.getStatus();
            String payMoneyDesc = null;

            //第一个项目
            ShopVO shopVO = shopVOMap.get(shopId1);
            shopId = shopId1;
            shopName = shopVO.getName();
            if (status == 0) {
                statusDesc = "等待买家付款";

            }
            if (status == 1 || status == 2) {
                statusDesc = "买家已付款";

            }
            if (status == 3) {
                statusDesc = "卖家已发货";

            }
            if (status == 4 || status == 5) {
                statusDesc = "交易成功";

            }

            if (status == 6) {
                statusDesc = "交易已关闭";

            }
            payMoneyDesc = "实付款￥" + a.getActualPayMoney();

            List<OrderItemPageVO> orderItemPageVOList = new ArrayList<>();
            OrderPageVO orderPageVO = new OrderPageVO();

            for (OrderItemDO b : orderItemDOList1) {

                OrderItemPageVO orderItemPageVO = new OrderItemPageVO();
                orderItemPageVO.setGoodsTitle(b.getGoodsTitle());
                orderItemPageVO.setSkuName(b.getSkuName());
                orderItemPageVO.setQuantity(b.getQuantity());
                orderItemPageVO.setUnitPrice(b.getUnitPrice());
                orderItemPageVO.setSkuPictureAddress(b.getSkuPictureAddress());
                orderItemPageVOList.add(orderItemPageVO);

            }
            orderPageVO.setItemPageVOList(orderItemPageVOList);
            orderPageVO.setOrderId(id);
            orderPageVO.setShopId(shopId);
            orderPageVO.setShopName(shopName);
            orderPageVO.setStatus(status);
            orderPageVO.setStatusDesc(statusDesc);
            orderPageVO.setPayMoneyDesc(payMoneyDesc);
            v2.add(orderPageVO);
            orderPageVOList.addAll(v2);
        });
        PageResult<OrderPageVO> result = new PageResult<>();
        result.setRecords(orderPageVOList);
        result.setSize(pageResult.getSize());
        result.setTotal(pageResult.getTotal());
        return result;
    }


    @Override
    public OrderDetailVO orderDetail(Long id) {

        OrderDO orderDO = this.getById(id);
        if (orderDO == null) {

            throw new BizException("订单不存在");
        }
        List<OrderItemDO> orderItemDOList = orderItemService.lambdaQuery().eq(OrderItemDO::getOrderId, id).list();

        OrderTradeVO orderTradeVO = payApiService.queryOrderTradeByOrderId(id).getData();

        Long shopId = orderDO.getShopId();
        List<Long> shopIdList = new ArrayList<>();
        shopIdList.add(shopId);
        List<ShopVO> shopVOList = systemService.shopQuery(shopIdList).getData();
        Map<Long, ShopVO> shopVOMap = shopVOList.stream()
                .collect(Collectors.toMap(ShopVO::getId, Function.identity()));

        /**
         * 构建 orderItemPageVOList
         */
        List<OrderItemPageVO> orderItemPageVOList = new ArrayList<>();
        for (OrderItemDO b : orderItemDOList) {

            OrderItemPageVO orderItemPageVO = new OrderItemPageVO();
            orderItemPageVO.setGoodsTitle(b.getGoodsTitle());
            orderItemPageVO.setSkuName(b.getSkuName());
            orderItemPageVO.setQuantity(b.getQuantity());
            orderItemPageVO.setUnitPrice(b.getUnitPrice());
            orderItemPageVO.setSkuPictureAddress(b.getSkuPictureAddress());
            orderItemPageVOList.add(orderItemPageVO);

        }
        /**
         * 构建orderDetailVO
         */

        BigDecimal payMoney = null;
        String payTypeName = null;
        Date payFinishTime = null;

        OrderDetailVO orderDetailVO = new OrderDetailVO();

        if (orderTradeVO != null) {
            payMoney = orderTradeVO.getPayMoney();
            payFinishTime = orderTradeVO.getPayFinishTime();
            if (orderTradeVO.getPayType() == 0) {
                payTypeName = "支付宝";
            }
        }
        orderDetailVO.setId(id);
        orderDetailVO.setPayMoney(payMoney);
        orderDetailVO.setOrderNumber(orderDO.getOrderNumber());
        orderDetailVO.setPayTypeName(payTypeName);
        orderDetailVO.setPayFinishTime(payFinishTime);
        orderDetailVO.setOrderCreateTime(orderDO.getCreateTime());
        orderDetailVO.setTakeAddress(orderDO.getTakeAddress());
        orderDetailVO.setShopId(shopId);
        orderDetailVO.setShopName(shopVOMap.get(shopId).getName());
        //
        orderDetailVO.setOrderStatusName("已完成");
        orderDetailVO.setItemPageVOList(orderItemPageVOList);
        return orderDetailVO;
    }

    @Override
    public void cancelOrder(Long orderId) {

        OrderDO orderDO = this.getById(orderId);
        if (orderDO == null) {

            throw new BizException("订单不存在");
        }
        Integer status = orderDO.getStatus();
        if (!(status.equals(1) || status.equals(0))) {
            throw new BizException("订单已出库，无法取消");
        }
        updateOrderCancel(orderId);
        CancelOrderBO dto = new CancelOrderBO();
        dto.setOrderId(orderId);
        rocketMqClient.sendMessage(TopicName.CANCEL_ORDER_TOPIC, dto);
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
        Integer status = orderDO.getStatus();
        if (status != 0) {
            //已付款，忽略
            log.info("订单已支付");
        } else {
            //订单待付款状态
            log.info("订单待付款，关闭订单");
            updateOrderCancel(orderId);

        }


    }

    @Override
    public PageResult<OrderPageVO2> orderPageVO2(OrderPageQuery2 query2) {

        /**
         * 查询订单信息
         */
        IPage<OrderDO> page = new Page<>(query2.getPage(), query2.getPageSize());
        IPage<OrderDO> pageResult = this.lambdaQuery().
                eq(OrderDO::getShopId, UserUtils.getShopId()).
                eq(query2.getStatus() != null, OrderDO::getStatus, query2.getStatus()).
                orderByDesc(BaseEntity::getId)
                .page(page);

        List<OrderDO> records = pageResult.getRecords();

        if (records.isEmpty()) {

            return PageResult.emptyResult(OrderPageVO2.class);
        }
        List<Long> orderIdList = records.stream().map(BaseEntity::getId).collect(Collectors.toList());

        /**
         * 查询支付信息
         */
        TradeOrderBathQuery query = new TradeOrderBathQuery();
        query.setOrderIdList(orderIdList);
        List<TradeOrderBathVO> tradeOrderBathVOList = payApiService.tradeOrderBathQuery(query).getData();
        Map<Long, TradeOrderBathVO> tradeOrderMap = tradeOrderBathVOList.stream()
                .collect(Collectors.toMap(TradeOrderBathVO::getOrderId, Function.identity()));
        /**
         * 构建返回信息
         */
        List<OrderPageVO2> orderPageVO2List = new ArrayList<>();

        for (OrderDO a : records) {

            String takeAddress = a.getTakeAddress();
            String[] takeAddressSplit = takeAddress.split(",");
            String consignee = takeAddressSplit[0];
            //收货人联系电话
            String phone = takeAddressSplit[1];
            //收货地址
            String takeAddress2 = takeAddressSplit[2];
            TradeOrderBathVO tradeOrderBathVO = tradeOrderMap.get(a.getId());
            OrderPageVO2 orderPageVO2 = new OrderPageVO2();
            orderPageVO2.setId(a.getId());
            orderPageVO2.setPayType(tradeOrderBathVO.getPayType());
            orderPageVO2.setPayMoney(tradeOrderBathVO.getPayMoney());
            orderPageVO2.setCreateTime(a.getCreateTime());
            orderPageVO2.setStatus(a.getStatus());
            orderPageVO2.setOrderNumber(a.getOrderNumber());
            orderPageVO2.setConsignee(consignee);
            orderPageVO2.setPhone(phone);
            orderPageVO2.setTakeAddress(takeAddress2);
            orderPageVO2List.add(orderPageVO2);
        }

        return PageResult.toPageResult(pageResult, orderPageVO2List);
    }

    @Override
    public OrderDetailVO2 orderDetailVO2(Long orderId) {

        OrderTradeVO orderTradeVO = payApiService.queryOrderTradeByOrderId(orderId).getData();
        List<OrderItemDO> orderItemDOList = orderItemService.lambdaQuery().eq(OrderItemDO::getOrderId, orderId).list();

        /**
         * 构建返回信息
         */
        OrderDO orderDO = this.getById(orderId);
        String takeAddress = orderDO.getTakeAddress();
        String[] takeAddressSplit = takeAddress.split(",");
        String consignee = takeAddressSplit[0];
        String phone = takeAddressSplit[1];
        String takeAddress2 = takeAddressSplit[2];
        List<OrderItemDetailVO> orderItemDetailVOS = BeanCopyUtils.copyBeanList(orderItemDOList, OrderItemDetailVO.class);
        //构建OrderDetailVO2
        OrderDetailVO2 vo2 = new OrderDetailVO2();
        vo2.setOrderNumber(orderDO.getOrderNumber());
        vo2.setCreateTime(orderDO.getCreateTime());
        vo2.setStatus(orderDO.getStatus());
        vo2.setStatusName(getStatusName(orderDO.getStatus()));
        vo2.setConsignee(consignee);
        vo2.setPhone(phone);
        vo2.setTakeAddress(takeAddress2);
        vo2.setOrderItemDetailVOList(orderItemDetailVOS);
        //填充支付信息
        if (orderTradeVO != null) {
            vo2.setOrderMoney(orderTradeVO.getOrderMoney());
            vo2.setPayMoney(orderTradeVO.getPayMoney());
            vo2.setDiscountMoney(orderTradeVO.getDiscountMoney());
            vo2.setDiscountType(orderTradeVO.getDiscountType());
            vo2.setDiscountTypeName(orderTradeVO.getDiscountTypeName());
            vo2.setPayType(orderTradeVO.getPayType());
            vo2.setPayTypeName(orderTradeVO.getPayTypeName());
            vo2.setPayFinishTime(orderTradeVO.getPayFinishTime());
        }
        //填充物流信息
        LogisticsVO logisticsVO = logisticsApiService.logisticsDetail(orderId).getData();
        List<LogisticsTrackVO> allLogisticsTrack = new ArrayList<>();
        List<LogisticsTrackStatusVO> trackStatusVOList = logisticsVO.getLogisticsTrackStatusVOList();
        trackStatusVOList.forEach(a -> {
            allLogisticsTrack.addAll(a.getLogisticsTrackVOList());
        });

        vo2.setExpressCompany(logisticsVO.getExpressCompany());
        vo2.setExpressNumber(logisticsVO.getNumber());
        vo2.setLogisticsTrackVOList(allLogisticsTrack);

        return vo2;
    }


    private String getStatusName(Integer status) {

        if (status == 0) {

            return "待付款";
        }
        if (status == 1) {

            return "待出库";
        }
        if (status == 2) {

            return "已出库";
        }
        if (status == 3) {

            return "已发货";
        }
        if (status == 4) {

            return "已完成";
        }
        if (status == 5) {

            return "已关闭";
        }
        if (status == 6) {

            return "已取消";
        }
        return "待付款";
    }

    @Override
    public Integer contrastBillOrderCountQuery(ContrastBillOrderQuery query) {


        return buildLambdaQueryChainWrapper(query).count();
    }

    @Override
    public List<Long> contrastBillOrderIdQuery(ContrastBillOrderQuery query) {

        IPage<OrderDO> page = new Page<>(query.getPage(), query.getPageSize());

        List<OrderDO> records = buildLambdaQueryChainWrapper(query).
                page(page).getRecords();

        return records.stream().map(BaseEntity::getId).collect(Collectors.toList());
    }


    private LambdaQueryChainWrapper<OrderDO> buildLambdaQueryChainWrapper(ContrastBillOrderQuery query) {


        return this.lambdaQuery().select(BaseEntity::getId);
    }

    @Override
    public OrderVO2 queryById(Long id) {

        OrderDO orderDO = this.getById(id);

        return BeanCopyUtils.copyBean(orderDO, OrderVO2.class);
    }


}
