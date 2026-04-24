package com.lanf.order.service.impl;


import com.lanf.cache.aop.DistributedLock;
import com.lanf.common.utils.*;
import com.lanf.constant.exception.BizException;
import com.lanf.constant.result.Result;
import com.lanf.constant.result.RpcResultParser;
import com.lanf.goods.api.GoodsApiService;
import com.lanf.goods.model.bo.GoodsItemBO;
import com.lanf.goods.model.bo.GoodsSkuBO;
import com.lanf.goods.model.bo.ShopGoodsBO;
import com.lanf.goods.model.dto.CalculateOrderTotalAmountDTO;
import com.lanf.goods.model.dto.ClearCartDTO;
import com.lanf.goods.model.dto.DeductStockDTO;
import com.lanf.goods.model.dto.ValidateCartDTO;
import com.lanf.goods.model.vo.CalculateOrderTotalAmountVO;
import com.lanf.goods.model.vo.ClearCartVO;
import com.lanf.goods.model.vo.DeductStockVO;
import com.lanf.goods.model.vo.ValidateCartItemVO;
import com.lanf.order.api.OrderApiService;
import com.lanf.order.model.bo.CancelOrderBO;
import com.lanf.order.model.bo.OrderInitParamsBO;
import com.lanf.order.model.bo.SubmitCartOrderInitParamsBO;
import com.lanf.order.model.dto.*;
import com.lanf.order.model.vo.CalculateOrderAmountVO;
import com.lanf.order.model.vo.PlaceOrderVO;
import com.lanf.order.model.vo.SubmitCartVO;
import com.lanf.order.model.vo.ValidateCartVO;
import com.lanf.order.service.IOrderService;
import com.lanf.order.service.OrderManagerService;
import com.lanf.order.utils.OrderServiceUtils;
import com.lanf.pay.api.PayApiService;
import com.lanf.pay.model.dto.CancelTradeOrderDTO;
import com.lanf.pay.model.dto.CreateMergeTradeOrderDTO;
import com.lanf.pay.model.dto.CreateMergeTradeOrderItemDTO;
import com.lanf.pay.model.dto.CreateTradeOrderDTO;
import com.lanf.pay.model.vo.CancelTradeOrderVO;
import com.lanf.rocketmq.model.TopicName;
import com.lanf.rocketmq.model.message.CancelOrderEventMessage;
import com.lanf.rocketmq.model.message.OrderCreateSuccessMessage;
import com.lanf.rocketmq.util.RocketMqClient;
import com.lanf.security.utils.UserIdContext;
import com.lanf.welfare.api.WelfareApiService;
import com.lanf.welfare.model.bo.DiscountInfoBO;
import com.lanf.welfare.model.dto.CalculateDiscountAmountDTO;
import com.lanf.welfare.model.dto.UseMultipleCouponDTO;
import com.lanf.welfare.model.vo.CalculateDiscountAmountVO;
import lombok.extern.slf4j.Slf4j;
import org.dromara.hmily.annotation.HmilyTCC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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
    private OrderApiService orderApiService;
    @Autowired
    private RocketMqClient rocketMqClient;
    @Autowired
    private IOrderService orderService;

    @Override
    public CalculateOrderAmountVO calculateOrderAmount(CalculateOrderAmountDTO dto) {



        CalculateOrderTotalAmountVO amountVO =  calculateOrderTotalAmount(dto);
        BigDecimal totalAmount = amountVO.getTotalAmount();
        //计算优惠金额
        CalculateDiscountAmountVO calculateDiscountAmountVO = calculateDiscountAmount(dto,
                UserIdContext.getUserId(), totalAmount);
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

        Result<CalculateDiscountAmountVO> calculateDiscountAmountVOResult = welfareApiService.calculateDiscountAmount(discountAmountDTO);

        return RpcResultParser.parseResult(calculateDiscountAmountVOResult);
    }
    /**
     *立即下单
     *
     *
     */
    @HmilyTCC(confirmMethod = "confirmPlaceOrder", cancelMethod = "cancelPlaceOrder")
    @DistributedLock(key = "#orderDTO.orderNumber")
    @Override
    public PlaceOrderVO placeOrder(PlaceOrderDTO orderDTO) {

        OrderInitParamsBO orderInitParamsBO = initParams( orderDTO.getOrderNumber());
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
         * 发送MQ 订单创建成功消息
         */
        sendOrderCreateSuccessMessage(orderInitParamsBO.getOrderId());
        
        /**
         * 构建返回结果
         */
        PlaceOrderVO vo = new PlaceOrderVO();
        vo.setOrderId(orderInitParamsBO.getOrderId());

        return vo;
    }

    @Override
    public ValidateCartVO validateCart(ValidateCartDTO dto) {


        /**
         * 校验库存
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



    /**
     * 空方法 让tcc框架调用
     *
     */
    public  void  confirmPlaceOrder(PlaceOrderDTO orderDTO){

    }
    /**
     * 空方法 让tcc框架调用
     *
     */
    public  void  cancelPlaceOrder(PlaceOrderDTO orderDTO){

    }


    /**
     * 创建订单项
     * @param orderInitParamsBO 订单初始化参数
     * @param orderDTO 下单请求参数
     * @param deductStockVO 扣减库存结果
     * @return 订单项对象
     */
    private OrderItemDTO createOrderItem(OrderInitParamsBO orderInitParamsBO, PlaceOrderDTO orderDTO, DeductStockVO deductStockVO) {

        GoodsSkuBO goodsSkuBO = deductStockVO.getGoodsSkuBO();
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
        return orderItem;
    }
    /**
     * 创建订单
     * @param orderDTO 下单请求参数
     * @param orderInitParamsBO 订单初始化参数
     * @param deductStockVO 扣减库存结果
     * @param tradeMoney 实际支付金额
     * @param amountVO 优惠券计算结果
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
        createOrderDTO.setUserId(orderInitParamsBO.getUserId());
        createOrderDTO.setOrderNumber(orderDTO.getOrderNumber());
        createOrderDTO.setTotalMoney(deductStockVO.getTotalAmount());
        createOrderDTO.setActualPayMoney(tradeMoney);
        createOrderDTO.setDiscountAmount(discountAmount);
        createOrderDTO.setDiscountInfoBO(discountInfoBOS);
        createOrderDTO.setTakeAddressBO(orderDTO.getTakeAddress());
        createOrderDTO.setOrderItems(orderItems);
        RpcResultParser.parseResult(orderApiService.createOrder(createOrderDTO));

    }
    /**
     * 创建支付订单
     * @param orderInitParamsBO 订单初始化参数
     * @param orderDTO 下单请求参数
     * @param tradeMoney 交易金额
     */
    private void createPayOrder(OrderInitParamsBO orderInitParamsBO, PlaceOrderDTO orderDTO, BigDecimal tradeMoney) {
        CreateTradeOrderDTO dto = new CreateTradeOrderDTO();
        dto.setUserId(orderInitParamsBO.getUserId());
        dto.setOrderId(orderInitParamsBO.getOrderId());
        dto.setTradeMoney(tradeMoney);
        dto.setPayType(orderDTO.getPayType());
        dto.setOrderNumber(orderDTO.getOrderNumber());
        RpcResultParser.parseResult(payApiService.createPayOrder(dto));

    }

    /**
     * 从优惠券计算结果中获取折扣金额
     * @param amountVO 优惠券计算结果
     * @return 折扣金额
     */
    private BigDecimal getDiscountAmount(CalculateDiscountAmountVO amountVO) {
        return amountVO != null ? amountVO.getTotalDiscountAmount() : BigDecimal.ZERO;
    }
    /**
     * 计算订单实际支付金额
     * @param totalAmount 商品总金额
     * @param amountVO 优惠券计算结果，包含折扣金额
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
     * @param actualPayment 实际支付金额
     */
    private void validatePaymentAmount(BigDecimal actualPayment) {
        if (BigDecimalUtil.lt(actualPayment, BigDecimal.ZERO)) {
            throw new BizException("实际支付金额不能小于0");
        }
    }

    private void sendOrderCreateSuccessMessage(Long orderId) {
        OrderCreateSuccessMessage message = new OrderCreateSuccessMessage();
        message.setOrderId(orderId);
        rocketMqClient.sendMessage(TopicName.ORDER_CREATE_SUCCESS_TOPIC,  message);
    }



    private CalculateDiscountAmountVO useMultipleCoupon(PlaceOrderDTO orderDTO, OrderInitParamsBO orderInitParamsBO, BigDecimal totalAmount){
        if (IStringUtils.isEmpty(orderDTO.getCouponIds())){
            return null;
        }
        UseMultipleCouponDTO dto = new UseMultipleCouponDTO();
        dto.setOrderId(orderInitParamsBO.getOrderId());
        dto.setUserId(orderInitParamsBO.getUserId());
        dto.setShopId(orderDTO.getShopId());
        dto.setTotalAmount(totalAmount);
        dto.setCouponIds(orderDTO.getCouponIds());
        dto.setBizKeyPrx(orderInitParamsBO.getBizKeyPrx() );

        return RpcResultParser.parseResult(welfareApiService.useMultipleCoupon(dto));
    }
    private DeductStockVO  deductStock( PlaceOrderDTO orderDTO, OrderInitParamsBO orderInitParamsBO){


        DeductStockDTO deductStockDTO = new DeductStockDTO();
        deductStockDTO.setOrderId(orderInitParamsBO.getOrderId());
        deductStockDTO.setSkuCode(orderDTO.getSkuCode());
        deductStockDTO.setQuantity(orderDTO.getQuantity());
        deductStockDTO.setBizKeyPrx(orderInitParamsBO.getBizKeyPrx() );
        return RpcResultParser.parseResult(goodsApiService.deductStock(deductStockDTO));


    }

    private OrderInitParamsBO initParams(String orderNumber){

        OrderInitParamsBO  orderInitParamsBO = new OrderInitParamsBO();
        orderInitParamsBO.setTradeOrderId(IdUtils.generateId());
        orderInitParamsBO.setOrderId(IdUtils.generateId());
        orderInitParamsBO.setUserId(UserIdContext.getUserId());
        orderInitParamsBO.setBizKeyPrx(orderNumber);
        orderInitParamsBO.setOrderNumber(orderNumber);
        return orderInitParamsBO;
    }

    /**
     * 构建属性 特别小心 容易出bug
     * @param dto
     * @return
     */
    @DistributedLock(key = "#dto.mainOrderNumber")
    @Override
    public SubmitCartVO submitCart(SubmitCartDTO dto) {


        /**
         * 初始化一些参数
         */
        SubmitCartOrderInitParamsBO submitCartOrderInitParamsBO = buildSubmitCartOrderInitParamsBO();
        /**
         * 清空购物车
         */
        ClearCartDTO clearCartDTO = new ClearCartDTO();
        clearCartDTO.setBizKeyPrx(dto.getMainOrderNumber());
        ClearCartVO clearCartVO = RpcResultParser.parseResult(goodsApiService.clearCart(clearCartDTO));

        /**
         * 添加一些字段
         */
        addField( clearCartVO.getGoodsVOList());
        /**
         * 创建交易单 以ClearCartVO 信息进行构建
         *
         */
        CreateMergeTradeOrderDTO createMergeTradeOrderDTO =
                buildCreateMergeTradeOrderDTO( submitCartOrderInitParamsBO, dto,clearCartVO.getGoodsVOList()) ;
        RpcResultParser.parseResult( payApiService.createMergeTradeOrder(createMergeTradeOrderDTO));

        /**
         * 创建订单 创建交易单 以ClearCartVO 信息进行构建
         */
        BathCreateOrderDTO bathCreateOrderDTO1 = buildBathCreateOrderDTO(submitCartOrderInitParamsBO, dto, clearCartVO);
        RpcResultParser.parseResult(orderApiService.bathCreateOrder(bathCreateOrderDTO1));

        /**
         * 构建返回值
         */
        SubmitCartVO submitCartVO = new SubmitCartVO();
        submitCartVO.setMainOrderId(submitCartOrderInitParamsBO.getMainOrderId());
        return submitCartVO;
    }



    /**
     * 计算订单金额
     * @param cartItemList 购物车商品项列表
     * @return 订单金额
     */
    private BigDecimal calculateOrderAmount(List<GoodsItemBO> cartItemList) {
        return cartItemList.stream()
                .map(item -> BigDecimalUtil.multiply(item.getPrice(), new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimalUtil::add);
    }


    private SubmitCartOrderInitParamsBO buildSubmitCartOrderInitParamsBO(){
        SubmitCartOrderInitParamsBO submitCartOrderInitParamsBO = new SubmitCartOrderInitParamsBO();
        submitCartOrderInitParamsBO.setMainOrderId(IdUtils.generateId());
        submitCartOrderInitParamsBO.setUserId(UserIdContext.getUserId());

        return submitCartOrderInitParamsBO;
    }


    private void addField(List<ShopGoodsBO> goodsVOList){
        //添加订单id 这样订单id与交易单的订单id一一关联起来
        goodsVOList.forEach(shopGoodsBO -> {shopGoodsBO.setOrderId(IdUtils.generateId());});

    }


    private CreateMergeTradeOrderDTO buildCreateMergeTradeOrderDTO(SubmitCartOrderInitParamsBO submitCartOrderInitParamsBO,SubmitCartDTO dto, List<ShopGoodsBO> goodsVOList) {
        CreateMergeTradeOrderDTO createMergeTradeOrderDTO = new CreateMergeTradeOrderDTO();
        createMergeTradeOrderDTO.setMainOrderId(submitCartOrderInitParamsBO.getMainOrderId());
        createMergeTradeOrderDTO.setUserId(submitCartOrderInitParamsBO.getUserId());
        createMergeTradeOrderDTO.setPayType(dto.getPayType());
        createMergeTradeOrderDTO.setMainOrderNumber(dto.getMainOrderNumber());
        List<CreateMergeTradeOrderItemDTO> tradeOrderItemList = new ArrayList<>(goodsVOList.size());
        createMergeTradeOrderDTO.setTradeOrderItemList(tradeOrderItemList);

        for (ShopGoodsBO shopGoodsBO : goodsVOList) {
            List<GoodsItemBO> cartItemList = shopGoodsBO.getCartItemList();
            BigDecimal orderAmount = calculateOrderAmount(cartItemList);

            CreateMergeTradeOrderItemDTO tradeOrderItem = new CreateMergeTradeOrderItemDTO();
            tradeOrderItem.setOrderId(shopGoodsBO.getOrderId());
            tradeOrderItem.setTradeMoney(orderAmount);
            tradeOrderItemList.add(tradeOrderItem);
        }

        return createMergeTradeOrderDTO;
    }

    /**
     * 构建批量创建订单的 DTO
     * @param submitCartOrderInitParamsBO 购物车订单初始化参数
     * @param dto 提交购物车请求参数
     * @param clearCartVO 清空购物车返回结果
     * @return 批量创建订单的 DTO
     */
    private BathCreateOrderDTO buildBathCreateOrderDTO(SubmitCartOrderInitParamsBO submitCartOrderInitParamsBO,
                                                       SubmitCartDTO dto,
                                                       ClearCartVO clearCartVO) {
        List<ShopGoodsBO> goodsVOList = clearCartVO.getGoodsVOList();
        List<CreateOrderDTO> createOrderDTOList = new ArrayList<>(goodsVOList.size());

        for (ShopGoodsBO shopGoodsBO : goodsVOList) {
            CreateOrderDTO createOrderDTO = buildCreateOrderDTO(shopGoodsBO, submitCartOrderInitParamsBO, dto);
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
     * @param shopGoodsBO 店铺商品信息
     * @param submitCartOrderInitParamsBO 购物车订单初始化参数
     * @param dto 提交购物车请求参数
     * @return 创建订单的 DTO
     */
    private CreateOrderDTO buildCreateOrderDTO(ShopGoodsBO shopGoodsBO,
                                               SubmitCartOrderInitParamsBO submitCartOrderInitParamsBO,
                                               SubmitCartDTO dto) {
        BigDecimal orderAmount = calculateOrderAmount(shopGoodsBO.getCartItemList());
        List<GoodsItemBO> cartItemList = shopGoodsBO.getCartItemList();
        List<OrderItemDTO> orderItems = new ArrayList<>(cartItemList.size());

        for (GoodsItemBO cartItem : cartItemList) {
            OrderItemDTO orderItemDTO = buildOrderItemDTO(shopGoodsBO.getOrderId(), cartItem);
            orderItems.add(orderItemDTO);
        }

        CreateOrderDTO createOrderDTO = new CreateOrderDTO();
        createOrderDTO.setOrderId(shopGoodsBO.getOrderId());
        createOrderDTO.setShopId(shopGoodsBO.getShopId());
        createOrderDTO.setUserId(submitCartOrderInitParamsBO.getUserId());
        createOrderDTO.setOrderNumber(OrderServiceUtils.generateOrderNumber());
        createOrderDTO.setTotalMoney(orderAmount);
        createOrderDTO.setActualPayMoney(orderAmount);
        createOrderDTO.setDiscountAmount(BigDecimal.ZERO);
        createOrderDTO.setTakeAddressBO(dto.getTakeAddress());
        createOrderDTO.setOrderItems(orderItems);

        return createOrderDTO;
    }

    /**
     * 构建订单项 DTO
     * @param orderId 订单 ID
     * @param cartItem 购物车商品项
     * @return 订单项 DTO
     */
    private OrderItemDTO buildOrderItemDTO(Long orderId, GoodsItemBO cartItem) {
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
        return orderItemDTO;
    }


    @DistributedLock(key = "#dto.orderId")
    @Override
    public void cancelOrder(CancelOrderDTO dto) {

        String  bizKeySuffix = dto.getOrderId().toString();
        CancelOrderBO cancelOrderBO = new CancelOrderBO();
        cancelOrderBO.setOrderId(dto.getOrderId());
        cancelOrderBO.setBizKeySuffix(bizKeySuffix);
        cancelOrderBO.setCancelSource(dto.getCancelSource());
        /**
         * 通过被代理后的对象去执行
         */
        OrderManagerService bean = BeanUtil.getBean(OrderManagerService.class);
        bean.doCancelOrder( cancelOrderBO);
    }
    @HmilyTCC(confirmMethod = "confirmCancelOrder", cancelMethod = "cancelCancelOrder")
    @Override
    public void doCancelOrder(CancelOrderBO dto) {

        String bizKeySuffix = dto.getBizKeySuffix();
        /**
         * 取消交易单
         */
        cancelTradeOrder(dto, bizKeySuffix);
        /**
         * 取消订单
         */
        orderService.cancelOrder(dto);
        /**
         * 发送mq消息
         */
        //查询订单 关联的skuId
        List<Long> skuIdList = orderService.querySkuIdsByOrderId(dto.getOrderId());
        CancelOrderEventMessage cancelOrderEventMessage = new CancelOrderEventMessage();
        cancelOrderEventMessage.setOrderId(dto.getOrderId());
        cancelOrderEventMessage.setSkuIdList(skuIdList);
        cancelOrderEventMessage.setCancelSource(dto.getCancelSource());
        rocketMqClient.sendMessage(TopicName.CANCEL_ORDER_EVENT_TOPIC, JsonUtils.toJsonString(cancelOrderEventMessage));
    }

    private CancelTradeOrderVO  cancelTradeOrder(CancelOrderBO dto, String  bizKeySuffix){
        CancelTradeOrderDTO cancelTradeOrderDTO = new CancelTradeOrderDTO();
        cancelTradeOrderDTO.setBizKeySuffix(bizKeySuffix);
        cancelTradeOrderDTO.setOrderId(dto.getOrderId());
        return RpcResultParser.parseResult(payApiService.cancelTradeOrder(cancelTradeOrderDTO));

    }

    public void  confirmCancelOrder(CancelOrderBO dto){
        orderService.confirmCancelOrder(dto);
    }
    public void  cancelCancelOrder(CancelOrderBO dto){
        orderService.cancelCancelOrder(dto);
    }





}
