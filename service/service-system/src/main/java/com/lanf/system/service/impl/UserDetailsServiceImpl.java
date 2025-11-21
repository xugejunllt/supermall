package com.lanf.system.service.impl;

import com.lanf.common.utils.BeanCopyUtils;
import com.lanf.constant.constant.Constants;
import com.lanf.security.utils.MerchantIdContext;
import com.lanf.system.mapper.SysUserMapper;
import com.lanf.system.model.bo.CustomUserBO;
import com.lanf.system.model.entiry.MerchantDO;
import com.lanf.system.model.entiry.SysUserDO;
import com.lanf.system.model.bo.SysUserBO;
import com.lanf.system.service.SysMenuService;
import com.lanf.system.service.SysUserService;
import com.lanf.system.service.manager.PermissionFilter;
import com.lanf.system.service.merchant.IMerchantService;
import com.lanf.web.utils.WebUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component("userDetailsServiceImpl")
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private SysMenuService sysMenuService;
    @Autowired
    private SysUserMapper sysUserMapper;
    @Autowired
    private IMerchantService companyService;
    @Autowired
    private IMerchantService merchantService;
    @Autowired
    private PermissionFilter permissionFilter;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        HttpServletRequest request = WebUtil.getRequest();
        String tenantCode = (String) request.getAttribute(Constants.TENANT_CODE);
        String chanel = request.getHeader(Constants.CHANEL);
        String deviceId = request.getHeader(Constants.DEVICE_ID);

        MerchantDO merchantDO = merchantService.lambdaQuery().eq(MerchantDO::getTenantCode, tenantCode).one();
        if (merchantDO == null) {
            throw new UsernameNotFoundException("租户不存在！");
        }
        MerchantIdContext.setMerchantId(merchantDO.getId());
        SysUserDO sysUser = sysUserService.lambdaQuery()
                .eq(SysUserDO::getUsername, username)
                .eq(SysUserDO::getTenantId, merchantDO.getId()).one();

        if (sysUser == null) {
            throw new UsernameNotFoundException("租户不存在！");
        }



        /**
         * 排除权限 非平台账号
         */
        List<String> userPermsList = sysMenuService.findUserPermsList(sysUser.getId() + "", username);
        if ( !permissionFilter.isPlatformAdminAccount(sysUser.getUsername(),tenantCode)){
            log.info("非平台租户,开始过滤按钮权限");
            userPermsList = permissionFilter.excludeButton(userPermsList);
        }

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        for (String perm : userPermsList) {
            authorities.add(new SimpleGrantedAuthority(perm.trim()));
        }

        SysUserBO sysUserBO = BeanCopyUtils.copyBean(sysUser, SysUserBO.class);
        sysUserBO.setChannel(Integer.parseInt(chanel));
        sysUserBO.setDeviceId(deviceId);
        sysUserBO.setMerchantId(merchantDO.getId());
        sysUserBO.setTenantCode(tenantCode);
        return new CustomUserBO(sysUserBO, authorities);

    }




}
