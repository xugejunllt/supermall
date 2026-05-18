package com.lanf.pay.task;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lanf.api.pay.model.enums.TradePurposeEnum;
import com.lanf.common.utils.JsonUtils;
import com.lanf.constant.model.enums.CancelSourceEnum;
import com.lanf.pay.model.entity.TradeOrderDO;
import com.lanf.pay.model.enums.TradeOrderStatusEnum;
import com.lanf.pay.service.pay.IPrepayPayTypeService;
import com.lanf.pay.service.trade.ITradeOrderService;
import com.lanf.rocketmq.model.TopicName;
import com.lanf.rocketmq.model.message.CancelExpiredOrderMessage;
import com.lanf.rocketmq.model.message.CancelOrderMessage;
import com.lanf.rocketmq.util.RocketMqClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;


@Slf4j
@Component
public class CancelExpiredOrderTask {


    @Autowired
    private RocketMqClient rocketMqClient;

    @Autowired
    private ITradeOrderService tradeOrderService;

    @Autowired
    private IPrepayPayTypeService prepayPayTypeService;

    /**
     * 取消超时待支付的交易单
     */
    @Scheduled(cron = "0/5 * * * * *")
    public void cancelExpiredOrderTask() {

        log.info("查询超时未支付的交易单定时任务开始");

        //提前10分钟
        int beforeTime = 10;

        Date beforeTimeDate = new Date(System.currentTimeMillis() + beforeTime * 60 * 1000);
        LambdaQueryChainWrapper<TradeOrderDO> queryChainWrapper = tradeOrderService.lambdaQuery()
                .eq(TradeOrderDO::getPayStatus, TradeOrderStatusEnum.PENDING.getCode())
                .ge(TradeOrderDO::getExpireTime, beforeTimeDate)
                .select(TradeOrderDO::getOrderId);

        //履约时间到期的订单
        double count = queryChainWrapper.count().doubleValue();
        if (count < 1){
            log.info("没有找到待支付的交易单");
            return;
        }
        double pageSize = 2000;
        //向上取整
        double currentTotal = Math.ceil(count / pageSize);
        long currentTotal2 = (long) currentTotal;

        for (long i = 1; i <= currentTotal2; i++) {

            IPage<TradeOrderDO> page = new Page<>(i, (long) pageSize);

            List<TradeOrderDO> records = tradeOrderService.page(page, queryChainWrapper).getRecords();

            for (TradeOrderDO a : records) {
                TradePurposeEnum tradeType = a.getTradePurpose();
                switch (tradeType){
                    case REALTIME_ORDER:
                        CancelExpiredOrderMessage message = new CancelExpiredOrderMessage();
                        message.setOrderId(a.getOrderId());
                        message.setCancelSource(CancelSourceEnum.SYSTEM_TIMEOUT.getCode());
                        rocketMqClient.sendMessage(TopicName.CANCEL_EXPIRED_ORDER_TOPIC,
                                JsonUtils.toJsonString(message));
                        break;
                    case  WALLET_RECHARGE:

                        log.info("取消钱包充值订单");
                        String outTradeNo = a.getOutTradeNo();
                        List<Integer> payTypesByOutTradeNo = prepayPayTypeService.getPayTypesByOutTradeNo(outTradeNo);
                        if (payTypesByOutTradeNo.isEmpty()) {
                            log.info("未查询到支付方式");
                            return;
                        }

                        for (Integer payType : payTypesByOutTradeNo){

                            CancelOrderMessage cancelOrderMessage = new CancelOrderMessage();
                            cancelOrderMessage.setOutTradeNo(outTradeNo);
                            cancelOrderMessage.setPayType(payType);
                            cancelOrderMessage.setCancelSource(null);
                            //取消订单 全部退款时 outRequestNo = outTradeNo
                            cancelOrderMessage.setOutRequestNo(outTradeNo);
                            rocketMqClient.sendMessage(TopicName.CANCEL_PAY_ORDER_TOPIC, JsonUtils.toJsonString(cancelOrderMessage));

                        }
                }

            }


        }

    }

}
