package com.lanf.system.service.impl.manager;


import com.lanf.common.utils.BeanCopyUtils;
import com.lanf.common.utils.ThreadLocalUtils;
import com.lanf.constant.constant.Constants;
import com.lanf.security.utils.UserUtil;
import com.lanf.system.model.bo.AddAdminUserBO;
import com.lanf.system.model.bo.SysUserBO;
import com.lanf.system.model.entiry.SysUserDO;
import com.lanf.system.service.SysUserService;
import com.lanf.system.service.manager.SystemManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 抽取公共方法
 */
@Service
public class SystemManagerServiceImpl implements SystemManagerService {

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Lazy
    @Autowired
    private SysUserService sysUserService;

    @Override
    public void addUser(AddAdminUserBO addSysUser) {

        SysUserDO sysUser = new SysUserDO();
        BeanCopyUtils.copy(addSysUser, sysUser);
        String pwd = bCryptPasswordEncoder.encode(sysUser.getPassword());
        sysUser.setPassword(pwd);
        sysUser.setStatus(sysUser.getStatus());
        sysUserService.save(sysUser);
    }

    @Override
    public void ignoreTableName() {

        SysUserBO userInfo = UserUtil.getUserInfo();
        String tenantCode = userInfo.getTenantCode();
        if (Constants.ADMIN_TENANT_CODE.equals(tenantCode)) {
            ThreadLocalUtils.addIgnoreTableName(true);
        }


    }
}
