package com.lanf.pay.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lanf.pay.model.entity.ReconciliationDiffMarkerDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 去重标记 Mapper 接口
 * </p>
 *
 * @author jarven
 * @since 2026-04-30
 */
public interface ReconciliationDiffMarkerMapper extends BaseMapper<ReconciliationDiffMarkerDO> {

    /**
     * 批量插入，忽略重复
     */
    int batchInsertIgnore(@Param("list") List<ReconciliationDiffMarkerDO> list);

    /**
     * 物理删除全表数据
     */
    int deleteAll();

}
