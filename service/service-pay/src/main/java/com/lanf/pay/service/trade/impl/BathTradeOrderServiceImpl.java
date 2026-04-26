package com.lanf.pay.service.trade.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.common.utils.BigDecimalUtil;
import com.lanf.common.utils.DateUtils;
import com.lanf.common.utils.IdUtils;
import com.lanf.constant.exception.BizException;
import com.lanf.pay.mapper.BathTradeOrderMapper;
import com.lanf.pay.model.dto.BathCreatePrepayOrderDTO;
import com.lanf.client.pay.model.dto.CreateMergeTradeOrderDTO;
import com.lanf.client.pay.model.dto.CreateMergeTradeOrderItemDTO;
import com.lanf.pay.model.dto.PrepayOrderDTO;
import com.lanf.pay.model.entity.BathTradeOrderDO;
import com.lanf.pay.model.entity.TradeOrderDO;
import com.lanf.pay.model.enums.BathTradeOrderStatusEnum;
import com.lanf.pay.model.vo.CreatePrepayOrderVO;
import com.lanf.pay.model.vo.PrepayOrderVO;
import com.lanf.pay.service.pay.PaymentService;
import com.lanf.pay.service.pay.PaymentServiceFactory;
import com.lanf.pay.service.pay.config.PayConfig;
import com.lanf.pay.service.trade.IBathTradeOrderService;
import com.lanf.pay.service.trade.ITradeOrderService;
import com.lanf.pay.utils.PryServiceUtils;
import lombok.extern.slf4j.Slf4j;
import org.dromara.hmily.annotation.HmilyTCC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 批量交易订单 服务实现类
 * </p>
 *
 * @author jarven
 * @since 2025-12-28
 */
@Slf4j
@Service
public class BathTradeOrderServiceImpl extends ServiceImpl<BathTradeOrderMapper, BathTradeOrderDO> implements IBathTradeOrderService {


    @Autowired
    private ITradeOrderService tradeOrderService;
    @Autowired
    private PayConfig payConfig;

    @HmilyTCC(confirmMethod = "confirmCreateMergeTradeOrder", cancelMethod = "cancelCreateMergeTradeOrder")
    @Override
    public void createMergeTradeOrder(CreateMergeTradeOrderDTO dto) {





    }



    @Transactional
    public void confirmCreateMergeTradeOrder(CreateMergeTradeOrderDTO dto) {
        log.info("confirmCreateMergeTradeOrder:{}",dto);

        Long mainOrderId = dto.getMainOrderId();
        BathTradeOrderDO bathTradeOrderDO = this.lambdaQuery().eq(BathTradeOrderDO::getMainOrderId, mainOrderId).one();

        if (bathTradeOrderDO != null) {
            log.info("批量交易单已存在");
            return;
        }

        /**
         * 构建BathTradeOrderDO
         */
        Date expireTime = DateUtils.addMinutes(new Date(), payConfig.getExpireInterval().longValue());
        BathTradeOrderDO bathTradeOrderDO1 = buildBathTradeOrderDO(dto, expireTime);

        /**
         *
         * 创建交易单
         */
        List<TradeOrderDO> tradeOrderDOList = buildTradeOrderDOList(dto,bathTradeOrderDO,expireTime);
        try {
            this.save(bathTradeOrderDO1);
        } catch (DuplicateKeyException e) {
            log.info("批量交易单已存在");
            return;
        }

        tradeOrderService.saveBatch(tradeOrderDOList);



    }

    private List<TradeOrderDO> buildTradeOrderDOList(CreateMergeTradeOrderDTO dto, BathTradeOrderDO bathTradeOrderDO,Date expireTime) {
        return dto.getTradeOrderItemList().stream()
                .map(item -> {
                    String outTradeNo = PryServiceUtils.generateOutTradeNo(item.getOrderId());
                    TradeOrderDO tradeOrderDO = new TradeOrderDO();
                    tradeOrderDO.setBathPayOrderId(bathTradeOrderDO.getId());
                    tradeOrderDO.setUserId(dto.getUserId());
                    tradeOrderDO.setOrderId(item.getOrderId());
                    tradeOrderDO.setOutTradeNo(outTradeNo);
                    tradeOrderDO.setTradeMoney(item.getTradeMoney());
                    tradeOrderDO.setPayStatus(0);
                    tradeOrderDO.setBathPay(1);
                    tradeOrderDO.setExpireTime(expireTime);
                    tradeOrderDO.setBusinessId(dto.getBusinessId());
                    return tradeOrderDO;
                })
                .collect(Collectors.toList());
    }
    private BathTradeOrderDO buildBathTradeOrderDO(CreateMergeTradeOrderDTO dto, Date expireTime) {
        String batchNo =  PryServiceUtils.generateOutTradeNo(dto.getMainOrderId());
        Integer batchNum = dto.getTradeOrderItemList().size();

        /**
         * reduce: 这里的第一个参数是 BigDecimal.ZERO，它的意义是：
         * 初始值： BigDecimal.ZERO 是 reduce 操作的初始值。
         * 它表示在开始累加之前，结果的默认值为 0。如果没有这个初始值，
         * reduce 会返回一个 Optional<BigDecimal>，因为流可能为空。
         * 避免空指针异常： 如果流为空（即 dto.getTradeOrderItemList()
         * 为空或没有元素），reduce 会直接返回初始值 BigDecimal.ZERO，而不会抛出异常。
         *
         */
        BigDecimal batchFee = dto.getTradeOrderItemList().stream().
                map(CreateMergeTradeOrderItemDTO::getTradeMoney).
                reduce(BigDecimal.ZERO, BigDecimalUtil::add);

        BathTradeOrderDO bathTradeOrderDO1 = new BathTradeOrderDO();
        bathTradeOrderDO1.setId(IdUtils.generateId());
        bathTradeOrderDO1.setUserId(dto.getUserId());
        bathTradeOrderDO1.setMainOrderId(dto.getMainOrderId());
        bathTradeOrderDO1.setOutTradeNo(batchNo);
        bathTradeOrderDO1.setBatchNum(batchNum);
        bathTradeOrderDO1.setBatchFee(batchFee);
        bathTradeOrderDO1.setMainOrderNumber(dto.getMainOrderNumber());
        bathTradeOrderDO1.setExpireInterval(payConfig.getExpireInterval());
        bathTradeOrderDO1.setExpireTime(expireTime);
        bathTradeOrderDO1.setBusinessId(dto.getBusinessId());
        return bathTradeOrderDO1;
    }


    public void cancelCreateMergeTradeOrder(CreateMergeTradeOrderDTO dto) {
        log.info("cancelCreateMergeTradeOrder:{}",dto);



    }
    @Override
    public CreatePrepayOrderVO bathCreatePrepayOrder(BathCreatePrepayOrderDTO dto) {

        BathTradeOrderDO bathTradeOrderDO = this.lambdaQuery().eq(BathTradeOrderDO::getMainOrderId, dto.getMainOrderId()).one();
        if (bathTradeOrderDO == null) {
            log.warn("批量交易单不存在");
            throw new BizException("批量交易单不存在");
        }
        if ( !BathTradeOrderStatusEnum.PENDING.getCode().
                equals(bathTradeOrderDO.getPayStatus())){
            log.warn("交易单状态异常");
            throw new BizException("交易单状态异常");
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

}
