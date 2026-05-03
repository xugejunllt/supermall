package com.lanf.pay.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lanf.pay.model.entity.ReconciliationDiffDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 对账差异明细表 Mapper 接口
 * </p>
 *
 * @author jarven
 * @since 2026-04-30
 */
public interface ReconciliationDiffMapper extends BaseMapper<ReconciliationDiffDO> {

    /**
     * 批量插入，忽略重复
     */
    int batchInsertIgnore(@Param("list") List<ReconciliationDiffDO> list);
}
