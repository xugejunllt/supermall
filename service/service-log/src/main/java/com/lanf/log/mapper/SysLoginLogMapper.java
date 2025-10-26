package com.lanf.log.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lanf.log.model.entity.SysLoginLogDO;
import com.lanf.log.model.vo.SysLoginLogQueryVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
/**
* @author tanlingfei
* @version 1.0
* @description 系统访问记录 Mapper层
* @date 2023-04-30 21:36:41
*/
@Repository
@Mapper
public interface SysLoginLogMapper extends BaseMapper<SysLoginLogDO> {
    IPage<SysLoginLogDO> selectPage(Page<SysLoginLogDO> page, @Param("vo") SysLoginLogQueryVo sysLoginLogQueryVo);
    List<SysLoginLogDO> queryList(@Param("vo") SysLoginLogQueryVo sysLoginLogQueryVo);
}