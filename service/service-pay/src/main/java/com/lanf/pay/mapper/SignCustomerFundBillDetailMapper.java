package com.lanf.pay.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lanf.pay.model.entity.SignCustomerFundBillDetailDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 资金账单明细表 Mapper 接口
 * </p>
 *
 * @author jarven
 * @since 2026-04-29
 */
public interface SignCustomerFundBillDetailMapper extends BaseMapper<SignCustomerFundBillDetailDO> {
    /**
     * 批量插入，忽略重复
     */
    int batchInsertIgnore( @Param("list") List<SignCustomerFundBillDetailDO> list);
}
