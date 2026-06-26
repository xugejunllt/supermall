package com.lanf.order.service.impl;


import com.lanf.api.goods.api.GoodsApiService;
import com.lanf.api.goods.model.bo.GoodsItem;
import com.lanf.api.goods.model.bo.GoodsSku;
import com.lanf.api.goods.model.bo.ShopGoods;
import com.lanf.api.goods.model.dto.*;
import com.lanf.api.goods.model.vo.CalculateOrderTotalAmountVO;
import com.lanf.api.goods.model.vo.ClearCartVO;
import com.lanf.api.goods.model.vo.DeductStockVO;
import com.lanf.api.goods.model.vo.ValidateCartItemVO;
import com.lanf.api.order.model.enums.OrderTypeEnum;
import com.lanf.api.order.model.enums.ShippingStatusEnum;
import com.lanf.api.order.mq.message.OrderCreateSuccessMessage;
import com.lanf.api.order.mq.message.SecKillPlaneCreateOrderSuccessMessage;
import com.lanf.api.pay.api.PayApiService;
import com.lanf.api.pay.model.dto.CreateMergeTradeOrderDTO;
import com.lanf.api.pay.model.dto.CreateMergeTradeOrderItemDTO;
import com.lanf.api.pay.model.dto.CreateTradeOrderDTO;
import com.lanf.api.user.api.UserCacheService;
import com.lanf.api.user.model.vo.AddressListVO;
import com.lanf.cache.aop.DistributedLock;
import com.lanf.common.utils.*;
import com.lanf.constant.exception.BizException;
import com.lanf.constant.model.enums.order.OrderStatusEnum;
import com.lanf.constant.mq.OrderTopicWithTag;
import com.lanf.constant.result.RpcResultParser;
import com.lanf.constant.utils.IdUtils;
import com.lanf.constant.utils.UserContext;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.order.model.bo.BuildBathCreateOrderBO;
import com.lanf.order.model.bo.OrderInitParamsBO;
import com.lanf.order.model.bo.StartSubmitCartBO;
import com.lanf.order.model.dto.*;
import com.lanf.order.model.entity.MainOrderDO;
import com.lanf.order.model.entity.OrderDO;
import com.lanf.order.model.entity.OrderItemDO;
import com.lanf.order.model.entity.OrderStatusTraceDO;
import com.lanf.order.model.vo.CalculateOrderAmountVO;
import com.lanf.order.model.vo.PlaceOrderVO;
import com.lanf.order.model.vo.SubmitCartVO;
import com.lanf.order.model.vo.ValidateCartVO;
import com.lanf.order.mq.constant.OrderMqTopicName;
import com.lanf.order.mq.message.BathAddShippingTrackMessage;
import com.lanf.order.mq.message.ShippingTrackMessage;
import com.lanf.order.service.OrderManagerService;
import com.lanf.order.service.order.IMainOrderService;
import com.lanf.order.service.order.IOrderItemService;
import com.lanf.order.service.order.IOrderService;
import com.lanf.order.service.order.IOrderStatusTraceService;
import com.lanf.order.utils.OrderServiceUtils;
import com.lanf.rocketmq.exception.MessageRetryConsumeException;
import com.lanf.rocketmq.model.message.CancelExpiredOrderMessage;
import com.lanf.rocketmq.model.message.CancelOrderEventMessage;
import com.lanf.rocketmq.model.message.OrderGoodsInfo;
import com.lanf.rocketmq.util.RocketMqClient;
import com.lanf.welfare.api.SecKillResultCache;
import com.lanf.welfare.api.WelfareApiService;
import com.lanf.welfare.model.bo.DiscountInfoBO;
import com.lanf.welfare.model.dto.CalculateDiscountAmountDTO;
import com.lanf.welfare.model.dto.UseMultipleCouponDTO;
import com.lanf.welfare.model.enums.SecKillResultEnum;
import com.lanf.welfare.model.vo.CalculateDiscountAmountVO;
import com.lanf.welfare.mq.message.SecKillPlaneMessage;
import lombok.extern.slf4j.Slf4j;
import org.dromara.hmily.annotation.HmilyTCC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OrderManagerServiceImpl implements OrderManagerService {


    @Autowired
    private PayApiService payApiService;

    @Autowired
    private GoodsApiService goodsApiService;

    @Autowired
    private WelfareApiService welfareApiService;
    @Autowired
    private UserCacheService userCacheService;

    @Autowired
    private RocketMqClient rocketMqClient;
    @Autowired
    private IOrderService orderService;
    @Autowired
    private IOrderStatusTraceService orderStatusTraceService;
    @Autowired
    private IOrderItemService orderItemService;
    @Autowired
    private IMainOrderService mainOrderService;
    @Autowired
    private SecKillResultCache secKillResultCache;
    @Value("${order.expireInterval}")
    private Long expireInterval;



    @Override
    public CalculateOrderAmountVO calculateOrderAmount(CalculateOrderAmountDTO dto) {


        CalculateOrderTotalAmountVO amountVO = calculateOrderTotalAmount(dto);
        BigDecimal totalAmount = amountVO.getTotalAmount();
        //计算优惠金额
        CalculateDiscountAmountVO calculateDiscountAmountVO = calculateDiscountAmount(dto,
                UserContext.getUserId(), totalAmount);
        /**
         * 构建返回结果
         */
        CalculateOrderAmountVO orderAmountVO = new CalculateOrderAmountVO();
        orderAmountVO.setTotalAmount(totalAmount);
        orderAmountVO.setDiscountAmount(calculateDiscountAmountVO.getTotalDiscountAmount());
        orderAmountVO.setPayAmount(BigDecimalUtil.subtract(totalAmount, calculateDiscountAmountVO.getTotalDiscountAmount()));
        orderAmountVO.setOrderNumber(OrderServiceUtils.generateOrderNumber());
        return orderAmountVO;

    }


    /**
     * 计算订单总金额的私有方法
     *
     * @param dto 计算订单金额的DTO
     * @return 计算后的金额结果
     */
    private CalculateOrderTotalAmountVO calculateOrderTotalAmount(CalculateOrderAmountDTO dto) {
        CalculateOrderTotalAmountDTO calculateOrderTotalAmountDTO = new CalculateOrderTotalAmountDTO();
        calculateOrderTotalAmountDTO.setSkuId(dto.getSkuId());
        calculateOrderTotalAmountDTO.setQuantity(dto.getQuantity());

        return RpcResultParser.parseResult(
                goodsApiService.calculateOrderTotalAmount(calculateOrderTotalAmountDTO));
    }

    private CalculateDiscountAmountVO calculateDiscountAmount(CalculateOrderAmountDTO dto, Long userId, BigDecimal totalAmount) {
        CalculateDiscountAmountDTO discountAmountDTO = new CalculateDiscountAmountDTO();
        discountAmountDTO.setUserId(userId);
        discountAmountDTO.setShopId(dto.getSkuId());
        discountAmountDTO.setTotalAmount(totalAmount);
        /**
         * 暂时不加入优惠卷
         */

//        Result<CalculateDiscountAmountVO> calculateDiscountAmountVOResult = welfareApiService.calculateDiscountAmount(discountAmountDTO);

//        return RpcResultParser.parseResult(calculateDiscountAmountVOResult);
        CalculateDiscountAmountVO vo = new CalculateDiscountAmountVO();
        vo.setTotalDiscountAmount(BigDecimal.ZERO);
        return vo;
    }

    /**
     * 立即下单
     */
    @DistributedLock(key = "#orderDTO.orderNumber")
    @Override
    public PlaceOrderVO placeOrder(PlaceOrderDTO orderDTO) {

        OrderInitParamsBO orderInitParamsBO = initParams(orderDTO);
        orderDTO.setOrderInitParamsBO(orderInitParamsBO);
        /**
         * 获取代理对象
         */
        OrderManagerService managerService = BeanUtil.getBean(OrderManagerService.class);

        return managerService.startPlaceOrder(orderDTO);
    }

    @HmilyTCC(confirmMethod = "confirmPlaceOrder", cancelMethod = "cancelPlaceOrder")
    @Override
    public PlaceOrderVO startPlaceOrder(PlaceOrderDTO orderDTO) {

        OrderInitParamsBO orderInitParamsBO = orderDTO.getOrderInitParamsBO();
        /**
         * 扣减库存
         */
        DeductStockVO deductStockVO = deductStock(orderDTO, orderInitParamsBO);

        /**
         * 使用优惠卷
         */
        CalculateDiscountAmountVO amountVO = useMultipleCoupon(orderDTO, orderInitParamsBO,
                deductStockVO.getTotalAmount());
        /**
         * 创建交易单
         */
        BigDecimal tradeMoney = calculateActualPayment(deductStockVO.getTotalAmount(), amountVO);
        createPayOrder(orderInitParamsBO, orderDTO, tradeMoney);
        /**
         * 创建订单
         *
         */
        createOrder(orderDTO, orderInitParamsBO, deductStockVO, tradeMoney, amountVO);

        /**
         * 构建返回结果
         */
        PlaceOrderVO vo = new PlaceOrderVO();
        vo.setOrderNumber(orderInitParamsBO.getOrderNumber());
        vo.setOrderId(orderInitParamsBO.getOrderId());
        return vo;
    }

    @Override
    public ValidateCartVO validateCart(ValidateCartDTO dto) {


        /**
         * 校验购物车
         */
        ValidateCartItemVO validateCartItemVO = RpcResultParser.parseResult(goodsApiService.validateCartItem(dto));

        /**
         * 构建返回值
         */
        //这里不考虑使用优惠卷场景
        BigDecimal discountPrice = new BigDecimal(0);
        BigDecimal actualPrice = validateCartItemVO.getTotalPrice();
        ValidateCartVO validateCartVO = new ValidateCartVO();
        validateCartVO.setDiscountPrice(discountPrice);
        validateCartVO.setActualPrice(actualPrice);
        validateCartVO.setGoodsVOList(validateCartItemVO.getGoodsVOList());
        validateCartVO.setTotalPrice(validateCartItemVO.getTotalPrice());
        validateCartVO.setMainOrderNumber(OrderServiceUtils.generateOrderNumber());
        return validateCartVO;
    }


    @Override
    public void confirmPlaceOrder(PlaceOrderDTO orderDTO) {
        /**
         * 发送MQ 订单创建成功消息
         */

        OrderInitParamsBO initParamsBO = orderDTO.getOrderInitParamsBO();
        sendOrderCreateSuccessMessage(initParamsBO.getOrderId(),
                initParamsBO.getUserId());
    }

    /**
     *
     */
    @Transactional
    @Override
    public void cancelPlaceOrder(PlaceOrderDTO orderDTO) {
        /**
         * 如果数据不存在 那么执行删除操作也没有影响
         * 如果数据库异常 那么会抛出异常进行重试
         */
        OrderInitParamsBO orderInitParamsBO = orderDTO.getOrderInitParamsBO();
        orderService.lambdaUpdate()
                .eq(OrderDO::getUserId, orderInitParamsBO.getUserId())
                .eq(OrderDO::getId, orderInitParamsBO.getOrderId())
                .remove();
        orderItemService.lambdaUpdate()
                .eq(OrderItemDO::getUserId, orderInitParamsBO.getUserId())
                .eq(OrderItemDO::getOrderId, orderInitParamsBO.getOrderId())
                .remove();
        orderStatusTraceService.lambdaUpdate()
                .eq(OrderStatusTraceDO::getOrderId, orderInitParamsBO.getOrderId())
                .eq(OrderStatusTraceDO::getUserId, orderInitParamsBO.getUserId())
                .remove();

    }

    /**
     * 创建订单项
     *
     * @param orderInitParamsBO 订单初始化参数
     * @param orderDTO          下单请求参数
     * @param deductStockVO     扣减库存结果
     * @return 订单项对象
     */
    private OrderItemDTO createOrderItem(OrderInitParamsBO orderInitParamsBO, PlaceOrderDTO orderDTO, DeductStockVO deductStockVO) {

        GoodsSku goodsSkuBO = deductStockVO.getGoodsSkuBO();
        OrderItemDTO orderItem = new OrderItemDTO();
        orderItem.setOrderId(orderInitParamsBO.getOrderId());
        orderItem.setGoodsId(goodsSkuBO.getGoodsId());
        orderItem.setGoodsName(goodsSkuBO.getGoodsName());
        orderItem.setGoodsTitle(goodsSkuBO.getGoodsTitle());
        orderItem.setSkuId(goodsSkuBO.getSkuId());
        orderItem.setSkuName(goodsSkuBO.getSkuName());
        orderItem.setSkuPictureAddress(goodsSkuBO.getSkuPictureAddress());
        orderItem.setQuantity(orderDTO.getQuantity());
        orderItem.setUnitPrice(goodsSkuBO.getPrice());
        orderItem.setGoodsVersion(goodsSkuBO.getGoodsVersion());
        orderItem.setSkuVersion(goodsSkuBO.getSkuVersion());
        orderItem.setSkuCode(goodsSkuBO.getSkuCode());
        orderItem.setWarehouseId(goodsSkuBO.getWarehouseId());
        orderItem.setTenantId(goodsSkuBO.getTenantId());
        orderItem.setUserId(orderInitParamsBO.getUserId());
        orderItem.setSkuName(goodsSkuBO.getSkuName());
        return orderItem;
    }

    /**
     * 创建订单
     *
     * @param orderDTO          下单请求参数
     * @param orderInitParamsBO 订单初始化参数
     * @param deductStockVO     扣减库存结果
     * @param tradeMoney        实际支付金额
     * @param amountVO          优惠券计算结果
     */
    private void createOrder(PlaceOrderDTO orderDTO, OrderInitParamsBO orderInitParamsBO, DeductStockVO deductStockVO,
                             BigDecimal tradeMoney, CalculateDiscountAmountVO amountVO) {
        List<OrderItemDTO> orderItems = new ArrayList<>(1);
        List<DiscountInfoBO> discountInfoBOS = amountVO != null ? amountVO.getDiscountInfoBOList() : null;
        OrderItemDTO orderItem = createOrderItem(orderInitParamsBO, orderDTO, deductStockVO);
        orderItems.add(orderItem);
        BigDecimal discountAmount = getDiscountAmount(amountVO);
        CreateOrderDTO createOrderDTO = new CreateOrderDTO();
        createOrderDTO.setOrderId(orderInitParamsBO.getOrderId());
        createOrderDTO.setShopId(orderDTO.getShopId());
        createOrderDTO.setShopName(deductStockVO.getGoodsSkuBO().getShopName());
        createOrderDTO.setUserId(orderInitParamsBO.getUserId());
        createOrderDTO.setOrderNumber(orderDTO.getOrderNumber());
        createOrderDTO.setTotalMoney(deductStockVO.getTotalAmount());
        createOrderDTO.setActualPayMoney(tradeMoney);
        createOrderDTO.setDiscountAmount(discountAmount);
        createOrderDTO.setDiscountInfoBOS(discountInfoBOS);
        createOrderDTO.setAddressListVO(orderInitParamsBO.getAddressListVO());
        createOrderDTO.setOrderItems(orderItems);
        createOrderDTO.setTenantId(deductStockVO.getGoodsSkuBO().getTenantId());

        orderService.createOrder(createOrderDTO);
    }

    /**
     * 创建支付订单
     *
     * @param orderInitParamsBO 订单初始化参数
     * @param orderDTO          下单请求参数
     * @param tradeMoney        交易金额
     */
    private void createPayOrder(OrderInitParamsBO orderInitParamsBO, PlaceOrderDTO orderDTO, BigDecimal tradeMoney) {
        CreateTradeOrderDTO dto = new CreateTradeOrderDTO();
        dto.setUserId(orderInitParamsBO.getUserId());
        dto.setOrderId(orderInitParamsBO.getOrderId());
        dto.setTradeMoney(tradeMoney);
        dto.setOrderNumber(orderDTO.getOrderNumber());
        RpcResultParser.parseResult(payApiService.createPayOrder(dto));

    }

    /**
     * 从优惠券计算结果中获取折扣金额
     *
     * @param amountVO 优惠券计算结果
     * @return 折扣金额
     */
    private BigDecimal getDiscountAmount(CalculateDiscountAmountVO amountVO) {
        return amountVO != null ? amountVO.getTotalDiscountAmount() : BigDecimal.ZERO;
    }

    /**
     * 计算订单实际支付金额
     *
     * @param totalAmount 商品总金额
     * @param amountVO    优惠券计算结果，包含折扣金额
     * @return 实际支付金额
     */
    private BigDecimal calculateActualPayment(BigDecimal totalAmount, CalculateDiscountAmountVO amountVO) {
        BigDecimal discountAmount = getDiscountAmount(amountVO);
        BigDecimal actualPayment = BigDecimalUtil.subtract(totalAmount, discountAmount);
        validatePaymentAmount(actualPayment);
        return actualPayment;
    }

    /**
     * 校验实际支付金额是否小于0
     *
     * @param actualPayment 实际支付金额
     */
    private void validatePaymentAmount(BigDecimal actualPayment) {
        if (BigDecimalUtil.lt(actualPayment, BigDecimal.ZERO)) {
            throw new BizException("实际支付金额不能小于0");
        }
    }

    @Transactional
    public void sendOrderCreateSuccessMessage(Long orderId, Long userId) {
        OrderCreateSuccessMessage message = new OrderCreateSuccessMessage();
        message.setOrderId(orderId);
        message.setUserId(userId);

        CancelExpiredOrderMessage message2 = new CancelExpiredOrderMessage();
        message.setOrderId(orderId);
        message.setUserId(userId);

        OrderDO orderDO = orderService.lambdaQuery()
                .eq(BaseEntity::getId, orderId)
                .eq(OrderDO::getUserId, userId)
                .one();

        BathAddShippingTrackMessage bathMessage = new BathAddShippingTrackMessage();
        bathMessage.setOrderId(orderId);
        bathMessage.setTenantId(orderDO.getTenantId());
        bathMessage.setUserId(userId);
        List<ShippingTrackMessage> shippingTrackList = new ArrayList<>();
        ShippingTrackMessage trackMessage = new ShippingTrackMessage();
        trackMessage.setStatus(ShippingStatusEnum.ORDER_PLACED);
        trackMessage.setFinishTime(new Date());
        trackMessage.setFinishContent("订单已提交");
        trackMessage.setFlowNo(IStringUtils.hashToUniqueString(orderId + trackMessage.getFinishContent()));
        shippingTrackList.add(trackMessage);
        bathMessage.setShippingTrackList(shippingTrackList);


        /**
         *
         * 发送延迟 关单消息 提前5分钟
         *
         */
        rocketMqClient.sendDelayMessage(OrderMqTopicName.CANCEL_EXPIRED_ORDER_TOPIC,
                JsonUtils.toJsonString(message2), TimeUnit.MINUTES, (int) (expireInterval - 5));

        /**
         * 发送订单创建成功消息
         */
        rocketMqClient.sendOrderlyMessageWithTags(OrderTopicWithTag.ORDER_EVENT_TOPIC,
                OrderStatusEnum.WAIT_PAY.getTag(), JsonUtils.toJsonString(message),
                orderId.toString());

        rocketMqClient.sendMessage(OrderMqTopicName.BATH_ADD_SHIPPING_TRACK_TOPIC, JsonUtils.toJsonString(bathMessage));


}


    private CalculateDiscountAmountVO useMultipleCoupon(PlaceOrderDTO orderDTO, OrderInitParamsBO orderInitParamsBO, BigDecimal totalAmount) {
        if (IStringUtils.isEmpty(orderDTO.getCouponIds())) {
            return null;
        }
        UseMultipleCouponDTO dto = new UseMultipleCouponDTO();
        dto.setOrderId(orderInitParamsBO.getOrderId());
        dto.setUserId(orderInitParamsBO.getUserId());
        dto.setShopId(orderDTO.getShopId());
        dto.setTotalAmount(totalAmount);
        dto.setCouponIds(orderDTO.getCouponIds());
        CalculateDiscountAmountVO amountVO = RpcResultParser.parseResult(welfareApiService.useMultipleCoupon(dto));

        return amountVO;
    }

    private DeductStockVO deductStock(PlaceOrderDTO orderDTO, OrderInitParamsBO orderInitParamsBO) {

        //表示唯一一次库存扣减 使用tcc_operation 表就需要
        String bizKeyPrx = orderDTO.getOrderNumber() + "_" + orderDTO.getSkuCode();
        DeductStockDTO deductStockDTO = new DeductStockDTO();
        deductStockDTO.setOrderId(orderInitParamsBO.getOrderId());
        deductStockDTO.setSkuCode(orderDTO.getSkuCode());
        deductStockDTO.setQuantity(orderDTO.getQuantity());
        deductStockDTO.setBizKeyPrx(bizKeyPrx);
        deductStockDTO.setGoodsId(orderDTO.getGoodsId());
        deductStockDTO.setWarehouseId(orderDTO.getWarehouseId());
        deductStockDTO.setOrderNumber(orderDTO.getOrderNumber());
        return RpcResultParser.parseResult(goodsApiService.deductStock(deductStockDTO));


    }

    private OrderInitParamsBO initParams(PlaceOrderDTO orderDTO) {


        OrderInitParamsBO orderInitParamsBO = new OrderInitParamsBO();
        orderInitParamsBO.setOrderId(IdUtils.generateId());
        orderInitParamsBO.setUserId(UserContext.getUserId());
        orderInitParamsBO.setOrderNumber(orderDTO.getOrderNumber());

        List<AddressListVO> addressListVOS = RpcResultParser.parseResult(userCacheService.addressListQuery());
        Long addressId = orderDTO.getAddressId();
        AddressListVO addressListVO1 = addressListVOS.stream()
                .filter(addressListVO -> addressListVO.getId().equals(addressId)).
                findFirst().orElse(null);
        if (addressListVO1 == null) {
            throw new BizException("收货地址不存在");
        }
        orderInitParamsBO.setAddressListVO(addressListVO1);

        return orderInitParamsBO;
    }

    @DistributedLock(key = "#dto.mainOrderNumber")
    @Override
    public SubmitCartVO submitCart(SubmitCartDTO dto) {

        StartSubmitCartBO initParamsBO = buildSubmitCartOrderInitParamsBO(dto);

        OrderManagerService managerService = BeanUtil.getBean(OrderManagerService.class);

        return managerService.startSubmitCart(initParamsBO);
    }

    @HmilyTCC(confirmMethod = "confirmSubmitCart", cancelMethod = "cancelSubmitCart")
    @Override
    public SubmitCartVO startSubmitCart(StartSubmitCartBO dto) {


        /**
         * 扣减库存
         */
        BathDeductStockDTO bathDeductStockDTO = buildBathDeductStockDTO(dto.getBathCreateOrderDTO());
        RpcResultParser.parseResult(goodsApiService.bathDeductStock(bathDeductStockDTO));

        /**
         * 清空购物车
         */
        RpcResultParser.parseResult(goodsApiService.clearCart(dto.getClearCartDTO()));

        /**
         * 创建交易单
         */
        CreateMergeTradeOrderDTO createMergeTradeOrderDTO = buildCreateMergeTradeOrderDTO(dto.getBathCreateOrderDTO());
        RpcResultParser.parseResult(payApiService.createMergeTradeOrder(createMergeTradeOrderDTO));
        /**
         * 创建订单
         */
        mainOrderService.bathCreateOrder(dto.getBathCreateOrderDTO());

        /**
         * 构建返回值
         */
        SubmitCartVO submitCartVO = new SubmitCartVO();
        submitCartVO.setMainOrderNumber(dto.getBathCreateOrderDTO().getMainOrderNumber());
        submitCartVO.setMainOrderId(dto.getBathCreateOrderDTO().getMainOrderId());
        return submitCartVO;
    }

    private CreateMergeTradeOrderDTO buildCreateMergeTradeOrderDTO(BathCreateOrderDTO bathCreateOrderDTO) {

        List<CreateOrderDTO> createOrderDTOList = bathCreateOrderDTO.getCreateOrderDTOList();

        CreateMergeTradeOrderDTO createMergeTradeOrderDTO = new CreateMergeTradeOrderDTO();
        createMergeTradeOrderDTO.setMainOrderId(bathCreateOrderDTO.getMainOrderId());
        createMergeTradeOrderDTO.setMainOrderNumber(bathCreateOrderDTO.getMainOrderNumber());
        createMergeTradeOrderDTO.setUserId(bathCreateOrderDTO.getUserId());

        List<CreateMergeTradeOrderItemDTO> tradeOrderItemList = getCreateMergeTradeOrderItemDTOS(createOrderDTOList);
        createMergeTradeOrderDTO.setTradeOrderItemList(tradeOrderItemList);

        return createMergeTradeOrderDTO;

    }

    private List<CreateMergeTradeOrderItemDTO> getCreateMergeTradeOrderItemDTOS(List<CreateOrderDTO> createOrderDTOList) {
        List<CreateMergeTradeOrderItemDTO> tradeOrderItemList = new ArrayList<>(createOrderDTOList.size());

        for (CreateOrderDTO createOrderDTO : createOrderDTOList) {
            CreateMergeTradeOrderItemDTO tradeOrderItemDTO = new CreateMergeTradeOrderItemDTO();
            tradeOrderItemDTO.setOrderNumber(createOrderDTO.getOrderNumber());
            tradeOrderItemDTO.setOrderId(createOrderDTO.getOrderId());
            tradeOrderItemDTO.setTradeMoney(createOrderDTO.getActualPayMoney());
            tradeOrderItemList.add(tradeOrderItemDTO);
        }
        return tradeOrderItemList;
    }

    private BathDeductStockDTO buildBathDeductStockDTO(BathCreateOrderDTO bathCreateOrderDTO) {


        String mainOrderNumber = bathCreateOrderDTO.getMainOrderNumber();

        List<DeductStockDTO> deductStockDTOList = new ArrayList<>();
        bathCreateOrderDTO.getCreateOrderDTOList().forEach(a -> {

            Long orderId = a.getOrderId();
            a.getOrderItems().forEach(b -> {
                //表示 在一个批次提交里 唯一的key 可能多个商品下相同的skuCode 所以加上GoodsId
                String bizKeyPrx = mainOrderNumber + "_" + b.getSkuCode() + "_" + b.getGoodsId();
                DeductStockDTO deductStockDTO = new DeductStockDTO();
                deductStockDTO.setBizKeyPrx(bizKeyPrx);
                deductStockDTO.setOrderId(orderId);
                deductStockDTO.setSkuCode(b.getSkuCode());
                deductStockDTO.setQuantity(b.getQuantity());
                deductStockDTO.setGoodsId(b.getGoodsId());
                deductStockDTO.setWarehouseId(b.getWarehouseId());
                //订单编号使用主编号 防止重复提交 因为 订单编号是随机生成的
                deductStockDTO.setOrderNumber(mainOrderNumber);
                deductStockDTOList.add(deductStockDTO);
            });
        });
        BathDeductStockDTO bathDeductStockDTO = new BathDeductStockDTO();
        bathDeductStockDTO.setDeductStockDTOList(deductStockDTOList);
        return bathDeductStockDTO;

    }

    @Override
    public void confirmSubmitCart(StartSubmitCartBO dto) {

        BathCreateOrderDTO orderDTO = dto.getBathCreateOrderDTO();
        List<CreateOrderDTO> createOrderDTOList = orderDTO.getCreateOrderDTOList();
        createOrderDTOList.forEach(a -> {
            sendOrderCreateSuccessMessage(a.getOrderId(),
                    a.getUserId());

        });


    }

    @Override
    public void cancelSubmitCart(StartSubmitCartBO dto) {

        BathCreateOrderDTO orderDTO = dto.getBathCreateOrderDTO();

        Long mainOrderId = orderDTO.getMainOrderId();
        Long userId = orderDTO.getUserId();
        List<Long> orderIdList = new ArrayList<>();
        orderDTO.getCreateOrderDTOList().forEach(a -> {
            orderIdList.add(a.getOrderId());
        });
        mainOrderService.lambdaUpdate()
                .eq(MainOrderDO::getId, mainOrderId)
                .eq(MainOrderDO::getUserId, userId)
                .remove();
        orderService.lambdaUpdate()
                .in(OrderDO::getId, orderIdList)
                .eq(OrderDO::getUserId, userId)
                .remove();
        orderStatusTraceService.lambdaUpdate()
                .in(OrderStatusTraceDO::getOrderId, orderIdList)
                .eq(OrderStatusTraceDO::getUserId, userId)
                .remove();

    }

    /**
     * 计算订单金额
     *
     * @param cartItemList 购物车商品项列表
     * @return 订单金额
     */
    private BigDecimal calculateOrderAmount(List<GoodsItem> cartItemList) {
        return cartItemList.stream()
                .map(item -> BigDecimalUtil.multiply(item.getPrice(), new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimalUtil::add);
    }


    private StartSubmitCartBO buildSubmitCartOrderInitParamsBO(SubmitCartDTO dto) {

        List<AddressListVO> listVOList = RpcResultParser.parseResult(userCacheService.addressListQuery());
        AddressListVO addressListVO = listVOList.stream()
                .filter(a -> a.getId().equals(dto.getAddressId()))
                .findFirst().orElse(null);
        if (addressListVO == null) {
            log.warn("收货地址不存在");
            throw new BizException("收货地址不存在");
        }
        List<CartInfoDTO> cartInfoList = dto.getCartInfoList();
        List<Long> cartIdList =
                cartInfoList.stream().map(CartInfoDTO::getCartId).collect(Collectors.toList());
        ClearCartDTO clearCartDTO = new ClearCartDTO();
        clearCartDTO.setCartIds(cartIdList);
        clearCartDTO.setUserId(UserContext.getUserId());

        ClearCartVO clearCartVO = RpcResultParser.parseResult(goodsApiService.queryCartGoodsInfo(clearCartDTO));
        /**
         * 添加一些字段
         */
        addField(clearCartVO.getGoodsVOList());

        Map<Long, Long> warehouseIdMap = dto.getCartInfoList().stream()
                .collect(Collectors.toMap(CartInfoDTO::getCartId, CartInfoDTO::getWarehouseId));

        StartSubmitCartBO startSubmitCartDTO = new StartSubmitCartBO();
        BuildBathCreateOrderBO initParamsBO = new BuildBathCreateOrderBO();
        initParamsBO.setMainOrderId(IdUtils.generateId());
        initParamsBO.setUserId(UserContext.getUserId());
        initParamsBO.setAddressListVO(addressListVO);
        initParamsBO.setWarehouseIdMap(warehouseIdMap);
        BathCreateOrderDTO bathCreateOrderDTO1 =
                buildBathCreateOrderDTO(initParamsBO, dto, clearCartVO);

        startSubmitCartDTO.setBathCreateOrderDTO(bathCreateOrderDTO1);
        startSubmitCartDTO.setClearCartDTO(clearCartDTO);

        return startSubmitCartDTO;
    }


    private void addField(List<ShopGoods> goodsVOList) {
        //添加订单id 这样订单id与交易单的订单id一一关联起来
        goodsVOList.forEach(shopGoodsBO -> {
            shopGoodsBO.setOrderId(IdUtils.generateId());
        });

    }


    /**
     * 构建批量创建订单的 DTO
     *
     * @param submitCartOrderInitParamsBO 购物车订单初始化参数
     * @param dto                         提交购物车请求参数
     * @param clearCartVO                 清空购物车返回结果
     * @return 批量创建订单的 DTO
     */
    private BathCreateOrderDTO buildBathCreateOrderDTO(BuildBathCreateOrderBO submitCartOrderInitParamsBO,
                                                       SubmitCartDTO dto,
                                                       ClearCartVO clearCartVO) {
        List<ShopGoods> goodsVOList = clearCartVO.getGoodsVOList();
        List<CreateOrderDTO> createOrderDTOList = new ArrayList<>(goodsVOList.size());

        for (ShopGoods shopGoodsBO : goodsVOList) {
            CreateOrderDTO createOrderDTO = buildCreateOrderDTO(shopGoodsBO, submitCartOrderInitParamsBO);
            createOrderDTOList.add(createOrderDTO);
        }

        BathCreateOrderDTO bathCreateOrderDTO = new BathCreateOrderDTO();
        bathCreateOrderDTO.setMainOrderId(submitCartOrderInitParamsBO.getMainOrderId());
        bathCreateOrderDTO.setMainOrderNumber(dto.getMainOrderNumber());
        bathCreateOrderDTO.setUserId(submitCartOrderInitParamsBO.getUserId());
        bathCreateOrderDTO.setTotalAmount(clearCartVO.getTotalPrice());
        bathCreateOrderDTO.setCreateOrderDTOList(createOrderDTOList);

        return bathCreateOrderDTO;
    }

    /**
     * 构建单个订单的 DTO
     *
     * @param shopGoodsBO 店铺商品信息
     * @return 创建订单的 DTO
     */
    private CreateOrderDTO buildCreateOrderDTO(ShopGoods shopGoodsBO,
                                               BuildBathCreateOrderBO initParamsBO) {
        BigDecimal orderAmount = calculateOrderAmount(shopGoodsBO.getCartItemList());
        List<GoodsItem> cartItemList = shopGoodsBO.getCartItemList();
        List<OrderItemDTO> orderItems = new ArrayList<>(cartItemList.size());
        Map<Long, Long> warehouseIdMap = initParamsBO.getWarehouseIdMap();
        for (GoodsItem cartItem : cartItemList) {
            OrderItemDTO orderItemDTO = buildOrderItemDTO(shopGoodsBO.getOrderId(), cartItem);
            orderItemDTO.setWarehouseId(warehouseIdMap.get(cartItem.getCartId()));
            orderItemDTO.setUserId(initParamsBO.getUserId());
            orderItems.add(orderItemDTO);
        }
        OrderItemDTO orderItemDTO = orderItems.get(0);
        CreateOrderDTO createOrderDTO = new CreateOrderDTO();
        createOrderDTO.setOrderId(shopGoodsBO.getOrderId());
        createOrderDTO.setShopId(shopGoodsBO.getShopId());
        createOrderDTO.setUserId(initParamsBO.getUserId());
        createOrderDTO.setOrderNumber(OrderServiceUtils.generateOrderNumber());
        createOrderDTO.setTotalMoney(orderAmount);
        createOrderDTO.setActualPayMoney(orderAmount);
        createOrderDTO.setDiscountAmount(BigDecimal.ZERO);
        createOrderDTO.setOrderItems(orderItems);
        createOrderDTO.setShopName(shopGoodsBO.getShopName());
        createOrderDTO.setAddressListVO(initParamsBO.getAddressListVO());
        createOrderDTO.setTenantId(orderItemDTO.getTenantId());
        return createOrderDTO;
    }

    /**
     * 构建订单项 DTO
     *
     * @param orderId  订单 ID
     * @param cartItem 购物车商品项
     * @return 订单项 DTO
     */
    private OrderItemDTO buildOrderItemDTO(Long orderId, GoodsItem cartItem) {
        OrderItemDTO orderItemDTO = new OrderItemDTO();
        orderItemDTO.setOrderId(orderId);
        orderItemDTO.setGoodsId(cartItem.getGoodsId());
        orderItemDTO.setGoodsName(cartItem.getGoodsName());
        orderItemDTO.setGoodsTitle(cartItem.getGoodsTitle());
        orderItemDTO.setSkuId(cartItem.getSkuId());
        orderItemDTO.setSkuName(cartItem.getSkuName());
        orderItemDTO.setSkuPictureAddress(cartItem.getSkuPictureAddress());
        orderItemDTO.setQuantity(cartItem.getQuantity());
        orderItemDTO.setUnitPrice(cartItem.getPrice());
        orderItemDTO.setGoodsVersion(cartItem.getGoodsVersion());
        orderItemDTO.setSkuVersion(cartItem.getSkuVersion());
        orderItemDTO.setSkuCode(cartItem.getSkuCode());
        orderItemDTO.setTenantId(cartItem.getTenantId());
        return orderItemDTO;
    }


    @DistributedLock(key = "#dto.orderId")
    @Transactional
    @Override
    public void cancelOrder(CancelOrderDTO dto) {

        log.info("取消订单:{}", dto);
        Long orderId = dto.getOrderId();

        OrderDO orderDO = orderService.lambdaQuery()
                .eq(OrderDO::getId, orderId)
                .eq(OrderDO::getUserId, dto.getUserId())
                .one();

        if (orderDO == null) {
            log.error("订单不存在orderId:[{}]", dto.getOrderId());
            throw new BizException("订单不存在");
        }
        if (!OrderStatusEnum.isCancelable(orderDO.getStatus().getCode())) {
            log.warn("当前订单状态不允许取消:{}", orderDO.getStatus());
            throw new BizException("订单状态异常");
        }
        CancelOrderEventMessage orderEventMessage = buildCancelOrderEventMessage(orderDO);

        //
        Date date = new Date();
        OrderStatusTraceDO orderStatusTraceDO = new OrderStatusTraceDO();
        orderStatusTraceDO.setOrderId(orderId);
        orderStatusTraceDO.setFromStatus(orderDO.getStatus());
        orderStatusTraceDO.setToStatus(OrderStatusEnum.CANCELLED);
        orderStatusTraceDO.setCreateDate(DateUtils.format(date, DateUtils.DATE));
        orderStatusTraceDO.setTenantId(orderDO.getTenantId());
        orderStatusTraceDO.setRemark(dto.getRemark());
        orderStatusTraceDO.setUserId(dto.getUserId());

        boolean update = orderService.lambdaUpdate()
                .eq(OrderDO::getId, orderDO.getId())
                .eq(OrderDO::getUserId, dto.getUserId())
                .eq(OrderDO::getStatus, orderDO.getStatus())
                .eq(OrderDO::getVersion, orderDO.getVersion())
                .set(OrderDO::getStatus, OrderStatusEnum.CANCELLED)
                .set(OrderDO::getVersion, orderDO.getVersion() + 1)
                .update();
        if (!update) {
            log.warn("订单状态更新异常");
            throw new MessageRetryConsumeException("订单状态更新异常");
        }
        orderStatusTraceService.save(orderStatusTraceDO);
        /**
         * 发送取消订单事件
         */

        rocketMqClient.sendOrderlyMessageWithTags(OrderTopicWithTag.ORDER_EVENT_TOPIC,
                OrderStatusEnum.CANCELLED.getTag(), JsonUtils.toJsonString(orderEventMessage),
                orderDO.getId().toString());

        log.info("取消订单成功");
    }


    private CancelOrderEventMessage buildCancelOrderEventMessage(OrderDO orderDO) {


        List<OrderItemDO> list = orderItemService.lambdaQuery()
                .eq(OrderItemDO::getOrderId, orderDO.getId())
                .eq(OrderItemDO::getUserId, orderDO.getUserId())
                .list();
        if (list.isEmpty()) {
            log.error("订单商品项目不存在");
            throw new BizException("订单商品项目不存在");
        }
        List<OrderGoodsInfo> orderGoodsInfoList = getOrderGoodsInfos(list);
        CancelOrderEventMessage cancelOrderEventMessage = new CancelOrderEventMessage();
        cancelOrderEventMessage.setOrderId(orderDO.getId());
        cancelOrderEventMessage.setOrderNumber(orderDO.getOrderNumber());
        cancelOrderEventMessage.setOrderGoodsInfoList(orderGoodsInfoList);
        return cancelOrderEventMessage;
    }

    private static List<OrderGoodsInfo> getOrderGoodsInfos(List<OrderItemDO> list) {
        List<OrderGoodsInfo> orderGoodsInfoList = new ArrayList<>(list.size());
        for (OrderItemDO orderItemDO : list) {
            OrderGoodsInfo orderGoodsInfo = new OrderGoodsInfo();
            orderGoodsInfo.setGoodsId(orderItemDO.getGoodsId());
            orderGoodsInfo.setSkuCode(orderItemDO.getSkuCode());
            orderGoodsInfo.setWarehouseId(orderItemDO.getWarehouseId());
            orderGoodsInfo.setTenantId(orderItemDO.getTenantId());
            orderGoodsInfoList.add(orderGoodsInfo);
        }
        return orderGoodsInfoList;
    }

    @Transactional
    @HmilyTCC(confirmMethod = "confirmCreateSecKillOrder", cancelMethod = "cancelCreateSecKillOrder")
    @Override
    public void createSecKillOrder(SecKillPlaneMessage message) {

        log.info("创建秒杀订单");
        BigDecimal totalMoney = BigDecimalUtils.multiply(message.getUnitPrice(), BigDecimal.valueOf(message.getQuantity()));
        Long orderId = message.getOrderId();
        OrderDO orderDO = getOrderDO(message, orderId, totalMoney);
        //
        OrderItemDO orderItemDO = new OrderItemDO();
        orderItemDO.setOrderId(orderId);
        orderItemDO.setGoodsId(message.getGoodsId());
        orderItemDO.setGoodsName(message.getGoodsName());
        orderItemDO.setGoodsTitle(message.getGoodsTitle());
        orderItemDO.setSkuId(message.getSkuId());
        orderItemDO.setSkuCode(message.getSkuCode());
        orderItemDO.setSkuName(message.getSkuName());
        orderItemDO.setSkuPictureAddress(message.getSkuPictureAddress());
        orderItemDO.setQuantity(message.getQuantity());
        orderItemDO.setUnitPrice(message.getUnitPrice());
        orderItemDO.setGoodsVersion(message.getGoodsVersion());
        orderItemDO.setSkuVersion(message.getSkuVersion());
        orderItemDO.setWarehouseId(message.getWarehouseId());
        orderItemDO.setUserId(message.getUserId());
        orderItemDO.setTenantId(message.getTenantId());
        //

        SecKillPlaneCreateOrderSuccessMessage successMessage = new SecKillPlaneCreateOrderSuccessMessage();
        successMessage.setOrderId(orderId);
        successMessage.setOrderNumber(message.getOrderNumber());
        successMessage.setUserId(message.getUserId());
        successMessage.setTradeMoney(totalMoney);
        successMessage.setSkuCode(message.getSkuCode());
        successMessage.setWarehouseId(message.getWarehouseId());
        successMessage.setQuantity(message.getQuantity());
        //
        Date date = new Date();
        OrderStatusTraceDO orderStatusTraceDO = new OrderStatusTraceDO();
        orderStatusTraceDO.setOrderId(orderId);
        orderStatusTraceDO.setFromStatus(null);
        orderStatusTraceDO.setToStatus(OrderStatusEnum.WAIT_PAY);
        orderStatusTraceDO.setCreateDate(DateUtils.format(date, DateUtils.DATE));
        orderStatusTraceDO.setTenantId(message.getTenantId());
        orderStatusTraceDO.setUserId(message.getUserId());

        //创建交易单
        CreateTradeOrderDTO dto = new CreateTradeOrderDTO();
        dto.setUserId(message.getUserId());
        dto.setOrderId(message.getOrderId());
        dto.setTradeMoney(totalMoney);
        dto.setOrderNumber(message.getOrderNumber());
        RpcResultParser.parseResult(payApiService.createPayOrder(dto));

        try {
            orderService.save(orderDO);
        } catch (DuplicateKeyException e) {
            log.warn("订单已创建");
            return;
        }
        orderItemService.save(orderItemDO);
        orderStatusTraceService.save(orderStatusTraceDO);


    }

    private  OrderDO getOrderDO(SecKillPlaneMessage message, Long orderId, BigDecimal totalMoney) {

        AddressListVO defaultAddress = userCacheService.getDefaultAddress();
        log.info("用户默认收货地址:{}", defaultAddress);
        Date expireTime = DateUtils.addMinutes(new Date(), expireInterval);

        OrderDO orderDO = new OrderDO();
        orderDO.setId(orderId);
        orderDO.setShopId(message.getShopId());
        orderDO.setShopName(message.getShopName());
        orderDO.setTenantId(message.getTenantId());
        orderDO.setUserId(message.getUserId());
        orderDO.setOrderNumber(message.getOrderNumber());
        orderDO.setTotalMoney(totalMoney);
        orderDO.setActualPayMoney(totalMoney);
        orderDO.setStatus(OrderStatusEnum.WAIT_PAY);
        orderDO.setOrderType(OrderTypeEnum.SEC_KILL);
        orderDO.setAfterSaleDays(0);
        orderDO.setDiscountAmount(new BigDecimal(0));
        orderDO.setTakeAddress(JsonUtils.toJsonString(defaultAddress));
        orderDO.setExpireInterval( expireInterval.intValue());
        orderDO.setExpireTime(expireTime);
        return orderDO;
    }

    public void confirmCreateSecKillOrder(SecKillPlaneMessage message) {
        OrderCreateSuccessMessage message2 = new OrderCreateSuccessMessage();
        message2.setOrderId(message.getOrderId());
        message2.setUserId(message.getUserId());
        rocketMqClient.sendOrderlyMessageWithTags(OrderTopicWithTag.ORDER_EVENT_TOPIC,
                OrderStatusEnum.WAIT_PAY.getTag(), JsonUtils.toJsonString(message2),
                message.getOrderId().toString());
        //发送物流跟踪信息
        BathAddShippingTrackMessage bathMessage = new BathAddShippingTrackMessage();
        bathMessage.setOrderId(message.getOrderId());
        bathMessage.setTenantId(message.getTenantId());
        bathMessage.setUserId(message.getUserId());
        List<ShippingTrackMessage> shippingTrackList = new ArrayList<>();
        ShippingTrackMessage trackMessage = new ShippingTrackMessage();
        trackMessage.setStatus(ShippingStatusEnum.ORDER_PLACED);
        trackMessage.setFinishTime(new Date());
        trackMessage.setFinishContent("订单已提交");
        trackMessage.setFlowNo(IStringUtils.hashToUniqueString(message.getOrderId() + trackMessage.getFinishContent()));
        shippingTrackList.add(trackMessage);
        bathMessage.setShippingTrackList(shippingTrackList);
        rocketMqClient.sendMessage(OrderMqTopicName.BATH_ADD_SHIPPING_TRACK_TOPIC, JsonUtils.toJsonString(bathMessage));
        secKillResultCache.addResult(message.getUserId(), message.getSecKillItemId(), SecKillResultEnum.SUCCESS_ORDER_CREATED);
    }


    public void cancelCreateSecKillOrder(SecKillPlaneMessage message) {

        Long userId = message.getUserId();
        Long orderId = message.getOrderId();

        orderService.lambdaUpdate()
                .eq(OrderDO::getUserId, userId)
                .eq(OrderDO::getId, orderId)
                .remove();
        orderItemService.lambdaUpdate()
                .eq(OrderItemDO::getUserId, userId)
                .eq(OrderItemDO::getOrderId, orderId)
                .remove();
        orderStatusTraceService.lambdaUpdate()
                .eq(OrderStatusTraceDO::getOrderId, userId)
                .eq(OrderStatusTraceDO::getUserId, orderId)
                .remove();

    }
}
