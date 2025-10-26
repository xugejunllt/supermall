package com.lanf.system.utils;


import com.lanf.system.model.entiry.SysMenuDO;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 根据菜单数据构建菜单树的工具类
 * </p>
 */
public class MenuHelper {

    /**
     * 使用递归方法建菜单
     *
     * @param sysMenuList
     * @return
     */
    public static List<SysMenuDO> buildTree(List<SysMenuDO> sysMenuList) {
        List<SysMenuDO> trees = new ArrayList<>();
        for (SysMenuDO sysMenu : sysMenuList) {
            if (sysMenu.getParentId() == 0L) {
                trees.add(findChildren(sysMenu, sysMenuList));
            }
        }
        return trees;
    }

    /**
     * 递归查找子节点
     *
     * @param treeNodes
     * @return
     */
    public static SysMenuDO findChildren(SysMenuDO sysMenu, List<SysMenuDO> treeNodes) {
        sysMenu.setChildren(new ArrayList<SysMenuDO>());

        for (SysMenuDO it : treeNodes) {
            if (sysMenu.getId().equals(it.getParentId())) {
                if (sysMenu.getChildren() == null) {
                    sysMenu.setChildren(new ArrayList<>());
                }
                sysMenu.getChildren().add(findChildren(it, treeNodes));
            }
        }
        return sysMenu;
    }
}
