package com.lanf.security.model.bo;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

public class AdminUserBO extends User {

    /**
     * 我们自己的用户实体对象，要调取用户信息时直接获取这个实体对象
     */
    private AdminUser sysUser;

    public AdminUserBO(AdminUser sysUser, Collection<? extends GrantedAuthority> authorities) {
        /**
         * 给spring security 框架使用
         */
        super(sysUser.getUsername(), sysUser.getPassword(), authorities);
        this.sysUser = sysUser;
    }

    public AdminUser getSysUser() {
        return sysUser;
    }

    public void setSysUser(AdminUser sysUser) {
        this.sysUser = sysUser;
    }

}