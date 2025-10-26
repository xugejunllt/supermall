package com.lanf.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.system.model.entiry.SysRoleDO;
import com.lanf.system.model.vo.AssginRoleVO;
import com.lanf.system.model.vo.SysRoleQueryVO;

public interface SysRoleService extends IService<SysRoleDO> {
    IPage<SysRoleDO> selectPage(Page<SysRoleDO> pageParam, SysRoleQueryVO roleQueryVo);

    void doAssign(AssginRoleVO assginRoleVo);

}
