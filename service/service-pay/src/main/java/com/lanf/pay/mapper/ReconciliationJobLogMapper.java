package com.lanf.pay.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lanf.pay.model.entity.ReconciliationJobLogDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 对账任务执行记录表 Mapper 接口
 * </p>
 *
 * @author jarven
 * @since 2026-04-30
 */
public interface ReconciliationJobLogMapper extends BaseMapper<ReconciliationJobLogDO> {

    /**
     * 批量插入，忽略重复
     */
    int batchInsertIgnore(@Param("list") List<ReconciliationJobLogDO> list);

    /**
     * 物理删除全表数据
     */
    int deleteAll();

}
