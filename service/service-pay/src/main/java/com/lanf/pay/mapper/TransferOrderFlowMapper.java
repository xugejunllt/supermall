package com.lanf.pay.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lanf.pay.model.entity.TransferOrderFlowDO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

/**
 * <p>
 * 转账单 Mapper 接口
 * </p>
 *
 * @author jarven
 * @since 2026-05-02
 */
public interface TransferOrderFlowMapper extends BaseMapper<TransferOrderFlowDO> {

    /**
     * 根据日期统计转账金额
     */
    @Select("SELECT COALESCE(SUM(total_amount), 0) FROM transfer_order_flow WHERE pay_finish_date = #{payFinishDate}")
    BigDecimal sumTotalAmount(@Param("payFinishDate") String payFinishDate);
}
