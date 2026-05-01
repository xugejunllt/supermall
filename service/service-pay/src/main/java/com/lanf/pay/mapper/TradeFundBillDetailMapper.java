package com.lanf.pay.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lanf.pay.model.entity.TradeFundBillDetail;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 支付宝交易账单表 Mapper 接口
 * </p>
 *
 * @author jarven
 * @since 2026-05-02
 */
public interface TradeFundBillDetailMapper extends BaseMapper<TradeFundBillDetail> {

    /**
     * 批量插入，忽略重复
     */
    int batchInsertIgnore(@Param("list") List<TradeFundBillDetail> list);

}
