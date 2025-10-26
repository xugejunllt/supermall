package com.lanf.log.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lanf.log.model.entity.SysOperLogDO;
import com.lanf.log.model.vo.SysOperLogQueryVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
/**
* @author tanlingfei
* @version 1.0
* @description 操作日志记录 Mapper层
* @date 2023-04-30 21:39:39
*/
@Repository
@Mapper
public interface SysOperLogMapper extends BaseMapper<SysOperLogDO> {
    IPage<SysOperLogDO> selectPage(Page<SysOperLogDO> page, @Param("vo") SysOperLogQueryVo sysOperLogQueryVo);
    List<SysOperLogDO> queryList(@Param("vo") SysOperLogQueryVo sysOperLogQueryVo);
}