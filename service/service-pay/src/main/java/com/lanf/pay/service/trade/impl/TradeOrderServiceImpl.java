package com.lanf.pay.service.trade.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.common.utils.BigDecimalUtil;
import com.lanf.common.utils.DateUtils;
import com.lanf.common.utils.JsonUtils;
import com.lanf.constant.enums.FrozenStatusEnum;
import com.lanf.constant.exception.BizException;
import com.lanf.constant.result.Result;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.pay.mapper.TradeOrderMapper;
import com.lanf.pay.model.bo.*;
import com.lanf.pay.model.dto.*;
import com.lanf.pay.model.entity.*;
import com.lanf.pay.model.enums.*;
import com.lanf.pay.model.query.TradeOrderBathQuery;
import com.lanf.pay.model.query.TradeOrderQuery;
import com.lanf.pay.model.tcc.CancelTradeOrderBO;
import com.lanf.pay.model.vo.*;
import com.lanf.pay.service.pay.IPayOrderFlowService;
import com.lanf.pay.service.pay.PaymentService;
import com.lanf.pay.service.pay.PaymentServiceFactory;
import com.lanf.pay.service.pay.config.PayConfig;
import com.lanf.pay.service.trade.*;
import com.lanf.pay.utils.PryServiceUtils;
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
    private PayConfig payConfig;

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


        String outTradeNo = PryServiceUtils.generateOutTradeNo(dto.getOrderId());
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

        Long orderId = dto.getOrderId();
        TradeOrderDO tradeOrderDO = this.lambdaQuery().eq(TradeOrderDO::getOrderId, orderId).one();

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
        PrepayOrderDTO prepayOrderDTO = new PrepayOrderDTO();
        prepayOrderDTO.setOutTradeNo(tradeOrderDO.getOutTradeNo());
        prepayOrderDTO.setTotalAmount(tradeOrderDO.getTradeMoney());
        prepayOrderDTO.setBathPay(false);
        prepayOrderDTO.setExpireInterval(tradeOrderDO.getExpireInterval());
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
        PrepayOrderDTO prepayOrderDTO = new PrepayOrderDTO();
        prepayOrderDTO.setOutTradeNo(bathTradeOrderDO.getOutTradeNo());
        prepayOrderDTO.setTotalAmount(bathTradeOrderDO.getBatchFee());
        prepayOrderDTO.setBathPay(true);
        PrepayOrderVO prepayOrderVO = paymentService.createPrepayOrder(prepayOrderDTO);

        CreatePrepayOrderVO vo = new CreatePrepayOrderVO();
        vo.setOrderStr(prepayOrderVO.getOrderStr());

        return vo;
    }


    @Override
    public void payCallback(PayCallbackDTO dto) {

        PaymentService paymentService = PaymentServiceFactory.getPaymentService(dto.getPayType());
        try {
            handlePayCallback(dto);
        } catch (Exception e) {
            log.error("支付回调异常", e);
            paymentService.responsePayFail(dto.getResponse());

        }

    }


    private void handlePayCallback(PayCallbackDTO dto) {

        PaymentService paymentService = PaymentServiceFactory.getPaymentService(dto.getPayType());
        CallbackResultBO resultBO = null;

        try {
            resultBO = paymentService.parse(dto.getRequest());
        } catch (Exception e) {
            paymentService.responsePayFail(dto.getResponse());
            return;
        }
        PaySuccessHandleBO paySuccessHandleBO = new PaySuccessHandleBO();
        paySuccessHandleBO.setPayType(dto.getPayType());
        paySuccessHandleBO.setResultBO(resultBO);
        PaySuccessHandleResultBO handleResultBO = paySuccessHandleBO(paySuccessHandleBO);
        if (handleResultBO.getHandleSuccess()) {
            paymentService.responsePayOk(dto.getResponse());
        } else {
            paymentService.responsePayFail(dto.getResponse());
        }
    }

    @Override
    public PaySuccessHandleResultBO paySuccessHandleBO(PaySuccessHandleBO paySuccessHandleBO) {

        CallbackResultBO resultBO = paySuccessHandleBO.getResultBO();
        String outTradeNo = resultBO.getOutTradeNo();
        boolean responseOk = false;
        PaySceneEnum payScene = getPayScene(outTradeNo, resultBO.getBathPay());

        if (PaySceneEnum.SINGLE_ORDER_SINGLE_PAY.equals(payScene)) {
            try {
                responseOk = handleSinglePayScene(outTradeNo, resultBO, responseOk, paySuccessHandleBO.getPayType());
            } catch (BizException ignored) {
            }

        }
        if (PaySceneEnum.COMBINED_PAY.equals(payScene)) {
            try {
                handleCombinedPayScene(outTradeNo, resultBO, responseOk, paySuccessHandleBO.getPayType());
            } catch (BizException ignored) {
            }
        }
        if (PaySceneEnum.COMBINED_TO_SINGLE_PAY.equals(payScene)) {
            try {
                handleCombinedToSinglePayScene(outTradeNo, resultBO, responseOk, paySuccessHandleBO.getPayType());
            } catch (BizException ignored) {
            }
        }
        return new PaySuccessHandleResultBO(responseOk);

    }


    private PaySceneEnum getPayScene(String outTradeNo, Boolean bathPay) {

        if (!bathPay) {
            TradeOrderDO tradeOrderDO = this.lambdaQuery()
                    .eq(TradeOrderDO::getOutTradeNo, outTradeNo)
                    .one();
            if (tradeOrderDO == null) {
                throw new BizException("交易单不存在");
            }
            if (tradeOrderDO.getBathPay() == 0) {
                return PaySceneEnum.SINGLE_ORDER_SINGLE_PAY;
            }
            return PaySceneEnum.COMBINED_TO_SINGLE_PAY;
        }
        log.info("组合付款");

        return PaySceneEnum.COMBINED_PAY;

    }

    @Transactional
    public boolean handleSinglePayScene(String outTradeNo, CallbackResultBO resultBO, Boolean responseOk, Integer payType) {
        TradeOrderDO tradeOrderDO = this.lambdaQuery()
                .eq(TradeOrderDO::getOutTradeNo, outTradeNo)
                .one();

        if (tradeOrderDO == null) {
            log.error("交易单不存在outTradeNo:[{}]", outTradeNo);
            return false;
        }

        BigDecimal totalAmount = resultBO.getTotalAmount();
        BigDecimal tradeMoney = tradeOrderDO.getTradeMoney();
        if (!totalAmount.equals(tradeMoney)) {
            log.error("交易金额异常 outTradeNo:[{}],totalAmount[{}],tradeMoney[{}]", outTradeNo, totalAmount, tradeMoney);
            return false;
        }
        boolean alreadyPaid = isAlreadyPaid(outTradeNo, payType);

        if (alreadyPaid) {
            log.info("交易单支付成功 outTradeNo:[{}]", outTradeNo);
            return true;
        }

        if (TradeOrderStatusEnum.CANCELLED.getCode().
                equals(tradeOrderDO.getPayStatus())) {
            log.info("交易单已取消");
            /**
             * 进行退款 极端场景发生
             */

        }

        if (TradeOrderStatusEnum.COMPLETED.getCode()
                .equals(tradeOrderDO.getPayStatus()) && !alreadyPaid) {
            /**
             * 退款 被其他支付渠道支付过了
             */

        }


        if (BathTradeOrderStatusEnum.MERGE_TRANSFER_SINGLE.getCode().
                equals(tradeOrderDO.getPayStatus())) {
            /**
             * 待处理--合并转换
             */

        }
        if (TradeOrderStatusEnum.PENDING.getCode().equals(tradeOrderDO.getPayStatus())) {
            PayOrderFlowDO payOrderFlowDO = buildPayOrderFlowDO(payType, resultBO);

            try {
                payOrderFlowService.save(payOrderFlowDO);
            } catch (DuplicateKeyException e) {
                log.info("交易单支付成功 outTradeNo:[{}]", outTradeNo);
                return true;
            }

            boolean update = this.lambdaUpdate()
                    .eq(BaseEntity::getId, tradeOrderDO.getId())
                    .eq(TradeOrderDO::getVersion, tradeOrderDO.getVersion())
                    .eq(TradeOrderDO::getPayStatus, TradeOrderStatusEnum.PENDING.getCode())
                    .set(TradeOrderDO::getPayStatus, BathTradeOrderStatusEnum.COMPLETED.getCode())
                    .set(TradeOrderDO::getVersion, tradeOrderDO.getVersion() + 1)
                    .update();
            if (!update) {
                log.error("交易单更新失败");
                /**
                 * 抛出异常 回滚事务
                 */
                throw new BizException("交易单更新失败");
            }
            return true;
        }
        log.error("未知场景");
        throw new BizException("未知场景");

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


    @Transactional
    public void handleCombinedPayScene(String outTradeNo, CallbackResultBO resultBO, Boolean responseOk, Integer payType) {
        BathTradeOrderDO bathTradeOrderDO = bathTradeOrderService.lambdaQuery()
                .eq(BathTradeOrderDO::getOutTradeNo, outTradeNo)
                .one();

        if (bathTradeOrderDO == null) {
            log.error("批量交易单不存在");
            responseOk = false;
            return;
        }

        BigDecimal totalAmount = resultBO.getTotalAmount();
        BigDecimal tradeMoney = bathTradeOrderDO.getBatchFee();
        if (!totalAmount.equals(tradeMoney)) {
            log.error("交易金额异常 outTradeNo:[{}],totalAmount[{}],tradeMoney[{}]", outTradeNo, totalAmount, tradeMoney);
            responseOk = false;
            return;
        }

        boolean alreadyPaid = isAlreadyPaid(outTradeNo, payType);

        if (alreadyPaid) {
            log.info("交易单支付成功 outTradeNo:[{}]", outTradeNo);
            responseOk = true;
            return;
        }

        if (BathTradeOrderStatusEnum.COMPLETED.getCode()
                .equals(bathTradeOrderDO.getPayStatus()) && !alreadyPaid) {

            /**
             * 退款
             */

        }
        if (TradeOrderStatusEnum.CANCELLED.getCode().
                equals(bathTradeOrderDO.getPayStatus())) {
            log.info("交易单已取消");
            /**
             * 进行退款 极端场景发生
             */


        }
        Integer payStatus = bathTradeOrderDO.getPayStatus();

        if (BathTradeOrderStatusEnum.MERGE_TRANSFER_SINGLE.getCode().equals(payStatus)) {
            /**
             * 合并单已转单笔 进行退款
             */

        }
        if (BathTradeOrderStatusEnum.PENDING.getCode().equals(payStatus)) {

            List<TradeOrderDO> tradeOrderDOList = this.lambdaQuery().
                    eq(TradeOrderDO::getBathPayOrderId, bathTradeOrderDO.getId()).list();

            PayOrderFlowDO payOrderFlowDO = buildPayOrderFlowDO(payType, resultBO);
            try {
                payOrderFlowService.save(payOrderFlowDO);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            boolean update1 = bathTradeOrderService.lambdaUpdate()
                    .eq(BaseEntity::getId, bathTradeOrderDO.getId())
                    .eq(BathTradeOrderDO::getVersion, bathTradeOrderDO.getVersion())
                    .set(BathTradeOrderDO::getPayStatus, BathTradeOrderStatusEnum.COMPLETED.getCode())
                    .set(BathTradeOrderDO::getVersion, bathTradeOrderDO.getVersion() + 1)
                    .update();
            if (!update1) {
                log.warn("交易单更新失败");
                responseOk = false;
                /**
                 * 抛出异常 回滚事务
                 */
                throw new BizException("交易单更新失败");
            }

            /**
             *
             * 这里修改一下 要么全部成功 要么全部失败
             *
             */
            for (TradeOrderDO tradeOrderDO : tradeOrderDOList) {
                boolean update = this.lambdaUpdate()
                        .eq(BaseEntity::getId, tradeOrderDO.getId())
                        .eq(TradeOrderDO::getVersion, tradeOrderDO.getVersion())
                        .eq(TradeOrderDO::getPayStatus, TradeOrderStatusEnum.PENDING.getCode())
                        .set(TradeOrderDO::getPayStatus, TradeOrderStatusEnum.COMPLETED.getCode())
                        .set(TradeOrderDO::getVersion, tradeOrderDO.getVersion() + 1)
                        .update();
                if (!update) {
                    log.warn("交易单更新失败");
                    responseOk = false;
                    /**
                     * 抛出异常 回滚事务
                     */
                    throw new BizException("交易单更新失败");
                }
            }

        }



    }


    @Transactional
    public void handleCombinedToSinglePayScene(String outTradeNo, CallbackResultBO resultBO, Boolean responseOk, Integer payType) {
        TradeOrderDO tradeOrderDO = this.lambdaQuery()
                .eq(TradeOrderDO::getOutTradeNo, outTradeNo)
                .one();

        if (tradeOrderDO == null) {
            log.error("交易单不存在");
            responseOk = false;
            throw new BizException("交易单不存在");
        }

        Long bathPayOrderId = tradeOrderDO.getBathPayOrderId();
        BathTradeOrderDO orderDO = bathTradeOrderService.getById(bathPayOrderId);
        if (orderDO == null) {
            log.error("组合支付单不存在");
            responseOk = false;
            throw new BizException("组合支付单不存在");
        }


        boolean alreadyPaid = isAlreadyPaid(outTradeNo, payType);
        if (alreadyPaid) {
            log.info("交易单支付成功 outTradeNo:[{}]", outTradeNo);
            responseOk = true;
            return;
        }


        if (TradeOrderStatusEnum.COMPLETED.getCode()
                .equals(tradeOrderDO.getPayStatus()) && !alreadyPaid) {

            return;
        }

        if (TradeOrderStatusEnum.CANCELLED.getCode().equals(orderDO.getPayStatus())) {
            log.info("交易已取消");
            responseOk = true;
            return;
        }
        if (BathTradeOrderStatusEnum.PENDING.getCode().equals(orderDO.getPayStatus())) {
            log.info("更新批量交易单状态");

            bathTradeOrderService.lambdaUpdate()
                    .eq(BaseEntity::getId, bathPayOrderId)
                    .eq(BathTradeOrderDO::getVersion, orderDO.getVersion())
                    .set(BathTradeOrderDO::getPayStatus, BathTradeOrderStatusEnum.MERGE_TRANSFER_SINGLE.getCode())
                    .set(BathTradeOrderDO::getVersion, orderDO.getVersion() + 1)
                    .update();

        }
        PayOrderFlowDO payOrderFlowDO = buildPayOrderFlowDO(payType, resultBO);
        boolean update = this.lambdaUpdate()
                .eq(TradeOrderDO::getId, tradeOrderDO.getId())
                .eq(TradeOrderDO::getVersion, tradeOrderDO.getVersion())
                .set(TradeOrderDO::getPayStatus, TradeOrderStatusEnum.COMPLETED.getCode())
                .set(TradeOrderDO::getVersion, tradeOrderDO.getVersion() + 1)
                .update();
        if (!update) {
            log.error("交易单已支付");
            responseOk = false;
            throw new BizException("交易单已支付");
        }
        payOrderFlowService.save(payOrderFlowDO);

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
        if (FrozenStatusEnum.FROZEN.getCode().equals(tradeOrderDO.getFrozen())){
                log.warn("交易单已冻结");
            throw new BizException("交易单已冻结");
        }

        List<Integer> payTypesByOutTradeNo = prepayPayTypeService.getPayTypesByOutTradeNo(tradeOrderDO.getOutTradeNo());
        if (payTypesByOutTradeNo.isEmpty()) {
            log.info("未查询到支付方式，直接更新交易单状态:orderId={}", dto.getOrderId());
            updateTradeOrderToCancelled(tradeOrderDO);
            return new CancelTradeOrderVO();
        }
        CancelTradeOrderVO orderVO = cancelThirdPartyPayments(tradeOrderDO.getOutTradeNo(), payTypesByOutTradeNo);

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

        return orderVO;

    }
    private String buildCancelTradeOrderKey(String bizKeySuffix) {
        return  "cancelTradeOrder:"+bizKeySuffix;
    }
    private CancelTradeOrderVO cancelThirdPartyPayments(String outTradeNo, List<Integer> payTypes) {

        List<CancelTradeOrderTradeStatusBO> tradeStatusBOList = new ArrayList<>();
        for (Integer payType : payTypes) {

            PaymentService paymentService = PaymentServiceFactory.getPaymentService(payType);

            TradeStatusBO tradeStatusBO = paymentService.queryTradeStatus(outTradeNo);
            CancelTradeOrderTradeStatusBO cancelThirdPartyPaymentsBO = new CancelTradeOrderTradeStatusBO();
            cancelThirdPartyPaymentsBO.setPayType(payType);
            cancelThirdPartyPaymentsBO.setOutTradeNo(outTradeNo);
            cancelThirdPartyPaymentsBO.setTradeStatus(tradeStatusBO.getTradeStatus());
            tradeStatusBOList.add(cancelThirdPartyPaymentsBO);
        }
        /**
         * 三方交易单状态 WAIT_BUYER_PAY WAIT_BUYER_PAY
         * 状态下才允许被取消
         *
         */
        List<CancelTradeOrderTradeStatusBO> notExistTradeStatusBOList = tradeStatusBOList.stream().filter(cancelThirdPartyPaymentsBO ->
                TradeStatusEnum.NOT_EXIST.
                        equals(cancelThirdPartyPaymentsBO.getTradeStatus())).collect(Collectors.toList());
        if (notExistTradeStatusBOList.size() == tradeStatusBOList.size()) {
            log.info("所有支付渠道交易单不存在");

            return new CancelTradeOrderVO();
        }

        List<CancelTradeOrderTradeStatusBO> waitPayTradeStatusBOList = tradeStatusBOList.stream().filter(cancelThirdPartyPaymentsBO ->
                TradeStatusEnum.WAIT_BUYER_PAY.
                        equals(cancelThirdPartyPaymentsBO.getTradeStatus())).collect(Collectors.toList());
        if (!waitPayTradeStatusBOList.isEmpty()) {
            /**
             * 取消渠道支付订单状态
             *
             */
            List<OutTradeNoAndPayType> waitPayList = new ArrayList<>();
            waitPayTradeStatusBOList.forEach(a -> {
                OutTradeNoAndPayType outTradeNoAndPayType = new OutTradeNoAndPayType();
                outTradeNoAndPayType.setOutTradeNo(a.getOutTradeNo());
                outTradeNoAndPayType.setPayType(a.getPayType());
                waitPayList.add(outTradeNoAndPayType);
            });
            CancelTradeOrderVO vo = new CancelTradeOrderVO();
            vo.setWaitPayList(waitPayList);

            return vo;
        }
        List<CancelTradeOrderTradeStatusBO> successPayTradeStatusBOList = tradeStatusBOList.stream().filter(cancelThirdPartyPaymentsBO ->
                TradeStatusEnum.TRADE_SUCCESS.
                        equals(cancelThirdPartyPaymentsBO.getTradeStatus())).collect(Collectors.toList());
        if (!successPayTradeStatusBOList.isEmpty()) {
            /**
             * 支付成功的交易单
             *
             */
            List<OutTradeNoAndPayType> successPayList = new ArrayList<>();
            successPayTradeStatusBOList.forEach(a -> {
                OutTradeNoAndPayType outTradeNoAndPayType = new OutTradeNoAndPayType();
                outTradeNoAndPayType.setOutTradeNo(a.getOutTradeNo());
                outTradeNoAndPayType.setPayType(a.getPayType());
                successPayList.add(outTradeNoAndPayType);
            });
            CancelTradeOrderVO vo = new CancelTradeOrderVO();
            vo.setSuccessPayList(successPayList);
            return vo;
        }
        /**
         * 其他场景 抛出异常
         */
        throw new BizException("交易单不允许取消");
    }


    private void updateTradeOrderToCancelled(TradeOrderDO tradeOrderDO) {


    }
    @Transactional
    public void confirmCancelTradeOrder(CancelTradeOrderDTO dto) {

        log.info("confirmCancelTradeOrder:{}", dto);

        String bizKey = buildCancelTradeOrderKey(dto.getBizKeySuffix());
        TradeOrderDO tradeOrderDO = this.lambdaQuery().eq(TradeOrderDO::getId, dto.getOrderId()).one();
        if (tradeOrderDO == null){

            log.error("交易单不存在");
            throw new BizException("交易单不存在");
        }
        boolean operation = tccOperationService.confirmOperation(bizKey);
        if ( !operation){
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

        if (tradeOrderDO == null){
            log.error("交易单不存在");
            throw new BizException("交易单不存在");
        }
        boolean operation = tccOperationService.cancelOperation(bizKey);
        if ( !operation){
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



}
