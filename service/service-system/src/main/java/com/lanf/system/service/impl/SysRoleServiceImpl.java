package com.lanf.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.lanf.system.mapper.SysRoleMapper;
import com.lanf.system.model.entiry.SysRoleDO;
import com.lanf.system.model.entiry.SysUserRoleDO;
import com.lanf.system.model.vo.AssginRoleVO;
import com.lanf.system.model.vo.SysRoleQueryVO;
import com.lanf.system.service.SysRoleService;
import com.lanf.system.service.SysUserRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Transactional
@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRoleDO> implements SysRoleService {

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private SysUserRoleService sysUserRoleService;

    @Override
    public void doAssign(AssginRoleVO assginRoleVo) {
        //根据用户id删除原来分配的角色
        QueryWrapper<SysUserRoleDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", assginRoleVo.getUserId());
        sysUserRoleService.remove(queryWrapper);
        //获取所有的角色id
        List<Long> roleIdList = assginRoleVo.getRoleIdList();
        List<SysUserRoleDO> saveUserRoles = new ArrayList<>();
        for (Long roleId : roleIdList) {
            if (roleId != null) {
                SysUserRoleDO sysUserRole = new SysUserRoleDO();
                sysUserRole.setUserId(assginRoleVo.getUserId());
                sysUserRole.setRoleId(roleId);
                saveUserRoles.add(sysUserRole);
            }
        }
        if (saveUserRoles.size() > 0) {
            sysUserRoleService.saveBatch(saveUserRoles);
        }
    }

    @Override
    public IPage<SysRoleDO> selectPage(Page<SysRoleDO> pageParam, SysRoleQueryVO roleQueryVo) {
        return sysRoleMapper.selectPage(pageParam, roleQueryVo);
    }

}
