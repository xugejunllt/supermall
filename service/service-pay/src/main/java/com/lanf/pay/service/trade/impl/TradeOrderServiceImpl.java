package com.lanf.pay.service.trade.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.client.pay.model.dto.CancelTradeOrderDTO;
import com.lanf.client.pay.model.dto.CreatePayOrderDTO;
import com.lanf.client.pay.model.dto.CreateTradeOrderDTO;
import com.lanf.client.pay.model.dto.TradeOrderQuantitySumDTO;
import com.lanf.client.pay.model.enums.PayMethodEnum;
import com.lanf.client.pay.model.enums.TradePurposeEnum;
import com.lanf.client.pay.model.query.TradeOrderBathQuery;
import com.lanf.client.pay.model.query.TradeOrderQuery;
import com.lanf.client.pay.mq.message.PayOrderFlowInsertSuccessMessage;
import com.lanf.common.utils.BigDecimalUtil;
import com.lanf.common.utils.CodeGenerateUtils;
import com.lanf.common.utils.DateUtils;
import com.lanf.common.utils.JsonUtils;
import com.lanf.constant.constant.Constants;
import com.lanf.constant.enums.FlowNoPrefixEnum;
import com.lanf.constant.enums.FrozenStatusEnum;
import com.lanf.constant.exception.BizException;
import com.lanf.constant.result.Result;
import com.lanf.finance.model.enums.RecordTypeEnum;
import com.lanf.finance.mq.message.AddMoneyFlowMessage;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.pay.config.PayConfig;
import com.lanf.pay.mapper.TradeOrderMapper;
import com.lanf.pay.model.bo.CallbackResultBO;
import com.lanf.pay.model.bo.PassbackParams;
import com.lanf.pay.model.bo.PayCompensateOrderRetryPolicyBO;
import com.lanf.pay.model.dto.BathCreatePrepayOrderDTO;
import com.lanf.pay.model.dto.CreatePrepayOrderDTO;
import com.lanf.pay.model.dto.PrepayOrderDTO;
import com.lanf.pay.model.dto.RechargeDTO;
import com.lanf.pay.model.entity.*;
import com.lanf.pay.model.enums.BathTradeOrderStatusEnum;
import com.lanf.pay.model.enums.TradeOrderStatusEnum;
import com.lanf.pay.model.tcc.CancelTradeOrderBO;
import com.lanf.pay.model.vo.CreatePrepayOrderVO;
import com.lanf.pay.model.vo.CreateRechargeTradeOrderVO;
import com.lanf.pay.model.vo.PrepayOrderVO;
import com.lanf.pay.service.pay.IPayOrderFlowService;
import com.lanf.pay.service.pay.IPrepayPayTypeService;
import com.lanf.pay.service.pay.PaymentService;
import com.lanf.pay.service.pay.PaymentServiceFactory;
import com.lanf.pay.service.trade.IBathTradeOrderService;
import com.lanf.pay.service.trade.IPayOrderService;
import com.lanf.pay.service.trade.ITradeOrderItemService;
import com.lanf.pay.service.trade.ITradeOrderService;
import com.lanf.pay.utils.PayServiceUtils;
import com.lanf.rocketmq.model.TopicName;
import com.lanf.rocketmq.model.message.CompensatePaymentOrderMessage;
import com.lanf.rocketmq.model.message.RefundDTO;
import com.lanf.rocketmq.util.RocketMqClient;
import com.lanf.tcc.service.ITccOperationService;
import com.lanf.welfare.api.WelfareApiService;
import com.lanf.welfare.model.dto.UseCouponDTO;
import com.lanf.welfare.model.vo.UseCouponVO;
import lombok.extern.slf4j.Slf4j;
import org.dromara.hmily.annotation.HmilyTCC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <p>
 * 交易订单 服务实现类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-14
 */
@Slf4j
@Service
public class TradeOrderServiceImpl extends ServiceImpl<TradeOrderMapper, TradeOrderDO> implements ITradeOrderService {

    @Autowired
    private IPayOrderService payOrderService;

    @Autowired
    private ITradeOrderItemService tradeOrderItemService;
    @Autowired
    private WelfareApiService welfareApiService;
    @Autowired
    private ITccOperationService tccOperationService;
    @Autowired
    private IBathTradeOrderService bathTradeOrderService;
    @Autowired
    private IPayOrderFlowService payOrderFlowService;
    @Autowired
    private IPrepayPayTypeService prepayPayTypeService;

    @Autowired
    private static PayConfig payConfig;

    @Autowired
    private PayRetryPolicyCacheService payRetryPolicyCacheService;
    @Autowired
    private RocketMqClient rocketMqClient;


    private static final String PREPAY_PAY_TYPE_CACHE_KEY = "prepay_pay_type:%s";

    private static final long CACHE_EXPIRE_TIME = 30L;


    @HmilyTCC(confirmMethod = "confirmCreateTradeOrder", cancelMethod = "cancelCreateTradeOrder")
    @Override
    public void createTradeOrder(CreateTradeOrderDTO dto) {

    }


    @Transactional
    public void confirmCreateTradeOrder(CreateTradeOrderDTO dto) {

        log.info("confirmCreateTradeOrder:{}", dto);
        TradeOrderDO tradeOrderDO1 = this.lambdaQuery().eq(TradeOrderDO::getOrderNumber, dto.getOrderNumber()).one();
        if (tradeOrderDO1 != null) {
            log.info("交易单已存在");
            return;
        }
        TradeOrderDO tradeOrderDO = buildTradeOrderDO(dto);
        try {
            this.save(tradeOrderDO);
        } catch (DuplicateKeyException e) {
            log.info("交易单已存在");
        }

    }

    public void cancelCreateTradeOrder(CreateTradeOrderDTO dto) {

        log.info("cancelCreateTradeOrder");

    }

    private static TradeOrderDO buildTradeOrderDO(CreateTradeOrderDTO dto) {


        String outTradeNo = CodeGenerateUtils.generateFlowNo(FlowNoPrefixEnum.TRADE_ORDER,
                dto.getOrderNumber());
        Date expireTime = DateUtils.addMinutes(new Date(), payConfig.getExpireInterval().longValue());

        TradeOrderDO tradeOrderDO = new TradeOrderDO();
        tradeOrderDO.setBathPayOrderId(-1L);
        tradeOrderDO.setUserId(dto.getUserId());
        tradeOrderDO.setOrderId(dto.getOrderId());
        tradeOrderDO.setOutTradeNo(outTradeNo);
        tradeOrderDO.setTradeMoney(dto.getTradeMoney());
        tradeOrderDO.setPayStatus(0);
        tradeOrderDO.setBathPay(0);
        tradeOrderDO.setVersion(1L);
        tradeOrderDO.setOrderNumber(dto.getOrderNumber());
        tradeOrderDO.setExpireInterval(payConfig.getExpireInterval());
        tradeOrderDO.setExpireTime(expireTime);
        tradeOrderDO.setBusinessId(dto.getBusinessId());
        tradeOrderDO.setTradePurpose(TradePurposeEnum.REALTIME_ORDER);
        PassbackParams passbackParams = new PassbackParams();
        passbackParams.setBathPay(false);
        passbackParams.setTradeOrderId(tradeOrderDO.getId());
        passbackParams.setTradeType(TradePurposeEnum.REALTIME_ORDER);
        passbackParams.setSignValue(PayServiceUtils.generateSign(passbackParams));
        tradeOrderDO.setPassbackParams(JsonUtils.toJsonString(passbackParams));
        return tradeOrderDO;
    }


    @Override
    public CreatePayOrderVO createPayOrder(List<CreatePayOrderDTO> dto) {


        return null;
    }

    private BigDecimal getActualPayMoney(List<BigDecimal> discountMoneyList, BigDecimal orderMoney) {

        BigDecimal actualPayMoney = orderMoney;
        for (BigDecimal a : discountMoneyList) {
            actualPayMoney = BigDecimalUtil.subtract(orderMoney, a);
        }

        return actualPayMoney;
    }

    private Map<Long, UseCouponVO> useCoupon(List<CreatePayOrderDTO> dto) {

        List<UseCouponDTO> dtoList = new ArrayList<>();
        Map<Long, UseCouponVO> useCouponVOMap = new HashMap<>();
        dto.forEach(a -> {
            BigDecimal orderMoney = a.getOrderMoney();
            Long couponId = a.getCouponId();
            if (couponId != null) {
                UseCouponDTO useCouponDTO = new UseCouponDTO();
                useCouponDTO.setUserId(a.getUserId());
                useCouponDTO.setCouponId(a.getCouponId());
                useCouponDTO.setOrderMoney(orderMoney);
                dtoList.add(useCouponDTO);

            }

        });
        if (dtoList.isEmpty()) {

            return useCouponVOMap;
        }
        Result<List<UseCouponVO>> listResult = welfareApiService.bathUseCoupon(dtoList);
        Integer code = listResult.getCode();
        if (code == 200 && !listResult.getData().isEmpty()) {
            listResult.getData().forEach(a -> useCouponVOMap.put(a.getShopId(), a));

        } else {
            throw new BizException(listResult.getCode(), listResult.getMessage());
        }
        return useCouponVOMap;
    }

    /**
     * 查询订单交易信息
     */
    @Override
    public OrderTradeVO queryOrderTradeByOrderId(Long orderId) {

        TradeOrderDO tradeOrderDO = null;
        if (tradeOrderDO == null) {
            return null;
        }

        PayOrderDO payOrderDO = payOrderService.lambdaQuery().eq(PayOrderDO::getBizOrderId, orderId).one();
        if (payOrderDO == null) {
            return null;
        }

        TradeOrderItemDO discountInfo = null;

        List<TradeOrderItemDO> list = tradeOrderItemService.lambdaQuery().eq(TradeOrderItemDO::getTradeOrderId, tradeOrderDO.getId()).list();
        for (TradeOrderItemDO a : list) {
            if (a.getPayType().equals(3)) {
                discountInfo = a;
                break;
            }
        }
        Integer discountType = null;
        String discountTypeName = null;

        if (discountInfo != null) {
            discountType = discountInfo.getPayType();
            discountTypeName = "优惠券";
        }
        OrderTradeVO tradeVO = new OrderTradeVO();
        tradeVO.setOrderId(orderId);
        tradeVO.setPayType(payOrderDO.getPayType());
        tradeVO.setPayMoney(payOrderDO.getPayMoney());
        tradeVO.setDiscountType(discountType);
        tradeVO.setDiscountTypeName(discountTypeName);
        tradeVO.setPayTypeName(getPayTypeName(payOrderDO.getPayType()));
        tradeVO.setReceiptMoney(payOrderDO.getReceiptMoney());
        tradeVO.setIncomeAccount(payOrderDO.getIncomeAccount());
        tradeVO.setShopId(payOrderDO.getShopId());
        tradeVO.setPayStatus(payOrderDO.getPayStatus());
        return tradeVO;
    }

    private String getPayTypeName(Integer payType) {

        if (payType.equals(0)) {
            return "支付宝";
        }

        if (payType.equals(1)) {
            return "微信";
        }

        if (payType.equals(2)) {
            return "银联";
        }
        return "支付宝";

    }

    @Transactional
    @Override
    public void refund(RefundDTO dto) {


    }

    @Override
    public TradeOrderApiVO tradeOrderQuery(TradeOrderQuery query) {

        return null;
    }

    /**
     * 查询待优化
     * 支付完成后，写入中间表
     */
    @Override
    public Integer tradeOrderQuantitySum(TradeOrderQuantitySumDTO dto) {


        List<TradeOrderDO> tradeOrderDOList = this.lambdaQuery().select(BaseEntity::getId).
                eq(TradeOrderDO::getPayStatus, 2).list();
        List<Long> collect = tradeOrderDOList.stream().map(BaseEntity::getId).collect(Collectors.toList());

        return payOrderService.lambdaQuery().in(PayOrderDO::getTradeOrderId, collect).
                eq(PayOrderDO::getPayType, dto.getPayType()).
                eq(PayOrderDO::getPayAccount, dto.getPayAccount()).
                eq(PayOrderDO::getPayStatus, 2).count();
    }

    @Override
    public List<TradeOrderBathVO> tradeOrderBathQuery(TradeOrderBathQuery query) {

        List<Long> tradeOrderIdList = query.getTradeOrderIdList();
        List<Long> orderIdList = query.getOrderIdList();
        List<PayOrderDO> orderDOList = payOrderService.lambdaQuery().
                in(tradeOrderIdList != null, PayOrderDO::getTradeOrderId, tradeOrderIdList).
                in(orderIdList != null, PayOrderDO::getBizOrderId, orderIdList).
                list();

        List<TradeOrderBathVO> voList = new ArrayList<>(orderDOList.size());

        orderDOList.forEach(a -> {
            TradeOrderBathVO vo = new TradeOrderBathVO();
            vo.setPayMoney(a.getPayMoney());
            vo.setTradeOrderId(a.getTradeOrderId());
            vo.setPayType(a.getPayType());
            vo.setOrderId(a.getBizOrderId());
            voList.add(vo);
        });

        return voList;
    }

    @Override
    public CreatePrepayOrderVO createPrepayOrder(CreatePrepayOrderDTO dto) {

        String orderNumber = dto.getOrderNumber();
        TradeOrderDO tradeOrderDO = this.lambdaQuery().eq(TradeOrderDO::getOrderNumber, orderNumber).one();

        if (tradeOrderDO == null) {
            log.error("交易单不存在");
            throw new BizException("交易单不存在");
        }
        if (!TradeOrderStatusEnum.PENDING.getCode().
                equals(tradeOrderDO.getPayStatus())) {
            log.info("交易单状态异常");
            throw new BizException("交易单状态异常");
        }
        Integer payType = dto.getPayType();
        boolean saveIfAbsent = prepayPayTypeService.saveIfAbsent(tradeOrderDO.getOutTradeNo(), payType);
        if (!saveIfAbsent) {
            /**
             * 发送补单任务
             */
            PayCompensateOrderRetryPolicyBO firstLevelRetryPolicy = payRetryPolicyCacheService.getFirstLevelRetryPolicy();
            CompensatePaymentOrderMessage message = new CompensatePaymentOrderMessage();
            message.setOutTradeNo(tradeOrderDO.getOutTradeNo());
            message.setPayType(payType);
            message.setRetryLevel(firstLevelRetryPolicy.getRetryLevel());
            message.setBathOrder(true);
            rocketMqClient.sendDelayMessage(TopicName.COMPENSATE_PAYMENT_TOPIC,
                    JsonUtils.toJsonString(message), TimeUnit.SECONDS, firstLevelRetryPolicy.getDelaySeconds());

        }

        PaymentService paymentService = PaymentServiceFactory.getPaymentService(dto.getPayType());
        PassbackParams passbackParams = JsonUtils.toObject(tradeOrderDO.getPassbackParams(),
                PassbackParams.class);
        PrepayOrderDTO prepayOrderDTO = new PrepayOrderDTO();
        prepayOrderDTO.setOutTradeNo(tradeOrderDO.getOutTradeNo());
        prepayOrderDTO.setTotalAmount(tradeOrderDO.getTradeMoney());
        prepayOrderDTO.setExpireInterval(tradeOrderDO.getExpireInterval());
        prepayOrderDTO.setPassbackParams(passbackParams);
        PrepayOrderVO prepayOrderVO = paymentService.createPrepayOrder(prepayOrderDTO);
        CreatePrepayOrderVO vo = new CreatePrepayOrderVO();
        vo.setOrderStr(prepayOrderVO.getOrderStr());

        return vo;
    }

    public CreatePrepayOrderVO bathCreatePrepayOrder(BathCreatePrepayOrderDTO dto) {

        BathTradeOrderDO bathTradeOrderDO = bathTradeOrderService.lambdaQuery()
                .eq(BathTradeOrderDO::getMainOrderId, dto.getMainOrderId()).one();

        if (bathTradeOrderDO == null) {
            log.warn("批量交易单不存在");
            throw new BizException("批量交易单不存在");
        }
        if (!BathTradeOrderStatusEnum.PENDING.getCode().
                equals(bathTradeOrderDO.getPayStatus())) {
            log.warn("交易单状态异常");
            throw new BizException("交易单状态异常");
        }
        Integer payType = dto.getPayType();
        boolean saveIfAbsent = prepayPayTypeService.saveIfAbsent(bathTradeOrderDO.getOutTradeNo(), payType);
        if (!saveIfAbsent) {
            /**
             * 发送补单任务
             */
            PayCompensateOrderRetryPolicyBO firstLevelRetryPolicy = payRetryPolicyCacheService.getFirstLevelRetryPolicy();
            CompensatePaymentOrderMessage message = new CompensatePaymentOrderMessage();
            message.setOutTradeNo(bathTradeOrderDO.getOutTradeNo());
            message.setPayType(payType);
            message.setRetryLevel(firstLevelRetryPolicy.getRetryLevel());
            message.setBathOrder(true);
            rocketMqClient.sendDelayMessage(TopicName.COMPENSATE_PAYMENT_TOPIC,
                    JsonUtils.toJsonString(message), TimeUnit.SECONDS, firstLevelRetryPolicy.getDelaySeconds());

        }
        PaymentService paymentService = PaymentServiceFactory.getPaymentService(dto.getPayType());
        PassbackParams passbackParams =  JsonUtils.toObject(bathTradeOrderDO.getPassbackParams(),
                PassbackParams.class);

        PrepayOrderDTO prepayOrderDTO = new PrepayOrderDTO();
        prepayOrderDTO.setOutTradeNo(bathTradeOrderDO.getOutTradeNo());
        prepayOrderDTO.setTotalAmount(bathTradeOrderDO.getBatchFee());
        prepayOrderDTO.setPassbackParams(passbackParams);
        PrepayOrderVO prepayOrderVO = paymentService.createPrepayOrder(prepayOrderDTO);

        CreatePrepayOrderVO vo = new CreatePrepayOrderVO();
        vo.setOrderStr(prepayOrderVO.getOrderStr());

        return vo;
    }

    private AddMoneyFlowMessage buildAddMoneyFlowMessage(CallbackResultBO resultBO){


        PassbackParams passbackParams = resultBO.getPassbackParams();
        TradePurposeEnum tradePurposeEnum = passbackParams.getTradeType();
        RecordTypeEnum recordType = null;
        switch (tradePurposeEnum){
            case REALTIME_ORDER:
                recordType = RecordTypeEnum.ORDER;
                break;
            case WALLET_RECHARGE:
                recordType = RecordTypeEnum.WALLET_RECHARGE;
                break;
            default:
                log.error("不支持的用途");
              throw new BizException("不支持的用途");

        }
        AddMoneyFlowMessage addMoneyFlowMessage = new AddMoneyFlowMessage();
        addMoneyFlowMessage.setBusinessId(Constants.PLATFORM_BUSINESS_ID);
        addMoneyFlowMessage.setBizOrderId(resultBO.getPassbackParams().getTradeOrderId());
        addMoneyFlowMessage.setIncomeMoney(resultBO.getReceiptMoney());
        addMoneyFlowMessage.setRecordType(recordType);
        addMoneyFlowMessage.setFlowNo(CodeGenerateUtils.generateSerialNumber(passbackParams.getTradeOrderId().toString()));

        return addMoneyFlowMessage;
    }

    private PayOrderFlowInsertSuccessMessage buildPayOrderFlowInsertSuccessMessage
            (CallbackResultBO resultBO,Integer payType){
        PassbackParams passbackParams = resultBO.getPassbackParams();
        TradePurposeEnum tradeType = passbackParams.getTradeType();
        PayOrderFlowInsertSuccessMessage message = new PayOrderFlowInsertSuccessMessage();
        message.setOutTradeNo(resultBO.getOutTradeNo());
        message.setBathPay(passbackParams.getBathPay());
        message.setPayType(payType);
        message.setTradePurpose(tradeType);
        /**
         * 支付回调 那么一定是三方支付
         */
        message.setPayMethod(PayMethodEnum.THIRD_PARTY_PAY);
        return message;
    }

    private BigDecimal getTradeMoney(String outTradeNo, Boolean bathPay) {
        BigDecimal tradeMoney = null;
        if (bathPay) {

            BathTradeOrderDO bathTradeOrderDO = bathTradeOrderService.lambdaQuery()
                    .eq(BathTradeOrderDO::getOutTradeNo, outTradeNo)
                    .one();
            if (bathTradeOrderDO == null) {
                log.error("批量交易单不存在outTradeNo:[{}]", outTradeNo);
                return tradeMoney;
            }
            tradeMoney = bathTradeOrderDO.getBatchFee();
        } else {
            TradeOrderDO tradeOrderDO = this.lambdaQuery()
                    .eq(TradeOrderDO::getOutTradeNo, outTradeNo)
                    .one();
            if (tradeOrderDO == null) {
                log.error("交易单不存在outTradeNo:[{}]", outTradeNo);
                return tradeMoney;
            }
            tradeMoney = tradeOrderDO.getTradeMoney();
        }
        return tradeMoney;
    }



    /**
     * 是否已经支付过 以唯一支付流水为准
     */
    private boolean isAlreadyPaid(String outTradeNo, Integer payType) {

        PayOrderFlowDO flowDO = payOrderFlowService.lambdaQuery()
                .eq(PayOrderFlowDO::getOutTradeNo, outTradeNo)
                .eq(PayOrderFlowDO::getPayType, payType).one();
        return flowDO != null;
    }


    private PayOrderFlowDO buildPayOrderFlowDO(Integer payType, CallbackResultBO resultBO) {
        PayOrderFlowDO payOrderFlowDO = new PayOrderFlowDO();
        payOrderFlowDO.setPayType(payType);
        payOrderFlowDO.setOutTradeNo(resultBO.getOutTradeNo());
        payOrderFlowDO.setTradeMoney(resultBO.getReceiptMoney());
        payOrderFlowDO.setReceiptMoney(resultBO.getReceiptMoney());
        payOrderFlowDO.setPayFinishTime(resultBO.getPayFinishTime());
        payOrderFlowDO.setPayAccount(resultBO.getPayAccount());
        payOrderFlowDO.setIncomeAccount(resultBO.getIncomeAccount());
        payOrderFlowDO.setNotifyTime(resultBO.getNotifyTime());
        payOrderFlowDO.setTradeNo(resultBO.getTradeNo());
        payOrderFlowDO.setPassbackParams(JsonUtils.toJsonString(resultBO.getPassbackParams()));
        payOrderFlowDO.setAllParams(resultBO.getAllParams());

        return payOrderFlowDO;
    }


    @HmilyTCC(confirmMethod = "confirmCancelTradeOrder", cancelMethod = "cancelCancelTradeOrder")
    @Transactional
    @Override
    public CancelTradeOrderVO cancelTradeOrder(CancelTradeOrderDTO dto) {

        TradeOrderDO tradeOrderDO = this.lambdaQuery().eq(TradeOrderDO::getOrderId, dto.getOrderId()).one();
        if (tradeOrderDO == null) {
            log.warn("交易单不存在");
            throw new BizException("交易单不存在");
        }
        if (TradeOrderStatusEnum.CANCELLED.getCode().equals(tradeOrderDO.getPayStatus())) {
            log.warn("交易单已取消");
            throw new BizException("交易单已取消");

        }
        if (FrozenStatusEnum.FROZEN.getCode().equals(tradeOrderDO.getFrozen())) {
            log.warn("交易单已冻结");
            throw new BizException("交易单已冻结");
        }
        /**
         * DB 操作
         */
        String bizKey = buildCancelTradeOrderKey(dto.getBizKeySuffix());
        CancelTradeOrderBO cancelTradeOrderBO = new CancelTradeOrderBO();
        cancelTradeOrderBO.setCurrentPayStatus(tradeOrderDO.getPayStatus());
        tccOperationService.tryOperation(bizKey, JsonUtils.toJsonString(cancelTradeOrderBO));
        /**
         * 更新交易单状态
         */
        boolean update = this.lambdaUpdate()
                .eq(BaseEntity::getId, tradeOrderDO.getId())
                .eq(TradeOrderDO::getPayStatus, tradeOrderDO.getPayStatus())
                .eq(TradeOrderDO::getFrozen, FrozenStatusEnum.NORMAL.getCode())
                .eq(TradeOrderDO::getVersion, tradeOrderDO.getVersion())
                .set(TradeOrderDO::getPayStatus, TradeOrderStatusEnum.CANCELLED.getCode())
                .set(TradeOrderDO::getVersion, tradeOrderDO.getVersion() + 1)
                .set(TradeOrderDO::getFrozen, FrozenStatusEnum.FROZEN.getCode())
                .update();
        if (!update) {
            log.info("交易单更新失败");
            throw new BizException("交易单更新失败");
        }

        return null;

    }

    private String buildCancelTradeOrderKey(String bizKeySuffix) {
        return "cancelTradeOrder:" + bizKeySuffix;
    }


    @Transactional
    public void confirmCancelTradeOrder(CancelTradeOrderDTO dto) {

        log.info("confirmCancelTradeOrder:{}", dto);

        String bizKey = buildCancelTradeOrderKey(dto.getBizKeySuffix());
        TradeOrderDO tradeOrderDO = this.lambdaQuery().eq(TradeOrderDO::getId, dto.getOrderId()).one();
        if (tradeOrderDO == null) {

            log.error("交易单不存在");
            throw new BizException("交易单不存在");
        }
        boolean operation = tccOperationService.confirmOperation(bizKey);
        if (!operation) {
            log.info("confirm已执行");
            return;
        }
        /**
         *  解冻
         */
        boolean update = this.lambdaUpdate()
                .eq(BaseEntity::getId, tradeOrderDO.getId())
                .eq(TradeOrderDO::getPayStatus, TradeOrderStatusEnum.CANCELLED.getCode())
                .eq(TradeOrderDO::getFrozen, FrozenStatusEnum.FROZEN.getCode())
                .eq(TradeOrderDO::getVersion, tradeOrderDO.getVersion())
                .set(TradeOrderDO::getVersion, tradeOrderDO.getVersion() + 1)
                .set(TradeOrderDO::getFrozen, FrozenStatusEnum.NORMAL.getCode())
                .update();
        if (!update) {
            log.info("交易单更新失败");
            throw new BizException("交易单更新失败");
        }


    }

    public void cancelCancelTradeOrder(CancelTradeOrderDTO dto) {
        log.info("cancelCancelTradeOrder:{}", dto);
        String bizKey = buildCancelTradeOrderKey(dto.getBizKeySuffix());
        String parameter = tccOperationService.getParameter(bizKey);
        CancelTradeOrderBO cancelTradeOrderBO = JsonUtils.toObject(parameter, CancelTradeOrderBO.class);
        TradeOrderDO tradeOrderDO = this.lambdaQuery().eq(TradeOrderDO::getId, dto.getOrderId()).one();

        if (tradeOrderDO == null) {
            log.error("交易单不存在");
            throw new BizException("交易单不存在");
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
                .eq(BaseEntity::getId, tradeOrderDO.getId())
                .eq(TradeOrderDO::getPayStatus, TradeOrderStatusEnum.CANCELLED.getCode())
                .eq(TradeOrderDO::getFrozen, FrozenStatusEnum.FROZEN.getCode())
                .eq(TradeOrderDO::getVersion, tradeOrderDO.getVersion())
                .set(TradeOrderDO::getPayStatus, cancelTradeOrderBO.getCurrentPayStatus())
                .set(TradeOrderDO::getVersion, tradeOrderDO.getVersion() + 1)
                .set(TradeOrderDO::getFrozen, FrozenStatusEnum.NORMAL.getCode())
                .update();
        if (!update) {
            log.info("交易单更新失败");
            throw new BizException("交易单更新失败");
        }


    }

    @Override
    public CreateRechargeTradeOrderVO createRechargeTradeOrder(RechargeDTO dto) {
        Date expireTime = DateUtils.addMinutes(new Date(), payConfig.getExpireInterval().longValue());
        String outTradeNo = CodeGenerateUtils.generateFlowNo(FlowNoPrefixEnum.TRADE_ORDER, dto.getOrderNumber());
        TradeOrderDO tradeOrderDO = new TradeOrderDO();
        tradeOrderDO.setUserId(UserIdContext.getUserId());
        tradeOrderDO.setOrderNumber(dto.getOrderNumber());
        tradeOrderDO.setOutTradeNo(PayServiceUtils.generateOutTradeNo(dto.getOrderNumber()));
        tradeOrderDO.setTradeMoney(dto.getAmount());
        tradeOrderDO.setTradePurpose(TradePurposeEnum.WALLET_RECHARGE);
        tradeOrderDO.setPayStatus(TradeOrderStatusEnum.PENDING.getCode());
        tradeOrderDO.setBathPay(0);
        tradeOrderDO.setExpireInterval(payConfig.getExpireInterval());
        tradeOrderDO.setExpireTime(expireTime);
        tradeOrderDO.setOutTradeNo(outTradeNo);
        String params = PayServiceUtils.buildPassbackParams(tradeOrderDO.getId(), false,
                tradeOrderDO.getTradeMoney(),
                TradePurposeEnum.REALTIME_ORDER);
        tradeOrderDO.setPassbackParams(params);

        try {
            this.save(tradeOrderDO);
        } catch (DuplicateKeyException e) {
            log.warn("重复交易");
            throw new BizException("重复交易");
        }
        /**
         * 构建返回值
         */
        CreateRechargeTradeOrderVO createRechargeTradeOrderVO = new CreateRechargeTradeOrderVO();
        createRechargeTradeOrderVO.setOrderNumber(tradeOrderDO.getOrderNumber());

        return createRechargeTradeOrderVO;
    }


}
