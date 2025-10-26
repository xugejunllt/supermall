package com.lanf.system.service.manager;

import com.lanf.system.model.bo.AddAdminUserBO;

public interface SystemManagerService {
    /**
     * 添加系统用户
     * @param addSysUser
     */
    void addUser(AddAdminUserBO addSysUser);

    /**
     * 添加不需要多租户id的表
     */
    void  ignoreTableName();
}
