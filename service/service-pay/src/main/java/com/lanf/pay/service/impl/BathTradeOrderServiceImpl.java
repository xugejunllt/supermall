package com.lanf.pay.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.common.utils.BigDecimalUtil;
import com.lanf.pay.mapper.BathTradeOrderMapper;
import com.lanf.pay.model.dto.CreateMergeTradeOrderDTO;
import com.lanf.pay.model.dto.CreateMergeTradeOrderItemDTO;
import com.lanf.pay.model.entity.BathTradeOrderDO;
import com.lanf.pay.model.vo.CreateMergeTradeOrderVO;
import com.lanf.pay.service.IBathTradeOrderService;
import com.lanf.pay.utils.PryServiceUtils;
import lombok.extern.slf4j.Slf4j;
import org.dromara.hmily.annotation.HmilyTCC;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

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


    @HmilyTCC(confirmMethod = "confirmCreateMergeTradeOrder", cancelMethod = "cancelCreateMergeTradeOrder")
    @Override
    public CreateMergeTradeOrderVO createMergeTradeOrder(CreateMergeTradeOrderDTO dto) {




        return null;
    }

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
        BathTradeOrderDO bathTradeOrderDO1 = buildBathTradeOrderDO(dto);
        z
        //创建交易单

    }

    private BathTradeOrderDO buildBathTradeOrderDO(CreateMergeTradeOrderDTO dto) {
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
        bathTradeOrderDO1.setUserId(dto.getUserId());
        bathTradeOrderDO1.setMainOrderId(dto.getMainOrderId());
        bathTradeOrderDO1.setBatchNo(batchNo);
        bathTradeOrderDO1.setBatchNum(batchNum);
        bathTradeOrderDO1.setBatchFee(batchFee);
        bathTradeOrderDO1.setCancelMerge(0);
        return bathTradeOrderDO1;
    }


    public void cancelCreateMergeTradeOrder(CreateMergeTradeOrderDTO dto) {
        log.info("cancelCreateMergeTradeOrder:{}",dto);



    }


}
