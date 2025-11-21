package com.lanf.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.constant.constant.Constants;
import com.lanf.security.utils.AdminSessionCache;
import com.lanf.system.mapper.SysMenuMapper;
import com.lanf.system.model.bo.SysUserBO;
import com.lanf.system.model.entiry.SysMenuDO;
import com.lanf.system.model.entiry.SysRoleMenuDO;
import com.lanf.system.model.entiry.SysUserDO;
import com.lanf.system.model.vo.AssginMenuVO;
import com.lanf.system.service.SysMenuService;
import com.lanf.system.service.SysRoleMenuService;
import com.lanf.system.service.SysUserService;
import com.lanf.system.service.manager.PermissionFilter;
import com.lanf.system.utils.MenuHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Transactional
@Service
@Slf4j
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenuDO> implements SysMenuService {

    @Autowired
    private SysMenuMapper sysMenuMapper;
    @Autowired
    private SysRoleMenuService sysRoleMenuService;
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private PermissionFilter permissionFilter;

    @Override
    public List<SysMenuDO> findNodes() {
        //全部权限列表
        List<SysMenuDO> sysMenuList = sysMenuMapper.queryList("", "");
        if (CollectionUtils.isEmpty(sysMenuList)) return null;
        SysUserBO sysUser = AdminSessionCache.getSysUser();

        if ( !permissionFilter.isPlatformAdminAccount(sysUser.getUsername(),sysUser.getTenantCode())){
            log.info("非平台租户,开始过滤按钮和菜单权限");
            sysMenuList = permissionFilter.excludeMenuAndButton(sysMenuList);
        }
        //构建树形数据
        return MenuHelper.buildTree(sysMenuList);
    }


    @Override
    public List<SysMenuDO> findDir(String notId) {
        List<SysMenuDO> sysMenuList = sysMenuMapper.queryList("0", notId);
        if (CollectionUtils.isEmpty(sysMenuList)) return null;
        //构建树形数据
        List<SysMenuDO> result = MenuHelper.buildTree(sysMenuList);
        return result;
    }

    @Override
    public List<SysMenuDO> findMenu() {
        List<SysMenuDO> sysMenuList = sysMenuMapper.queryList("1", "");
        if (CollectionUtils.isEmpty(sysMenuList)) return null;
      /*  //构建树形数据
        List<SysMenu> result = MenuHelper.buildTree(sysMenuList);*/
        return sysMenuList;
    }

    @Override
    public boolean removeById(Serializable id) {
        int count = this.count(new QueryWrapper<SysMenuDO>().eq("parent_id", id));
        if (count > 0) {
            throw new RuntimeException();
        }
        sysMenuMapper.deleteById(id);
        return false;
    }


    @Override
    public List<String> findSysMenuByRoleId(String roleId) {
        //获取所有status为1的权限列表
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.select("menu_id");
        queryWrapper.eq("role_id", roleId);
        Function<Object, String> f = (o -> o.toString());
        return sysRoleMenuService.listObjs(queryWrapper, f);
    }

    @Override
    public void doAssign(AssginMenuVO assginMenuVo) {
        //删除已分配的权限
        sysRoleMenuService.remove(new QueryWrapper<SysRoleMenuDO>().eq("role_id", assginMenuVo.getRoleId()));
        //遍历所有已选择的权限id
        List<SysRoleMenuDO> saveRoleMenus = new ArrayList<>();
        for (Long menuId : assginMenuVo.getMenuIdList()) {
            if (menuId != null) {
                //创建SysRoleMenu对象
                SysRoleMenuDO sysRoleMenu = new SysRoleMenuDO();
                sysRoleMenu.setMenuId(menuId);
                sysRoleMenu.setRoleId(assginMenuVo.getRoleId());
                //添加新权限
                saveRoleMenus.add(sysRoleMenu);
            }
        }
        if (saveRoleMenus.size() > 0) {
            sysRoleMenuService.saveBatch(saveRoleMenus);
        }
    }

    @Override
    public List<SysMenuDO> findUserMenuList(String userName) {
        SysUserDO sysUser = sysUserService.getByUsername(userName);
        Long userId = sysUser.getId();
        //超级管理员admin账号id为：1
        List<SysMenuDO> sysMenuList = null;
        if ("admin".equals(userName)) {
            QueryWrapper<SysMenuDO> queryWrapper = new QueryWrapper();
            queryWrapper.ne("type", 2);
            queryWrapper.orderByAsc("sort_value");
            sysMenuList = sysMenuMapper.selectList(queryWrapper);
        } else {
            List<Integer> typeList = new ArrayList<>();
            typeList.add(0);
            typeList.add(1);
            sysMenuList = sysMenuMapper.findListByUserId(userId, null, typeList);
        }
        SysUserBO sysUser2 = AdminSessionCache.getSysUser();
        if ( !permissionFilter.isPlatformAdminAccount(sysUser2.getUsername(),sysUser2.getTenantCode())){
            log.info("非平台租户,开始过滤菜单权限");
            sysMenuList = permissionFilter.excludeMenu(sysMenuList);
        }

        //构建树形数据
        List<SysMenuDO> sysMenuTreeList = MenuHelper.buildTree(sysMenuList);
        return sysMenuTreeList;
    }


    @Override
    public List<String> findUserPermsList(String userId,String username) {

        List<SysMenuDO> sysMenuList = null;
        if (username.equals("admin")) {
            //admin返回所有权限
            sysMenuList = this.list();
        } else {
            sysMenuList = sysMenuMapper.findListByUserId(Long.parseLong(userId), 2, null);
        }
        //创建返回的集合
        List<String> permissionList = new ArrayList<>();
        for (SysMenuDO sysMenu : sysMenuList) {
            if (sysMenu.getType() == 2) {
                permissionList.add(sysMenu.getPerms());
            }
        }
        return permissionList;
    }

}

