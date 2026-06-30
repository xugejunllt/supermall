package com.lanf.pay.service.trade.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.api.pay.model.dto.CreatePayOrderDTO;
import com.lanf.api.pay.model.dto.CreateTradeOrderDTO;
import com.lanf.api.pay.model.dto.TradeOrderQuantitySumDTO;
import com.lanf.api.pay.model.enums.TradePurposeEnum;
import com.lanf.api.pay.model.query.TradeOrderBathQuery;
import com.lanf.api.pay.model.query.TradeOrderPageQuery;
import com.lanf.api.pay.model.query.TradeOrderQuery;
import com.lanf.api.pay.model.vo.CreatePayOrderVO;
import com.lanf.api.pay.model.vo.TradeOrderApiVO;
import com.lanf.api.pay.model.vo.TradeOrderBathVO;
import com.lanf.api.pay.model.vo.TradeOrderPageVO;
import com.lanf.common.utils.BeanCopyUtils;
import com.lanf.common.utils.CodeGenerateUtils;
import com.lanf.common.utils.DateUtils;
import com.lanf.common.utils.JsonUtils;
import com.lanf.constant.exception.BizException;
import com.lanf.constant.model.enums.FlowNoPrefixEnum;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.constant.utils.IdUtils;
import com.lanf.constant.utils.UserContext;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.pay.config.PayConfig;
import com.lanf.pay.mapper.TradeOrderMapper;
import com.lanf.pay.model.bo.PassbackParams;
import com.lanf.pay.model.bo.PayCompensateOrderRetryPolicyBO;
import com.lanf.pay.model.dto.BathCreatePrepayOrderDTO;
import com.lanf.pay.model.dto.CreatePrepayOrderDTO;
import com.lanf.pay.model.dto.PrepayOrderDTO;
import com.lanf.pay.model.dto.RechargeDTO;
import com.lanf.pay.model.entity.BathTradeOrderDO;
import com.lanf.pay.model.entity.PayOrderDO;
import com.lanf.pay.model.entity.PrepayPayTypeDO;
import com.lanf.pay.model.entity.TradeOrderDO;
import com.lanf.pay.model.enums.BathTradeOrderStatusEnum;
import com.lanf.pay.model.enums.TradeOrderStatusEnum;
import com.lanf.pay.model.vo.CreatePrepayOrderVO;
import com.lanf.pay.model.vo.CreateRechargeTradeOrderVO;
import com.lanf.pay.model.vo.PrepayOrderVO;
import com.lanf.pay.service.pay.IPayOrderFlowService;
import com.lanf.pay.service.pay.IPrepayPayTypeService;
import com.lanf.pay.service.pay.PaymentService;
import com.lanf.pay.service.pay.PaymentServiceFactory;
import com.lanf.pay.service.trade.IBathTradeOrderService;
import com.lanf.pay.service.trade.IPayOrderService;
import com.lanf.pay.service.trade.ITradeOrderService;
import com.lanf.pay.utils.PayServiceUtils;
import com.lanf.rocketmq.model.TopicName;
import com.lanf.rocketmq.model.message.CompensatePaymentOrderMessage;
import com.lanf.rocketmq.model.message.RefundDTO;
import com.lanf.rocketmq.util.RocketMqClient;
import com.lanf.tcc.service.ITccOperationService;
import lombok.extern.slf4j.Slf4j;
import org.dromara.hmily.annotation.HmilyTCC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
    @Autowired
    private PaymentServiceFactory paymentServiceFactory;

    private static final String PREPAY_PAY_TYPE_CACHE_KEY = "prepay_pay_type:%s";

    private static final long CACHE_EXPIRE_TIME = 30L;


    @HmilyTCC(confirmMethod = "confirmCreateTradeOrder", cancelMethod = "cancelCreateTradeOrder")
    @Override
    public void createTradeOrder(CreateTradeOrderDTO dto) {

    }


    @Transactional
    public void confirmCreateTradeOrder(CreateTradeOrderDTO dto) {

        try {
            log.info("插入交易单:{}", dto);
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
        } catch (Exception e) {
            log.error("插入交易单异常", e);
            throw e;
        }

    }

    public void cancelCreateTradeOrder(CreateTradeOrderDTO dto) {

        log.info("cancelCreateTradeOrder");

    }

    private TradeOrderDO buildTradeOrderDO(CreateTradeOrderDTO dto) {


        String outTradeNo = CodeGenerateUtils.generateFlowNo(FlowNoPrefixEnum.TRADE_ORDER,
                dto.getOrderNumber());
        log.info("过期时间是{}", payConfig.getExpireInterval());
        Date expireTime = DateUtils.addMinutes(new Date(), payConfig.getExpireInterval().longValue());
        Long id = IdUtils.generateId();
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
        tradeOrderDO.setTradePurpose(TradePurposeEnum.REALTIME_ORDER);
        PassbackParams passbackParams = new PassbackParams();
        passbackParams.setBathPay(false);
        passbackParams.setTradeOrderId(id);
        passbackParams.setTradeType(TradePurposeEnum.REALTIME_ORDER);
        passbackParams.setTradeMoney(dto.getTradeMoney());
        passbackParams.setSignValue(PayServiceUtils.generateSign(passbackParams));
        tradeOrderDO.setPassBackParams(JsonUtils.toJsonString(passbackParams));
        return tradeOrderDO;
    }


    @Override
    public CreatePayOrderVO createPayOrder(List<CreatePayOrderDTO> dto) {


        return null;
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
        Date expireTime = tradeOrderDO.getExpireTime();
        if (expireTime.before(new Date())) {
            log.warn("交易单已过期");
            throw new BizException("交易单已过期");
        }


        Integer payType = dto.getPayType();
        String orderStr = null;

        PayCompensateOrderRetryPolicyBO firstLevelRetryPolicy = payRetryPolicyCacheService.getFirstLevelRetryPolicy();
        CompensatePaymentOrderMessage message = new CompensatePaymentOrderMessage();
        message.setOutTradeNo(tradeOrderDO.getOutTradeNo());
        message.setPayType(payType);
        message.setRetryLevel(firstLevelRetryPolicy.getRetryLevel());
        message.setBathOrder(false);

        CreatePrepayOrderVO vo = new CreatePrepayOrderVO();


        PrepayPayTypeDO existingRecord = prepayPayTypeService.lambdaQuery()
                .eq(PrepayPayTypeDO::getOutTradeNo, tradeOrderDO.getOutTradeNo())
                .eq(PrepayPayTypeDO::getPayType, payType)
                .one();

        if (existingRecord != null) {
            orderStr = existingRecord.getOrderStr();
            vo.setOrderStr(orderStr);

        } else {
            orderStr =  savePrepayPayTypeDO( tradeOrderDO,  payType);
            vo.setOrderStr(orderStr);
        }

        rocketMqClient.sendDelayMessage(TopicName.COMPENSATE_PAYMENT_TOPIC,
                JsonUtils.toJsonString(message), TimeUnit.SECONDS, firstLevelRetryPolicy.getDelaySeconds());

        return vo;
    }

    private String savePrepayPayTypeDO(TradeOrderDO tradeOrderDO, Integer payType) {

        PaymentService paymentService = paymentServiceFactory.getPaymentService(payType);
        PassbackParams passbackParams = JsonUtils.toObject(tradeOrderDO.getPassBackParams(),
                PassbackParams.class);
        PrepayOrderDTO prepayOrderDTO = new PrepayOrderDTO();
        prepayOrderDTO.setOutTradeNo(tradeOrderDO.getOutTradeNo());
        prepayOrderDTO.setTotalAmount(tradeOrderDO.getTradeMoney());
        prepayOrderDTO.setExpireInterval(tradeOrderDO.getExpireInterval());
        prepayOrderDTO.setPassbackParams(passbackParams);
        PrepayOrderVO prepayOrderVO = paymentService.createPrepayOrder(prepayOrderDTO);
        String orderStr = prepayOrderVO.getOrderStr();

        PrepayPayTypeDO prepayPayTypeDO = new PrepayPayTypeDO();
        prepayPayTypeDO.setOutTradeNo(tradeOrderDO.getOutTradeNo());
        prepayPayTypeDO.setPayType(payType);
        prepayPayTypeDO.setOrderStr(orderStr);
        try {
            prepayPayTypeService.save(prepayPayTypeDO);

        } catch (DuplicateKeyException e) {
            PrepayPayTypeDO existingRecord = prepayPayTypeService.lambdaQuery()
                    .eq(PrepayPayTypeDO::getOutTradeNo, tradeOrderDO.getOutTradeNo())
                    .eq(PrepayPayTypeDO::getPayType, payType)
                    .one();
            orderStr = existingRecord.getOrderStr();
        }
        return orderStr;
    }
    private String savePrepayPayTypeDO(BathTradeOrderDO bathTradeOrderDO, Integer payType) {

        PaymentService paymentService = paymentServiceFactory.getPaymentService(payType);
        PassbackParams passbackParams = JsonUtils.toObject(bathTradeOrderDO.getPassbackParams(),
                PassbackParams.class);
        PrepayOrderDTO prepayOrderDTO = new PrepayOrderDTO();
        prepayOrderDTO.setOutTradeNo(bathTradeOrderDO.getOutTradeNo());
        prepayOrderDTO.setTotalAmount(bathTradeOrderDO.getBatchFee());
        prepayOrderDTO.setExpireInterval(bathTradeOrderDO.getExpireInterval());
        prepayOrderDTO.setPassbackParams(passbackParams);
        PrepayOrderVO prepayOrderVO = paymentService.createPrepayOrder(prepayOrderDTO);
        String orderStr = prepayOrderVO.getOrderStr();

        PrepayPayTypeDO prepayPayTypeDO = new PrepayPayTypeDO();
        prepayPayTypeDO.setOutTradeNo(bathTradeOrderDO.getOutTradeNo());
        prepayPayTypeDO.setPayType(payType);
        prepayPayTypeDO.setOrderStr(orderStr);
        try {
            prepayPayTypeService.save(prepayPayTypeDO);

        } catch (DuplicateKeyException e) {
            PrepayPayTypeDO existingRecord = prepayPayTypeService.lambdaQuery()
                    .eq(PrepayPayTypeDO::getOutTradeNo, bathTradeOrderDO.getOutTradeNo())
                    .eq(PrepayPayTypeDO::getPayType, payType)
                    .one();
            orderStr = existingRecord.getOrderStr();
        }
        return orderStr;
    }

    public CreatePrepayOrderVO bathCreatePrepayOrder(BathCreatePrepayOrderDTO dto) {

        BathTradeOrderDO bathTradeOrderDO = bathTradeOrderService.lambdaQuery()
                .eq(BathTradeOrderDO::getMainOrderNumber, dto.getMainOrderNumber()).one();

        if (bathTradeOrderDO == null) {
            log.warn("批量交易单不存在");
            throw new BizException("批量交易单不存在");
        }
        if (!BathTradeOrderStatusEnum.PENDING.getCode().
                equals(bathTradeOrderDO.getPayStatus())) {
            log.warn("交易单状态异常");
            throw new BizException("交易单状态异常");
        }
        Date expireTime = bathTradeOrderDO.getExpireTime();
        if (expireTime.before(new Date())) {
            log.warn("交易单已过期");
            throw new BizException("交易单已过期");
        }



        Integer payType = dto.getPayType();
        String orderStr = null;

        PayCompensateOrderRetryPolicyBO firstLevelRetryPolicy = payRetryPolicyCacheService.getFirstLevelRetryPolicy();
        CompensatePaymentOrderMessage message = new CompensatePaymentOrderMessage();
        message.setOutTradeNo(bathTradeOrderDO.getOutTradeNo());
        message.setPayType(payType);
        message.setRetryLevel(firstLevelRetryPolicy.getRetryLevel());
        message.setBathOrder(true);

        CreatePrepayOrderVO vo = new CreatePrepayOrderVO();


        PrepayPayTypeDO existingRecord = prepayPayTypeService.lambdaQuery()
                .eq(PrepayPayTypeDO::getOutTradeNo, bathTradeOrderDO.getOutTradeNo())
                .eq(PrepayPayTypeDO::getPayType, payType)
                .one();

        if (existingRecord != null) {
            orderStr = existingRecord.getOrderStr();
            vo.setOrderStr(orderStr);

        } else {
            orderStr =  savePrepayPayTypeDO( bathTradeOrderDO,  payType);
            vo.setOrderStr(orderStr);
        }

        rocketMqClient.sendDelayMessage(TopicName.COMPENSATE_PAYMENT_TOPIC,
                JsonUtils.toJsonString(message), TimeUnit.SECONDS, firstLevelRetryPolicy.getDelaySeconds());

        return vo;
    }

    @Override
    public PageResult<TradeOrderPageVO> tradeOrderPageQuery(TradeOrderPageQuery query) {
        Page<TradeOrderDO> page = this.page(new Page<>(query.getPage(), query.getPageSize()),
                new LambdaQueryWrapper<TradeOrderDO>()
                        .orderByDesc(TradeOrderDO::getCreateTime));
        List<TradeOrderDO> records = page.getRecords();
        if (records.isEmpty()) {
            return null;
        }
        List<TradeOrderPageVO> pageVOS = BeanCopyUtils.copyBeanList(records, TradeOrderPageVO.class);
        PageResult<TradeOrderPageVO> result = new PageResult<>();
        result.setTotal(page.getTotal());
        result.setRecords(pageVOS);
        result.setSize(page.getSize());
        return result;
    }

    @Override
    public CreateRechargeTradeOrderVO createRechargeTradeOrder(RechargeDTO dto) {

        Date expireTime = DateUtils.addMinutes(new Date(), payConfig.getExpireInterval().longValue());
        String outTradeNo = CodeGenerateUtils.generateFlowNo(FlowNoPrefixEnum.TRADE_ORDER, dto.getOrderNumber());
        TradeOrderDO tradeOrderDO = new TradeOrderDO();
        tradeOrderDO.setUserId(UserContext.getUserId());
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
        tradeOrderDO.setPassBackParams(params);

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
