package com.lanf.system.service.manager;

import com.lanf.constant.constant.Constants;
import com.lanf.system.model.entiry.SysMenuDO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Component
public class PermissionFilter {


    public List<SysMenuDO> excludeMenuAndButton( List<SysMenuDO> sysMenuList){

        Iterator<SysMenuDO> iterator = sysMenuList.iterator();
        List<String> excludeMenu = getExcludeMenu();
        List<String> excludeButton = getExcludeButton();
        while (iterator.hasNext()){

            SysMenuDO next = iterator.next();
            if (excludeMenu.contains(next.getPath())){
                //菜单通过path匹配
                iterator.remove();
            }
            if (excludeButton.contains(next.getPerms())){
                //按钮通过perms匹配
                iterator.remove();
            }

        }
        return sysMenuList;
    }
    public List<SysMenuDO> excludeMenu( List<SysMenuDO> sysMenuList){

        Iterator<SysMenuDO> iterator = sysMenuList.iterator();
        List<String> excludeMenu = getExcludeMenu();
        while (iterator.hasNext()){

            SysMenuDO next = iterator.next();
            if (excludeMenu.contains(next.getPath())){
                //菜单通过path匹配
                iterator.remove();
            }


        }
        return sysMenuList;
    }
    /**
     * 需要排除的菜单
     * @return
     */
    private List<String> getExcludeMenu(){

        /**
         * 非平台租户排除的操作权限
         *
         */
        List<String> excludeMenu = new ArrayList<>();
        excludeMenu.add("sysMenu");
        excludeMenu.add("sysDic");
        excludeMenu.add("sysDicItem");
        excludeMenu.add("mdile");

        return  excludeMenu;

    }

    /**
     * 排除菜单
     *
     *
     */
    public List<String> excludeButton(List<String> userPermsList){

        Iterator<String> iterator = userPermsList.iterator();
        List<String> excludeButton = getExcludeButton();
        while (iterator.hasNext()){
            String next = iterator.next();
            if (excludeButton.contains(next)){
                iterator.remove();
            }

        }
        return userPermsList;
    }
    /**
     * 需要排除的按钮
     * @param
     * @return
     */
    private List<String> getExcludeButton(){

        /**
         * 非平台租户排除的操作权限 可以防止配置中心 支持动态刷新
         *为了提高查询效率 使用map
         *
         */
        List<String> excludePerm = new ArrayList<>();
        excludePerm.add("bnt.sysMenu.add");
        excludePerm.add("bnt.sysMenu.update");
        excludePerm.add("bnt.sysMenu.remove");

        return  excludePerm;
    }

    /**
     * 是否是平台admin 账户
     * @param tenetId
     * @return
     */
    public boolean isPlatformAdminAccount(String userName,Long tenetId){

        return "admin".equals(userName) && Constants.PLATFORM_BUSINESS_ID.equals(tenetId);
    }

}
