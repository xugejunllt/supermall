package com.lanf.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lanf.system.model.entiry.SysRoleMenuDO;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Repository
@Mapper
public interface SysRoleMenuMapper extends BaseMapper<SysRoleMenuDO> {

}
