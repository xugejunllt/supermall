package com.lanf.pay.service;

import com.lanf.common.utils.BeanCopyUtils;
import com.lanf.common.utils.BigDecimalUtil;
import com.lanf.common.utils.DateUtils;
import com.lanf.constant.enums.LogisticsTrackStatusEnum;
import com.lanf.messagemanager.client.service.ISendMqMessageService;
import com.lanf.pay.model.bo.*;
import com.lanf.pay.model.dto.*;
import com.lanf.pay.model.entity.*;
import com.lanf.pay.model.vo.TradeOrderVO;
import com.lanf.pay.model.vo.TransferAccountsVO;
import com.lanf.rocketmq.model.TopicName;
import com.lanf.rocketmq.model.message.LiquidationDTO;
import com.lanf.rocketmq.model.message.LogisticsTrackBathAddDTO;
import com.lanf.rocketmq.model.message.PaySuccessEventMessage;
import com.lanf.rocketmq.util.MessageBuildAdapter;
import com.lanf.rocketmq.util.RocketMqClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Slf4j
public abstract class AbstractPayService implements PayService {


    @Autowired
    private ITradeOrderService tradeOrderService;
    @Autowired
    private IBathPayOrderService bathPayOrderService;
    @Autowired
    private IPayOrderService payOrderService;
    @Autowired
    private RocketMqClient rocketMqClient;
    @Autowired
    private ITransferOrderService transferOrderService;
    @Autowired
    private IRefundOrderService refundOrderService;
    //转账金额最少值
    private BigDecimal transferLowMoney = new BigDecimal(0.1);
    @Autowired
    private ISendMqMessageService sendMqMessageService;

    /**
     * 支付回调通知处理
     *
     */
    @Override
    public void payCallback(PayCallbackDTO dto) {

        //获取支付实现类
        AbstractPayService payService = (AbstractPayService) PayFactory.getPayService(dto.getPayType());
        //解析回调报文，抽象方法
        CallbackResultBO resultVO = payService.parse(dto.getRequest());
        //数据库操作，非抽象方法
        paySuccessHandle(resultVO, dto.getPayType());
        //响应报文给三方支付平台，抽象方法
        payService.callbackResponse(dto.getResponse());

    }

    @Override
    public void patChPayOrder(Long orderId) {

        TradeStatusBO tradeStatusBO = queryTradeStatus(orderId);
        CallbackResultBO callbackResultBO = new CallbackResultBO();

    }

    public void paySuccessHandle(CallbackResultBO resultVO, Integer payType) {
        String outTradeNo = resultVO.getOutTradeNo();
        Boolean bathPay = resultVO.getBathPay();
        //
        BathPayOrderDO payOrderDO = null;
        Long bathPayOrderId = null;
        BigDecimal receiptMoney = null;
        Integer bathPay2 = null;
        List<TradeOrderDO> tradeOrderDOList = null;
        List<PayOrderDO> orderDOList = null;

        if (bathPay) {
            /**
             * 批量付款
             */
            payOrderDO = bathPayOrderService.lambdaQuery().eq(BathPayOrderDO::getBatchNo, outTradeNo).one();
            receiptMoney = resultVO.getReceiptMoney();
            //合并付款 即多笔订单一次付款
            bathPayOrderId = payOrderDO.getId();
            bathPay2 = 0;
            tradeOrderDOList = tradeOrderService.lambdaQuery().eq(TradeOrderDO::getBathPayOrderId, bathPayOrderId).list();
            orderDOList = payOrderService.lambdaQuery().eq(PayOrderDO::getBathPayOrderId, bathPayOrderId).list();

        } else {
            /**
             * 单笔支付
             */
            PayOrderDO orderDO = payOrderService.lambdaQuery().eq(PayOrderDO::getOrderNumber, outTradeNo).one();
            bathPayOrderId = orderDO.getBathPayOrderId();
            BathPayOrderDO bathPayOrderDO = bathPayOrderService.getById(bathPayOrderId);
            TradeOrderDO tradeOrderDO = tradeOrderService.getById(orderDO.getTradeOrderId());
            //累加时收金额
           // receiptMoney = BigDecimalUtils.add(bathPayOrderDO.getReceiptMoney(), resultVO.getReceiptMoney());
            tradeOrderDOList = Arrays.asList(tradeOrderDO);
            orderDOList = Arrays.asList(orderDO);
            bathPay2 = 1;
        }
        /**
         * 构建更新信息
         */
        //构建BathPayOrderDO
        BathPayOrderDO bathPayOrderDOUpdate = new BathPayOrderDO();
        bathPayOrderDOUpdate.setId(bathPayOrderId);
        //bathPayOrderDOUpdate.setReceiptMoney(receiptMoney);
        //构建TradeOrderDO
        List<TradeOrderDO> tradeOrderDOUpdtateList = new ArrayList<>();
        for (TradeOrderDO a : tradeOrderDOList) {
            TradeOrderDO tradeOrderDO = new TradeOrderDO();
            tradeOrderDO.setId(a.getId());
            tradeOrderDO.setPayStatus(1);
            tradeOrderDO.setBathPay(bathPay2);
            tradeOrderDOUpdtateList.add(tradeOrderDO);
        }
        //构建PayOrderDO
        List<PayOrderDO> orderDOUpdateList = new ArrayList<>();
        Date payFinishTime = resultVO.getPayFinishTime();
        orderDOList.forEach(a -> {
            PayOrderDO payOrderDOUpdate = new PayOrderDO();
            payOrderDOUpdate.setId(a.getId());
            payOrderDOUpdate.setPayStatus(1);
            payOrderDOUpdate.setPayFinishTime(payFinishTime);
            payOrderDOUpdate.setTradeFinishTimeFormat(DateUtils.format(payFinishTime, DateUtils.DATE));
            payOrderDOUpdate.setPayAccount(resultVO.getPayAccount());
            payOrderDOUpdate.setIncomeAccount(resultVO.getIncomeAccount());
            payOrderDOUpdate.setNotifyTime(resultVO.getNotifyTime());
            payOrderDOUpdate.setOutTradeNo(outTradeNo);
            payOrderDOUpdate.setTradeNo(resultVO.getTradeNo());
            payOrderDOUpdate.setPayType(payType);
            payOrderDOUpdate.setReceiptMoney(resultVO.getReceiptMoney());

            orderDOUpdateList.add(payOrderDOUpdate);
        });
        /**
         * 进行更新
         */
        bathPayOrderService.updateById(bathPayOrderDOUpdate);
        tradeOrderService.updateBatchById(tradeOrderDOUpdtateList);
        payOrderService.updateBatchById(orderDOUpdateList);
        /**
         * 发送mq
         */
        orderDOList.forEach(a -> {

            String finishContent = "订单支付成功，准备出库";
            Integer source = 0;

            String key = a.getBizOrderId()+":"+finishContent+":"+source;
            PaySuccessEventMessage message = new PaySuccessEventMessage();
            //message.setBizKeyValue(key);
            //订单更新
            message.setOrderId(a.getBizOrderId());
            //进行结算
            LiquidationDTO settlementDTO = new LiquidationDTO();
            settlementDTO.setOrderId(a.getBizOrderId());
            settlementDTO.setPayFinishTime(payFinishTime);
            settlementDTO.setSource(source);
            settlementDTO.setPayMoney(a.getPayMoney());
            settlementDTO.setPlatformAccount(resultVO.getIncomeAccount());
            settlementDTO.setShopId(a.getShopId());
            settlementDTO.setAccountType(payType);
            settlementDTO.setReceiptMoney(resultVO.getReceiptMoney());
            message.setSettlementDTO(settlementDTO);
            //添加物流信息
            LogisticsTrackBathAddDTO logisticsTrackBathAddDTO = MessageBuildAdapter.buildLogisticsTrackAddDTO(a.getBizOrderId(),
                    finishContent, LogisticsTrackStatusEnum.PLACE_AN_ORDER_BUS_INCOME.getCode());
            message.setLogisticsTrackBathAddDTO(logisticsTrackBathAddDTO);

           // sendMqMessageService.sendMessage(TopicName.PAY_SUCCESS_EVENT_TOPIC,message);

        });


    }

    protected abstract void callbackResponse(HttpServletResponse response);

    /**
     * 它是一个抽象方法，让子类实现（支付宝和微信的报文不一样）
     * 解析不同支付类型的回调报文
     *
     */
    protected abstract CallbackResultBO parse(HttpServletRequest request);


    @Override
    public TransferAccountsVO transferAccounts(TransferAccountsDTO dto) {

        log.info("开始进行转账:{}", dto);


        if (BigDecimalUtil.compareTo(dto.getTransAmount(), transferLowMoney) != 1) {
            //转账金额小于最低转账金额
            log.info("转账金额小于最低转账金额");
            TransferAccountsVO transferAccountsVO = new TransferAccountsVO();
            transferAccountsVO.setStatus(1);
            transferAccountsVO.setPayFinishTime(new Date());
            return transferAccountsVO;
        }
        Integer payType = dto.getPayType();


        AbstractPayService payService = (AbstractPayService) PayFactory.getPayService(payType);

        TransferAccountsBO transferAccountsBO = payService.doTransferAccounts(dto);
        log.info("转账完成");
        TransferOrderDO transferOrderDO = new TransferOrderDO();
        BeanCopyUtils.copy(dto, transferOrderDO);
        BeanCopyUtils.copy(transferAccountsBO, transferOrderDO);
        transferOrderService.save(transferOrderDO);
        /**
         * 构建返回信息
         */
        TransferAccountsVO transferAccountsVO = new TransferAccountsVO();
        transferAccountsVO.setPayFinishTime(transferOrderDO.getPayFinishTime());
        transferAccountsVO.setStatus(0);
        return transferAccountsVO;
    }

    public abstract TransferAccountsBO doTransferAccounts(TransferAccountsDTO dto);




    /**
     * 生成预支付单信息
     */
    public abstract TradeOrderVO createTradeOrder(TradeOrderDTO dto);

    @Override
    public TradeOrderVO bathPay(BathPayDTO dto) {


        BathPayOrderDO payOrderDO = bathPayOrderService.lambdaQuery().eq(BathPayOrderDO::getMainOrderId, dto.getMainOrderId()).one();

        TradeOrderDTO tradeOrderDTO = new TradeOrderDTO();
        tradeOrderDTO.setOutTradeNo(payOrderDO.getBatchNo());
        tradeOrderDTO.setTotalAmount(payOrderDO.getBatchFee());
        tradeOrderDTO.setBathPay(true);
        AbstractPayService payService = (AbstractPayService) PayFactory.getPayService(dto.getPayType());

        return payService.createTradeOrder(tradeOrderDTO);
    }

    @Override
    public TradeOrderVO onePay(OnePayDTO dto) {


        PayOrderDO payOrderDO = payOrderService.lambdaQuery().eq(PayOrderDO::getBizOrderId, dto.getOrderId()).one();
        TradeOrderDTO tradeOrderDTO = new TradeOrderDTO();
        tradeOrderDTO.setOutTradeNo(payOrderDO.getOrderNumber());
        tradeOrderDTO.setTotalAmount(payOrderDO.getPayMoney());
        tradeOrderDTO.setBathPay(false);
        AbstractPayService payService = (AbstractPayService) PayFactory.getPayService(dto.getPayType());

        return payService.createTradeOrder(tradeOrderDTO);
    }



    /**
     * 获取支付超时时间 单位分钟
     */
    public int getTimeout() {

        return 30;
    }


    @Override
    public TradeStatusBO queryTradeStatus(Long orderId) {

        PayOrderDO payOrderDO = payOrderService.lambdaQuery().eq(PayOrderDO::getBizOrderId, orderId).one();
        Integer payType = payOrderDO.getPayType();
        AbstractPayService payService = (AbstractPayService) PayFactory.getPayService(payType);
        return payService.startQueryTradeStatus(payOrderDO.getOutTradeNo());

    }

    public abstract TradeStatusBO startQueryTradeStatus(String outTradeNo);

    @Override
    public void cancelTradeOrder(Long orderId) {

        TradeStatusBO tradeStatusBO = queryTradeStatus(orderId);
        Integer tradeStatus = tradeStatusBO.getTradeStatus();
        Boolean exist = tradeStatusBO.getExist();
        if (!exist) {
            log.info("未发起交易");
            return;
        }
        PayOrderDO payOrderDO = payOrderService.lambdaQuery().eq(PayOrderDO::getBizOrderId, orderId).one();

        if (tradeStatus.equals(0)) {
            log.info("交易创建，等待买家付款,关闭订单");
            closeTradeOrder(payOrderDO.getOutTradeNo());

        } else if (tradeStatus.equals(1)) {
            log.info("支付成功,进行退款");

            //转账免费率
            RefundBO bo = new RefundBO();
            bo.setSource(1);
            bo.setToUserType(1);
            bo.setOrderId(orderId);
            refund(bo);

        }

    }

    private void refundLiquidation(PayOrderDO payOrderDO,Date payFinishTime) {
        LiquidationDTO liquidationDTO = new LiquidationDTO();
        liquidationDTO.setOrderId(payOrderDO.getBizOrderId());
        liquidationDTO.setPayFinishTime(payFinishTime);
        liquidationDTO.setSource(2);
        liquidationDTO.setPayMoney(payOrderDO.getPayMoney());
        liquidationDTO.setPlatformAccount(payOrderDO.getIncomeAccount());
        liquidationDTO.setShopId(payOrderDO.getShopId());
        liquidationDTO.setAccountType(payOrderDO.getPayType());
        liquidationDTO.setReceiptMoney(payOrderDO.getReceiptMoney());
        rocketMqClient.sendMessage(TopicName.LIQUIDATION_TOPIC, liquidationDTO);

    }

    @Override
    public void refund(RefundBO refundBO) {

        Long orderId = refundBO.getOrderId();
        PayOrderDO payOrderDO = payOrderService.lambdaQuery().eq(PayOrderDO::getBizOrderId, orderId).one();

         Date payFinishTime = null;
        if ( Boolean.TRUE.equals(refundBO.getTransAccount())) {

            TransferAccountsDTO transferAccountsDTO = buildTransferAccountsDTO(payOrderDO, refundBO);
            AbstractPayService payService = (AbstractPayService) PayFactory.getPayService(payOrderDO.getPayType());
            TransferAccountsVO transferAccountsVO = payService.transferAccounts(transferAccountsDTO);
            payFinishTime = transferAccountsVO.getPayFinishTime();
        } else {

            payFinishTime = new Date();
            AbstractPayService payService = (AbstractPayService) PayFactory.getPayService(payOrderDO.getPayType());

            ReturnMoneyBO returnMoneyBO = payService.returnMoney(payOrderDO.getOutTradeNo(), payOrderDO.getPayMoney(),
                    payOrderDO.getOrderNumber());
            RefundOrderDO refundOrderDO = new RefundOrderDO();
            refundOrderDO.setReturnMoney(returnMoneyBO.getRefundMoney());
            refundOrderDO.setTradeNo(returnMoneyBO.getTradeNo());
            refundOrderDO.setPayOrderId(payOrderDO.getId());
            refundOrderDO.setPayFinishTime(payFinishTime);
            refundOrderService.save(refundOrderDO);

        }
        refundLiquidation(payOrderDO,payFinishTime);
    }

    private TransferAccountsDTO buildTransferAccountsDTO(PayOrderDO payOrderDO, RefundBO refundBO) {

        TransferAccountsDTO transferAccountsDTO = new TransferAccountsDTO();
        transferAccountsDTO.setUserId(payOrderDO.getUserId());
        transferAccountsDTO.setShopId(payOrderDO.getShopId());
        transferAccountsDTO.setSource(refundBO.getSource());
        transferAccountsDTO.setToUserType(refundBO.getToUserType());
        transferAccountsDTO.setOutBizNo(payOrderDO.getOutTradeNo());
        transferAccountsDTO.setToAccountType(0);
        transferAccountsDTO.setPayType(payOrderDO.getPayType());
        //服务费不退回 由用户出
        transferAccountsDTO.setTransAmount(payOrderDO.getReceiptMoney());
        transferAccountsDTO.setCertNo("360726199711144356");
        transferAccountsDTO.setName("刘强");
        transferAccountsDTO.setOrderTitle("取消订单,进行退款");
        transferAccountsDTO.setIncomeAccount("18320911824");


        return transferAccountsDTO;
    }

    public abstract void closeTradeOrder(String outTradeNo);

    public abstract ReturnMoneyBO returnMoney(String outTradeNo, BigDecimal refundAmount,String outRequestNo);


}
