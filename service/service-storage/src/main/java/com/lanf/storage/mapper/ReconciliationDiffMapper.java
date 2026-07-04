package com.lanf.storage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lanf.storage.model.entity.ReconciliationDiffDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author jarven
 * @since 2026-05-06
 */
public interface ReconciliationDiffMapper extends BaseMapper<ReconciliationDiffDO> {

    /**
     * 批量插入，忽略重复
     */
    int batchInsertIgnore(@Param("list") List<ReconciliationDiffDO> list);

}
