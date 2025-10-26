package com.lanf.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lanf.system.model.entiry.SysRoleDO;
import com.lanf.system.model.vo.SysRoleQueryVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
@Mapper
public interface SysRoleMapper extends BaseMapper<SysRoleDO> {
    IPage<SysRoleDO> selectPage(Page<SysRoleDO> page, @Param("vo") SysRoleQueryVO roleQueryVo);
}
