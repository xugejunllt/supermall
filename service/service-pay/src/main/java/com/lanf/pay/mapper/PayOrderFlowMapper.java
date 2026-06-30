package com.lanf.pay.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lanf.pay.model.entity.PayOrderFlowDO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

/**
 * <p>
 * 支付流水 Mapper 接口
 * </p>
 *
 * @author jarven
 * @since 2025-12-28
 */
public interface PayOrderFlowMapper extends BaseMapper<PayOrderFlowDO> {

    /**
     * 根据日期统计实收金额
     */
    @Select("SELECT COALESCE(SUM(receipt_money), 0) FROM pay_order_flow WHERE pay_finish_date = #{payFinishDate} AND status = 0")
    BigDecimal sumReceiptMoney(@Param("payFinishDate") String payFinishDate);
}
