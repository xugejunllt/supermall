package com.lanf.order.service.layout.impl;

import com.lanf.common.utils.BeanCopyUtils;
import com.lanf.common.utils.BigDecimalUtils;
import com.lanf.common.utils.IdUtils;
import com.lanf.common.utils.IStringUtils;
import com.lanf.constant.enums.LogisticsTrackStatusEnum;
import com.lanf.goods.api.GoodsApiService;
import com.lanf.goods.model.dto.CheckAndQueryGoodsDTO;
import com.lanf.goods.model.vo.ApiGoodsSkuVO;
import com.lanf.goods.model.vo.EmptyCartGoodsSkuVO;
import com.lanf.goods.model.vo.EmptyCartVO;
import com.lanf.logistics.api.LogisticsApiService;
import com.lanf.logistics.model.dto.LogisticsAddDTO;
import com.lanf.messagemanager.client.annotation.SendMessage;
import com.lanf.messagemanager.client.service.ISendMqMessageService;
import com.lanf.order.model.bo.OnePlaceAnOrderBO;
import com.lanf.order.model.dto.*;
import com.lanf.order.model.entity.OrderDO;
import com.lanf.order.model.vo.CreateOrderVO;
import com.lanf.order.service.IMainOrderService;
import com.lanf.order.service.IOrderService;
import com.lanf.order.service.layout.InterfaceLayoutService;
import com.lanf.pay.api.PayApiService;
import com.lanf.pay.model.dto.CreatePayOrderDTO;
import com.lanf.pay.model.vo.CreatePayOrderVO;
import com.lanf.rocketmq.model.TopicName;
import com.lanf.rocketmq.model.message.LogisticsTrackBathAddDTO;
import com.lanf.rocketmq.model.message.PrePayMsg;
import com.lanf.rocketmq.util.MessageBuildAdapter;
import com.lanf.rocketmq.util.RocketMqClient;
import com.lanf.security.utils.UserUtil;
import com.lanf.web.exception.BizException;
import com.lanf.web.result.Result;
import com.lanf.welfare.api.WelfareApiService;
import com.lanf.welfare.model.vo.CouponVO;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class InterfaceLayoutServiceImpl implements InterfaceLayoutService {

    @Autowired
    private GoodsApiService goodsApiService;
    @Autowired
    private PayApiService payApiService;
    @Autowired
    private IOrderService orderService;
    @Autowired
    private IMainOrderService mainOrderService;
    @Autowired
    private LogisticsApiService logisticsApiService;
    @Autowired
    private RocketMqClient rocketMqClient;
    @Autowired
    private WelfareApiService welfareApiService;
    @Autowired
    private ISendMqMessageService sendMqMessageService;
    @GlobalTransactional
    @SendMessage
    @Override
    public CreateOrderVO submitOrderDTO(SubmitOrderDTO dto) {

        Set<Long> cartIdSet = dto.getSubmitOrderItemDTOList().stream().map(SubmitOrderItemDTO::getCartId).
                collect(Collectors.toSet());
        //远程清空购物车接口
        EmptyCartVO emptyCartVO = goodsApiService.emptyCart(cartIdSet).getData();
        if (emptyCartVO == null) {
            throw new BizException("购物车清空异常");
        }
        List<EmptyCartGoodsSkuVO> goodsSkuVOList = emptyCartVO.getGoodsSkuVOList();
        //key,店铺id
        Map<Long, List<EmptyCartGoodsSkuVO>> goodsSkuMap = buildEmptyCartGoodsSkuVO(goodsSkuVOList);
        //key：店铺id value:商家id
        Map<Long, Long> businessIdMap = new HashMap<>();
        goodsSkuVOList.forEach(a -> {

            businessIdMap.put(a.getShopId(), a.getBusinessId());

        });
        Set<Long> shopIdSet = goodsSkuMap.keySet();
        //预先生成订单id
        Map<Long, Long> bizOrderIdMap = new HashMap<>();
        shopIdSet.forEach(a -> {
            bizOrderIdMap.put(a, IdUtils.generateId());
        });
        //主订单id
        Long mainOrderId = IdUtils.generateId();
        /**
         * 创建支付订单
         */
        List<CreatePayOrderDTO> createPayOrderDTOList = buildCreatePayOrderDTOList(shopIdSet, businessIdMap,
                bizOrderIdMap, goodsSkuMap, mainOrderId, dto.getCouponIdSet());
        //远程调用创建支付订单接口
        CreatePayOrderVO createPayOrderVO = payApiService.createPayOrder(createPayOrderDTOList).getData();
        if (createPayOrderVO == null) {

            throw new BizException("创建支付订单异常");
        }
        /**
         * 创建订单
         */
        CreateOrderDTO createOrderDTO = buildCreateOrderDTO(dto, mainOrderId, goodsSkuMap, shopIdSet, bizOrderIdMap,
                businessIdMap);
        //调用本地接口创建订单
        CreateOrderVO createOrderVO = mainOrderService.createOrder(createOrderDTO);
        /**
         * 发送物流信息
         */
        sendLogisticsMessage(createOrderVO.getOrderIdList());
        sendCloseOrderMessage(createOrderVO.getOrderIdList());
        return createOrderVO;

    }

    /**
     * 发送mq给物流服务
     */
    private void sendLogisticsMessage(List<Long> orderIdList) {
        String finishContent = "下单成功";
        LogisticsTrackBathAddDTO message = MessageBuildAdapter.buildLogisticsTrackAddDTO(orderIdList,
                finishContent, LogisticsTrackStatusEnum.PLACE_AN_ORDER_BUS_INCOME.getCode());
        message.setBizKeyValue(IStringUtils.generateKey(orderIdList,finishContent));
        sendMqMessageService.sendMessage(TopicName.BATH_ADD_LOGISTICS_TRACK_TOPIC, message);


    }
    private void sendCloseOrderMessage(List<Long> orderIdList) {

        orderIdList.forEach(a -> {
            String key = a.toString();
            PrePayMsg closeOrderPrePayMsg = new PrePayMsg();
            closeOrderPrePayMsg.setBizKeyValue(key);
            closeOrderPrePayMsg.setOrderId(a);
            closeOrderPrePayMsg.setDelayTime(getDelayLevel());
            sendMqMessageService.sendMessage(TopicName.PRE_PAY_CLOSE_ORDER_TOPIC, closeOrderPrePayMsg);

        });

    }
    private int getDelayLevel(){

        return 14;
    }
    private Map<Long, List<EmptyCartGoodsSkuVO>> buildEmptyCartGoodsSkuVO(List<EmptyCartGoodsSkuVO> goodsSkuVOList) {
        Map<Long, List<EmptyCartGoodsSkuVO>> goodsSkuMap = new HashMap<>();

        goodsSkuVOList.forEach(a -> {

            List<EmptyCartGoodsSkuVO> goodsSkuVOS = goodsSkuMap.get(a.getShopId());
            if (goodsSkuVOS == null) {

                goodsSkuVOS = new ArrayList<>();
                goodsSkuMap.put(a.getShopId(), goodsSkuVOS);
            }
            goodsSkuVOS.add(a);

        });
        return goodsSkuMap;
    }

    private CreateOrderDTO buildCreateOrderDTO(SubmitOrderDTO dto, Long mainOrderId, Map<Long, List<EmptyCartGoodsSkuVO>>
            goodsSkuMap, Set<Long> shopIdSet, Map<Long, Long> bizOrderIdMap, Map<Long, Long> businessIdMap) {

        CreateOrderDTO createOrderDTO = new CreateOrderDTO();
        createOrderDTO.setOrderNumber(dto.getOrderNumber());
        createOrderDTO.setMainOrderId(mainOrderId);
        List<OrderDTO> orderDTOList = new ArrayList<>();
        createOrderDTO.setOrderDTOList(orderDTOList);
        shopIdSet.forEach(a -> {
            OrderDTO orderDTO = new OrderDTO();
            orderDTOList.add(orderDTO);
            orderDTO.setId(bizOrderIdMap.get(a));
            orderDTO.setShopId(a);
            orderDTO.setBusinessId(businessIdMap.get(a));
            orderDTO.setUserId(UserUtil.getUserId());
            //待远程调用查询
            orderDTO.setTakeAddress(dto.getTakeAddress());
            //
            List<EmptyCartGoodsSkuVO> goodsSkuVOS = goodsSkuMap.get(a);

            List<OrderItemDTO> orderItemDTOS = BeanCopyUtils.copyBeanList(goodsSkuVOS, OrderItemDTO.class);
            orderItemDTOS.forEach(d -> {
                d.setSkuId(d.getId());
                d.setUnitPrice(d.getPrice());
                //skuid与id重名了
                d.setId(null);
            });
            orderDTO.setOrderItemDTOList(orderItemDTOS);

        });
        return createOrderDTO;
    }

    private List<CreatePayOrderDTO> buildCreatePayOrderDTOList(Set<Long> shopIdSet, Map<Long, Long> businessIdMap,
                                                               Map<Long, Long> bizOrderIdMap, Map<Long, List<EmptyCartGoodsSkuVO>> goodsSkuMap,
                                                               Long mainOrderId, Set<Long> couponIdSet) {
        //key:shopId,value:couponId
        Map<Long, Long> couponMap = new HashMap<>();
        if (couponIdSet != null && !couponIdSet.isEmpty()) {

            List<CouponVO> couponVOList = welfareApiService.queryByIdSet(couponIdSet).getData();

            if (!couponVOList.isEmpty()) {

                couponVOList.forEach(a -> {
                    couponMap.put(a.getShopId(), a.getId());
                });
            }

        }

        List<CreatePayOrderDTO> createPayOrderDTOList = new ArrayList<>();
        shopIdSet.forEach(a -> {
            Long couponId = couponMap.get(a);
            /**
             * 计算订单金额
             */
            List<EmptyCartGoodsSkuVO> emptyCartGoodsSkuVOS = goodsSkuMap.get(a);
            BigDecimal orderMoney = new BigDecimal(0);
            for (EmptyCartGoodsSkuVO vo : emptyCartGoodsSkuVOS) {
                BigDecimal v2 = BigDecimalUtils.multiply(vo.getPrice(), new BigDecimal(vo.getQuantity()));
                orderMoney = BigDecimalUtils.add(orderMoney, v2);
            }
            CreatePayOrderDTO createPayOrderDTO = new CreatePayOrderDTO();
            createPayOrderDTO.setShopId(a);
            createPayOrderDTO.setBizOrderId(bizOrderIdMap.get(a));
            createPayOrderDTO.setSource(0);
            createPayOrderDTO.setUserId(UserUtil.getUserId());
            createPayOrderDTO.setBusinessId(businessIdMap.get(a));
            createPayOrderDTO.setMainOrderId(mainOrderId);
            createPayOrderDTO.setOrderMoney(orderMoney);
            createPayOrderDTO.setCouponId(couponId);
            createPayOrderDTOList.add(createPayOrderDTO);

        });
        return createPayOrderDTOList;

    }
    @GlobalTransactional
    @SendMessage
    @Override
    public void delivery(DeliveryDTO dto) {

        //更新订单状态
        orderService.delivery(dto);
        Result result = logisticsApiService.logisticsAdd(buildLogisticsAddDTO(dto));
        Integer code = result.getCode();
        if (code != 200) {
            throw new BizException(result.getCode(), result.getMessage());
        }

        /**
         * 发送mq给物流服务
         */
        String finishContent = "您的订单已从库房发货交接";
        LogisticsTrackBathAddDTO bathAddDTO = MessageBuildAdapter.buildLogisticsTrackAddDTO(dto.getOrderId(), finishContent, LogisticsTrackStatusEnum.COLLECTED_ALREADY.getCode());
        bathAddDTO.setBizKeyValue(dto.getOrderId()+":"+finishContent);
        sendMqMessageService.sendMessage(TopicName.BATH_ADD_LOGISTICS_TRACK_TOPIC,bathAddDTO);

    }


    private LogisticsAddDTO buildLogisticsAddDTO(DeliveryDTO dto) {

        OrderDO orderDO = orderService.getById(dto.getOrderId());

        LogisticsAddDTO logisticsAddDTO = new LogisticsAddDTO();
        logisticsAddDTO.setOrderId(dto.getOrderId());
        logisticsAddDTO.setUserId(orderDO.getUserId());
        logisticsAddDTO.setExpressId(dto.getExpressId());

        logisticsAddDTO.setNumber(dto.getNumber());
        logisticsAddDTO.setToAddress(orderDO.getTakeAddress());

        return logisticsAddDTO;
    }

    @SendMessage
    @Override
    @GlobalTransactional
    public CreateOrderVO onePlaceAnOrder(OnePlaceAnOrderDTO dto) {


        OnePlaceAnOrderBO bo = buildOnePlaceAnOrderBO();
        ApiGoodsSkuVO goodsVO = goodsApiService.checkAndQueryGoods(BeanCopyUtils.copyBean(dto, CheckAndQueryGoodsDTO.class)).getData();
        CreatePayOrderDTO createPayOrderDTO = buildCreatePayOrderDTO(goodsVO, dto, bo);
        //创建支付订单
        Integer code = payApiService.createPayOrder(Arrays.asList(createPayOrderDTO)).getCode();
        if (!code.equals(200)) {

            throw new BizException("创建支付单异常");
        }
        CreateOrderDTO createOrderDTO = buildCreateOrderDTO(dto, goodsVO, bo);
        CreateOrderVO createOrderVO = mainOrderService.createOrder(createOrderDTO);

        /**
         * 发送物流信息
         */
        sendLogisticsMessage(createOrderVO.getOrderIdList());
        sendCloseOrderMessage(createOrderVO.getOrderIdList());
        return createOrderVO;

    }

    private OnePlaceAnOrderBO buildOnePlaceAnOrderBO() {


        Long mainOrderId = IdUtils.generateId();
        Long bizOrderId = IdUtils.generateId();

        OnePlaceAnOrderBO bo = new OnePlaceAnOrderBO();
        bo.setMainOrderId(mainOrderId);
        bo.setBizOrderId(bizOrderId);
        bo.setUserId(UserUtil.getUserId());

        return bo;
    }

    private CreatePayOrderDTO buildCreatePayOrderDTO(ApiGoodsSkuVO goodsVO, OnePlaceAnOrderDTO dto, OnePlaceAnOrderBO onePlaceAnOrderBO) {


        Integer source = 0;
        Long shopId = goodsVO.getShopId();
        Long businessId = -1L;
        Long couponId = dto.getCouponId();
        Integer quantity = dto.getQuantity();

        //计算订单金额
        BigDecimal orderMoney = BigDecimalUtils.multiply(goodsVO.getPrice(), new BigDecimal(quantity));

        CreatePayOrderDTO createPayOrderDTO = new CreatePayOrderDTO();
        createPayOrderDTO.setMainOrderId(onePlaceAnOrderBO.getMainOrderId());
        createPayOrderDTO.setShopId(shopId);
        createPayOrderDTO.setSource(source);
        createPayOrderDTO.setBizOrderId(onePlaceAnOrderBO.getBizOrderId());
        createPayOrderDTO.setUserId(onePlaceAnOrderBO.getUserId());
        createPayOrderDTO.setBusinessId(businessId);
        createPayOrderDTO.setCouponId(couponId);
        createPayOrderDTO.setOrderMoney(orderMoney);

        return createPayOrderDTO;

    }

    private CreateOrderDTO buildCreateOrderDTO(OnePlaceAnOrderDTO dto, ApiGoodsSkuVO goodsVO, OnePlaceAnOrderBO bo) {

        CreateOrderDTO createOrderDTO = new CreateOrderDTO();

        createOrderDTO.setTotalMoney(new BigDecimal(1));
        createOrderDTO.setOrderNumber(dto.getOrderNumber());
        createOrderDTO.setMainOrderId(bo.getMainOrderId());
        //构建OrderDTO
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setId(bo.getBizOrderId());
        orderDTO.setShopId(goodsVO.getShopId());
        orderDTO.setBusinessId(-1L);
        orderDTO.setUserId(bo.getUserId());
        orderDTO.setTakeAddress(dto.getTakeAddress());
        createOrderDTO.setOrderDTOList(Arrays.asList(orderDTO));
        //构建OrderItemDTO
        OrderItemDTO orderItemDTO = BeanCopyUtils.copyBean(goodsVO, OrderItemDTO.class);
        orderItemDTO.setSkuId(goodsVO.getId());
        orderItemDTO.setQuantity(dto.getQuantity());
        //copy把skuId复制
        orderItemDTO.setId(null);
        orderItemDTO.setUnitPrice(goodsVO.getPrice());
        orderDTO.setOrderItemDTOList(Arrays.asList(orderItemDTO));

        return createOrderDTO;
    }


}
