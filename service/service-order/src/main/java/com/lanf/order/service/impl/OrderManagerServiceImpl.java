package com.lanf.order.service.impl;


import com.lanf.common.utils.BigDecimalUtil;
import com.lanf.common.utils.IStringUtils;
import com.lanf.common.utils.IdUtils;
import com.lanf.constant.exception.BizException;
import com.lanf.constant.result.Result;
import com.lanf.constant.result.RpcResultParser;
import com.lanf.goods.api.GoodsApiService;
import com.lanf.goods.model.bo.GoodsSkuBO;
import com.lanf.goods.model.dto.CalculateOrderTotalAmountDTO;
import com.lanf.goods.model.dto.DeductStockDTO;
import com.lanf.goods.model.dto.ValidateCartDTO;
import com.lanf.goods.model.vo.CalculateOrderTotalAmountVO;
import com.lanf.goods.model.vo.DeductStockVO;
import com.lanf.goods.model.vo.ValidateCartItemVO;
import com.lanf.lock.aop.DistributedLock;
import com.lanf.order.api.OrderApiService;
import com.lanf.order.model.bo.OrderInitParamsBO;
import com.lanf.order.model.dto.*;
import com.lanf.order.model.vo.CalculateOrderAmountVO;
import com.lanf.order.model.vo.PlaceOrderVO;
import com.lanf.order.model.vo.ValidateCartVO;
import com.lanf.order.service.OrderManagerService;
import com.lanf.order.utils.OrderServiceUtils;
import com.lanf.pay.api.PayApiService;
import com.lanf.pay.model.dto.CreateTradeOrderDTO;
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
        validateCartVO.setTotalPrice(actualPrice);
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

        List<DiscountInfoBO> discountInfoBOS = amountVO != null ? amountVO.getDiscountInfoBOList() : null;
        OrderItemDTO orderItem = createOrderItem(orderInitParamsBO, orderDTO, deductStockVO);
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
        createOrderDTO.setOrderItem(orderItem);
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
        dto.setTradeOrderId(orderInitParamsBO.getOrderId());
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
        return orderInitParamsBO;
    }
























}
