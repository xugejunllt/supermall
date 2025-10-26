package com.lanf.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.system.model.entiry.SysUserDO;
import com.lanf.system.model.vo.SysPwdVO;
import com.lanf.system.model.vo.SysUserQueryVO;

import java.util.Map;

public interface SysUserService extends IService<SysUserDO> {

    IPage<SysUserDO> selectPage(Page<SysUserDO> pageParam, SysUserQueryVO adminQueryVo);

    public void updateStatus(String id, Integer status);

    public boolean saveSysUser(SysUserDO sysUser);

    public boolean updateById(SysUserDO sysUser);

    SysUserDO getByUsername(String username);

    /**
     * 根据用户名获取用户登录信息
     * @param username
     * @return
     */
    Map<String, Object> getUserInfo(String username);
    public SysUserDO getById(String id);

    public void changePwd(SysPwdVO sysPwdVo);

}
