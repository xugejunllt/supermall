package com.lanf.order.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.order.mapper.PromiseOrderMapper;
import com.lanf.order.model.entity.PromiseOrderDO;
import com.lanf.order.service.IPromiseOrderService;
import com.lanf.pay.api.PayApiService;
import com.lanf.pay.model.vo.OrderTradeVO;
import com.lanf.rocketmq.model.TopicName;
import com.lanf.rocketmq.model.message.LiquidationDTO;
import com.lanf.rocketmq.util.RocketMqClient;
import com.lanf.constant.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * <p>
 * 履约单 服务实现类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-08-29
 */
@Slf4j
@Service
public class PromiseOrderServiceImpl extends ServiceImpl<PromiseOrderMapper, PromiseOrderDO> implements IPromiseOrderService {


    @Autowired
    private PayApiService payApiService;
    @Autowired
    private RocketMqClient rocketMqClient;

    @Override
    public void returnMoney(Long orderId) {


        PromiseOrderDO one = this.lambdaQuery().eq(PromiseOrderDO::getOrderId, orderId).one();
        if (one.getReturnMoney().equals(1)) {
            throw new BizException("履约单已退款");
        }

        this.lambdaUpdate().eq(PromiseOrderDO::getOrderId, orderId).
                set(PromiseOrderDO::getReturnMoney, 1).
                set(BaseEntity::getUpdateTime, new Date());

    }

    @Override
    public void promiseOrderLiquidation(Long orderId) {

        PromiseOrderDO one = this.lambdaQuery().
                eq(PromiseOrderDO::getOrderId, orderId).
                one();
        if (one.getLiquidationStatus().equals(1)) {
            log.info("履约单已结算");
            return;
        }
        boolean result = this.lambdaUpdate().
                eq(PromiseOrderDO::getOrderId, orderId).
                set(PromiseOrderDO::getLiquidationStatus, 1).
                update();
        if (!result) {
            throw new BizException("更新失败");
        }
        LiquidationDTO liquidationDTO = buildLiquidationDTO(orderId);
        rocketMqClient.sendMessage(TopicName.LIQUIDATION_TOPIC, liquidationDTO);
    }

    private LiquidationDTO buildLiquidationDTO(Long orderId) {

        OrderTradeVO tradeVO = payApiService.queryOrderTradeByOrderId(orderId).getData();
        LiquidationDTO liquidationDTO = new LiquidationDTO();
        liquidationDTO.setOrderId(orderId);
        liquidationDTO.setPayFinishTime(tradeVO.getPayFinishTime());
        liquidationDTO.setSource(1);
        liquidationDTO.setPayMoney(tradeVO.getPayMoney());
        liquidationDTO.setPlatformAccount(tradeVO.getIncomeAccount());
        liquidationDTO.setShopId(tradeVO.getShopId());
        liquidationDTO.setAccountType(tradeVO.getPayType());
        liquidationDTO.setReceiptMoney(tradeVO.getReceiptMoney());
        return liquidationDTO;
    }
}
