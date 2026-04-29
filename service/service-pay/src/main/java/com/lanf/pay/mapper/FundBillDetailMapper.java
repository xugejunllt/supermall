package com.lanf.pay.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lanf.pay.model.entity.FundBillDetailDO;
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
public interface FundBillDetailMapper extends BaseMapper<FundBillDetailDO> {
    /**
     * 批量插入，忽略重复
     */
    int batchInsertIgnore( @Param("list") List<FundBillDetailDO> list);
}
