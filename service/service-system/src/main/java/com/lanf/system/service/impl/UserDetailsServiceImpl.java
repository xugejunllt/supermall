package com.lanf.system.service.impl;

import com.lanf.common.utils.BeanCopyUtils;
import com.lanf.common.utils.ThreadLocalUtils;
import com.lanf.constant.constant.Constants;
import com.lanf.system.mapper.SysUserMapper;
import com.lanf.system.model.bo.CustomUserBO;
import com.lanf.system.model.entiry.MerchantDO;
import com.lanf.system.model.entiry.SysUserDO;
import com.lanf.system.model.bo.SysUserBO;
import com.lanf.system.service.SysMenuService;
import com.lanf.system.service.SysUserService;
import com.lanf.system.service.merchant.IMerchantService;
import com.lanf.web.utils.WebUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;


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
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        HttpServletRequest request = WebUtil.getRequest();
        String tenantCode = (String) request.getAttribute(Constants.TENANT_CODE);
        String chanel = request.getHeader(Constants.CHANEL);
        String  deviceId = request.getHeader(Constants.DEVICE_ID);

        MerchantDO merchantDO = merchantService.lambdaQuery().eq(MerchantDO::getTenantCode, tenantCode).one();
        if ( merchantDO == null){
            throw new UsernameNotFoundException("租户不存在！");
        }

        SysUserDO sysUser = sysUserService.lambdaQuery()
                .eq(SysUserDO::getUsername,username)
                .eq(SysUserDO::getTenantId,merchantDO.getId()).one();

        if (  sysUser == null) {
            throw new UsernameNotFoundException("租户不存在！");
        }


        /**
         * 这里过滤出平台租户才有的页面权限
         */
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        List<String> userPermsList = sysMenuService.findUserPermsList(sysUser.getId() + "", username);
        for (String perm : userPermsList) {
            authorities.add(new SimpleGrantedAuthority(perm.trim()));
        }

        SysUserBO sysUserBO = BeanCopyUtils.copyBean(sysUser, SysUserBO.class);
        sysUserBO.setChannel(Integer.parseInt(chanel));
        sysUserBO.setDeviceId(deviceId);
        sysUserBO.setMerchantId(merchantDO.getId());
        return new CustomUserBO(sysUserBO, authorities);

    }
}
