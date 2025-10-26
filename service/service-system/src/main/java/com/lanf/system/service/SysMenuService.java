package com.lanf.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.system.model.entiry.SysMenuDO;
import com.lanf.system.model.vo.AssginMenuVO;

import java.util.List;

public interface SysMenuService extends IService<SysMenuDO> {
    /**
     * 菜单树形数据
     *
     * @return
     */
    List<SysMenuDO> findNodes();

    public List<SysMenuDO> findDir(String notId);

    public List<SysMenuDO> findMenu();

    public List<String> findSysMenuByRoleId(String roleId);

    void doAssign(AssginMenuVO assginMenuVo);

    /**
     * 获取用户菜单
     *
     * @param userId
     * @return
     */
    List<SysMenuDO> findUserMenuList(String userId);

    /**
     * 获取用户按钮权限
     * @param userId
     * @return
     */
    List<String> findUserPermsList(String userId,String username);

}
