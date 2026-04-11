package com.lanf.pay.service.trade.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.common.utils.BigDecimalUtil;
import com.lanf.constant.exception.BizException;
import com.lanf.constant.result.Result;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.pay.mapper.TradeOrderMapper;
import com.lanf.pay.model.bo.CallbackResultBO;
import com.lanf.pay.model.bo.PaySuccessHandleBO;
import com.lanf.pay.model.bo.PaySuccessHandleResultBO;
import com.lanf.pay.model.dto.*;
import com.lanf.pay.model.entity.*;
import com.lanf.pay.model.enums.BathTradeOrderStatusEnum;
import com.lanf.pay.model.enums.PaySceneEnum;
import com.lanf.pay.model.enums.TradeOrderStatusEnum;
import com.lanf.pay.model.query.TradeOrderBathQuery;
import com.lanf.pay.model.query.TradeOrderQuery;
import com.lanf.pay.model.vo.*;
import com.lanf.pay.service.pay.IPayOrderFlowService;
import com.lanf.pay.service.pay.PaymentService;
import com.lanf.pay.service.pay.PaymentServiceFactory;
import com.lanf.pay.service.trade.IBathTradeOrderService;
import com.lanf.pay.service.trade.IPayOrderService;
import com.lanf.pay.service.trade.ITradeOrderItemService;
import com.lanf.pay.service.trade.ITradeOrderService;
import com.lanf.pay.utils.PryServiceUtils;
import com.lanf.rocketmq.model.message.RefundDTO;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
            //使用成功
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

        //查询用户下单支付的交易单
        TradeOrderDO tradeOrderDO = null;
        if (tradeOrderDO == null) {
            //即使交易没有完成 也不会报错
            return null;
        }

        PayOrderDO payOrderDO = payOrderService.lambdaQuery().eq(PayOrderDO::getBizOrderId, orderId).one();
        if (payOrderDO == null) {
            //即使交易没有完成 也不会报错
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
        //优惠方式
        Integer discountType = null;
        String discountTypeName = null;

        if (discountInfo != null) {
            discountType = discountInfo.getPayType();
            discountTypeName = "优惠券";
        }
        /**
         * 构建返回信息
         */
        OrderTradeVO tradeVO = new OrderTradeVO();
        tradeVO.setOrderId(orderId);
        tradeVO.setPayType(payOrderDO.getPayType());
        tradeVO.setPayMoney(payOrderDO.getPayMoney());
//        tradeVO.setOrderMoney(tradeOrderDO.getOrderMoney());
//        tradeVO.setSource(tradeOrderDO.getSource());
//        tradeVO.setPayFinishTime(payOrderDO.getPayFinishTime());
//        tradeVO.setDiscountMoney(tradeOrderDO.getDiscountMoney());
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

//        Long orderId = query.getOrderId();
//        Integer source = query.getSource();
//
//        TradeOrderDO tradeOrderDO = this.lambdaQuery().eq(TradeOrderDO::getBizOrderId, orderId).eq(TradeOrderDO::getSource, source).one();
//        Long id = tradeOrderDO.getId();
//        PayOrderDO payOrderDO = payOrderService.lambdaQuery().eq(PayOrderDO::getTradeOrderId, id).one();
//
//        TradeOrderApiVO vo = new TradeOrderApiVO();
//        BeanCopyUtils.copy(tradeOrderDO, vo);
//        vo.setIncomeAccount(payOrderDO.getIncomeAccount());
//        vo.setAccountType(payOrderDO.getPayType());
//        vo.setActualPayMoney(payOrderDO.getPayMoney());
//        vo.setReceiptMoney(payOrderDO.getReceiptMoney());
        return null;
    }

    /**
     * 查询待优化
     * 支付完成后，写入中间表
     */
    @Override
    public Integer tradeOrderQuantitySum(TradeOrderQuantitySumDTO dto) {


        List<TradeOrderDO> tradeOrderDOList = this.lambdaQuery().select(BaseEntity::getId).
                //in(TradeOrderDO::getSource, dto.getSources()).
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
        if (!BathTradeOrderStatusEnum.PENDING.getCode().
                equals(tradeOrderDO.getPayStatus())) {
            log.info("交易单状态异常");
            throw new BizException("交易单状态异常");
        }

        PaymentService paymentService = PaymentServiceFactory.getPaymentService(dto.getPayType());
        PrepayOrderDTO prepayOrderDTO = new PrepayOrderDTO();
        prepayOrderDTO.setOutTradeNo(tradeOrderDO.getOutTradeNo());
        prepayOrderDTO.setTotalAmount(tradeOrderDO.getTradeMoney());
        prepayOrderDTO.setBathPay(false);
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
        /**
         * 解析报文
         */
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
        if (handleResultBO.getResponseOk()) {
            paymentService.responsePayOk(dto.getResponse());
        } else {
            paymentService.responsePayFail(dto.getResponse());
        }
    }

    private PaySuccessHandleResultBO paySuccessHandleBO(PaySuccessHandleBO paySuccessHandleBO){

        CallbackResultBO resultBO = paySuccessHandleBO.getResultBO();
        /**
         * 数据校验
         */
        String outTradeNo = resultBO.getOutTradeNo();
        Boolean responseOk = null;
        /**
         * 组合付款单笔付款场景处理
         */
        PaySceneEnum payScene = getPayScene(outTradeNo, resultBO.getBathPay());

        if (PaySceneEnum.SINGLE_ORDER_SINGLE_PAY.equals(payScene)) {
            try {
                handleSinglePayScene( outTradeNo, resultBO, responseOk,paySuccessHandleBO.getPayType());
            } catch (BizException ignored) {
                /**
                 * 抛异常只为让事务进行回滚
                 */
            }

        }
        if (PaySceneEnum.COMBINED_PAY.equals(payScene)) {
            try {
                handleCombinedPayScene( outTradeNo, resultBO, responseOk,paySuccessHandleBO.getPayType());
            } catch (BizException ignored) {
                /**
                 * 抛异常只为让事务进行回滚
                 */
            }
        }
        if (PaySceneEnum.COMBINED_TO_SINGLE_PAY.equals(payScene)) {
            try {
                handleCombinedToSinglePayScene( outTradeNo, resultBO,responseOk, paySuccessHandleBO.getPayType());
            } catch (BizException ignored) {
                /**
                 * 抛异常只为让事务进行回滚
                 */
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
    public void handleSinglePayScene( String outTradeNo, CallbackResultBO resultBO, Boolean responseOk,Integer payType) {
        TradeOrderDO tradeOrderDO = this.lambdaQuery()
                .eq(TradeOrderDO::getOutTradeNo, outTradeNo)
                .one();

        if (tradeOrderDO == null) {
            log.error("交易单不存在outTradeNo:[{}]", outTradeNo);
            responseOk = false;
            return;
        }

        BigDecimal totalAmount = resultBO.getTotalAmount();
        BigDecimal tradeMoney = tradeOrderDO.getTradeMoney();
        if (!totalAmount.equals(tradeMoney)) {
            log.error("交易金额异常 outTradeNo:[{}],totalAmount[{}],tradeMoney[{}]", outTradeNo, totalAmount, tradeMoney);
            responseOk = false;
            return;
        }
        if (TradeOrderStatusEnum.CANCELLED.getCode().
                equals(tradeOrderDO.getPayStatus())) {
            log.info("交易单已取消");
            responseOk = true;
            return;
        }
        PayOrderFlowDO flowDO = payOrderFlowService.lambdaQuery()
                .eq(PayOrderFlowDO::getOutTradeNo, outTradeNo)
                .eq(PayOrderFlowDO::getPayType, payType).one();


        if (TradeOrderStatusEnum.COMPLETED.getCode()
                .equals(tradeOrderDO.getPayStatus()) && flowDO != null) {
            log.info("交易单支付成功 outTradeNo:[{}]", outTradeNo);
            responseOk = true;
            return;
        }
        if (TradeOrderStatusEnum.COMPLETED.getCode()
                .equals(tradeOrderDO.getPayStatus()) && flowDO == null) {
            /**
             * 发生多次支付 这里进行退款
             */

        }


        if (BathTradeOrderStatusEnum.MERGE_TRANSFER_SINGLE.getCode().
                equals(tradeOrderDO.getPayStatus())) {
            /**
             * 已进行合并付款已单笔付款 这里进行合并付款退款
             */

        }

        PayOrderFlowDO payOrderFlowDO = buildPayOrderFlowDO(payType, resultBO);
        boolean update = this.lambdaUpdate()
                .eq(BaseEntity::getId, tradeOrderDO.getId())
                .eq(TradeOrderDO::getVersion, tradeOrderDO.getVersion())
                .set(TradeOrderDO::getPayStatus, BathTradeOrderStatusEnum.COMPLETED.getCode())
                .set(TradeOrderDO::getVersion, tradeOrderDO.getVersion() + 1)
                .update();
        if (!update) {
            log.info("交易单已支付");
            responseOk = true;
            throw new BizException("交易单已支付");
        }
        payOrderFlowService.save(payOrderFlowDO);


    }

    @Transactional
    public void handleCombinedPayScene( String outTradeNo, CallbackResultBO resultBO, Boolean responseOk,Integer payType) {
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
        Integer payStatus = bathTradeOrderDO.getPayStatus();

        PayOrderFlowDO flowDO = payOrderFlowService.lambdaQuery()
                .eq(PayOrderFlowDO::getOutTradeNo, outTradeNo)
                .eq(PayOrderFlowDO::getPayType, payType).one();


        if (BathTradeOrderStatusEnum.COMPLETED.getCode()
                .equals(bathTradeOrderDO.getPayStatus()) && flowDO != null) {
            log.info("批量交易单支付成功 outTradeNo:[{}]", outTradeNo);
            responseOk = true;
            return;
        }
        if (BathTradeOrderStatusEnum.COMPLETED.getCode()
                .equals(bathTradeOrderDO.getPayStatus()) && flowDO == null) {

            /**
             * 发生多渠道多次支付 这里进行退款
             */

            return;
        }







        if (BathTradeOrderStatusEnum.MERGE_TRANSFER_SINGLE.getCode().equals(payStatus) ) {
            /**
             * 退款 合并付款单 幂等退款
             */
            responseOk = true;
            return;
        }
        PayOrderFlowDO payOrderFlowDO = buildPayOrderFlowDO(payType, resultBO);
        boolean update1 = bathTradeOrderService.lambdaUpdate()
                .eq(BaseEntity::getId, bathTradeOrderDO.getId())
                .eq(BathTradeOrderDO::getVersion, bathTradeOrderDO.getVersion())
                .set(BathTradeOrderDO::getPayStatus, BathTradeOrderStatusEnum.COMPLETED.getCode())
                .set(BathTradeOrderDO::getVersion, bathTradeOrderDO.getVersion() + 1)
                .update();
        if (!update1) {
            log.info("批量交易单已支付");
            responseOk = true;

        }
        List<TradeOrderDO> tradeOrderDOList = this.lambdaQuery().
                eq(TradeOrderDO::getBathPayOrderId, bathTradeOrderDO.getId()).list();

        if (!tradeOrderDOList.isEmpty()) {
            List<Long> ids = tradeOrderDOList.stream()
                    .map(TradeOrderDO::getId)
                    .collect(Collectors.toList());
            this.lambdaUpdate()
                    .in(BaseEntity::getId, ids)
                    .set(TradeOrderDO::getPayStatus, BathTradeOrderStatusEnum.COMPLETED.getCode())
                    .setSql("version = version + 1")
                    .update();
        }
        payOrderFlowService.save(payOrderFlowDO);


    }


// ... existing code ...

    @Transactional
    public void handleCombinedToSinglePayScene( String outTradeNo, CallbackResultBO resultBO, Boolean responseOk,Integer payType) {
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
        if ( BathTradeOrderStatusEnum.COMPLETED.getCode().equals(orderDO.getPayStatus())) {

            /**
             * 组合单已付款 进行单笔退款  幂等退款
             */

            responseOk = true;
            return;
        }

        PayOrderFlowDO flowDO = payOrderFlowService.lambdaQuery()
                .eq(PayOrderFlowDO::getOutTradeNo, outTradeNo)
                .eq(PayOrderFlowDO::getPayType, payType).one();

        if (TradeOrderStatusEnum.COMPLETED.getCode()
                .equals(tradeOrderDO.getPayStatus()) && flowDO != null) {
            log.info("交易单支付成功 outTradeNo:[{}]", outTradeNo);
            responseOk = true;
            return;
        }
        if (TradeOrderStatusEnum.COMPLETED.getCode()
                .equals(tradeOrderDO.getPayStatus()) && flowDO == null) {
            /**
             * 发生多渠道多次支付 这里进行退款
             */
            return;
        }

        if ( TradeOrderStatusEnum.CANCELLED.getCode().equals(orderDO.getPayStatus())) {
            log.info("交易已取消");
            responseOk = true;
            return;
        }

        if ( BathTradeOrderStatusEnum.PENDING.getCode().equals(orderDO.getPayStatus())) {
           log.info("更新批量交易单状态");
            /**
             * 其中一笔交易单完成 那么就更新为已支付
             * 允许多线程并发更新失败 但不影响子交易单的更新
             */
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
                .set(TradeOrderDO::getPayStatus, BathTradeOrderStatusEnum.COMPLETED.getCode())
                .set(TradeOrderDO::getVersion, tradeOrderDO.getVersion() + 1)
                .update();
        if (!update) {
            log.info("交易单已支付");
            responseOk = true;
            throw new BizException("交易单已支付");
        }
        payOrderFlowService.save(payOrderFlowDO);

    }

// ... existing code ...

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
    @Override
    public void payCompensateOrder(String outTradeNo) {

       // 获取交易单
        //TradeOrderDO tradeOrderDO = this.lambdaQuery();



    }
















}
