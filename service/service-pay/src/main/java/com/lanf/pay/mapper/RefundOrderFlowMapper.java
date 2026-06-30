package com.lanf.pay.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lanf.pay.model.entity.RefundOrderFlowDO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

/**
 * <p>
 * 退款单表 Mapper 接口
 * </p>
 *
 * @author jarven
 * @since 2026-05-02
 */
public interface RefundOrderFlowMapper extends BaseMapper<RefundOrderFlowDO> {

    /**
     * 根据日期统计退款金额
     */
    @Select("SELECT COALESCE(SUM(return_money), 0) FROM refund_order_flow WHERE pay_finish_date = #{payFinishDate} AND status = 0")
    BigDecimal sumReturnMoney(@Param("payFinishDate") String payFinishDate);
}
