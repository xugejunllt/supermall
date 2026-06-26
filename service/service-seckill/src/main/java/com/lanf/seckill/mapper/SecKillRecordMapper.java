package com.lanf.seckill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lanf.seckill.model.entity.SecKillRecordDO;
import org.apache.ibatis.annotations.Delete;

/**
 * <p>
 * 秒杀记录表 Mapper 接口
 * </p>
 *
 * @author jarven
 * @since 2026-05-09
 */
public interface SecKillRecordMapper extends BaseMapper<SecKillRecordDO> {
    @Delete("DELETE FROM sec_kill_record")
    int deleteAll();
}
