package com.lanf.system.service.impl;

import com.lanf.common.utils.BeanCopyUtils;
import com.lanf.common.utils.ThreadLocalUtils;
import com.lanf.constant.constant.Constants;
import com.lanf.system.mapper.SysUserMapper;
import com.lanf.system.model.bo.CustomUserBO;
import com.lanf.system.model.entiry.CompanyDO;
import com.lanf.system.model.entiry.ShopDO;
import com.lanf.system.model.entiry.SysUserDO;
import com.lanf.system.model.bo.SysUserBO;
import com.lanf.system.service.SysMenuService;
import com.lanf.system.service.SysUserService;
import com.lanf.system.service.company.ICompanyService;
import com.lanf.system.service.company.IShopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

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
    private ICompanyService companyService;
    @Autowired
    private IShopService shopService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        SysUserBO sysUserVO = new SysUserBO();
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();


        String tenantCode = ThreadLocalUtils.getTenantCode();
        SysUserDO sysUser = sysUserMapper.getByUserName(username, tenantCode);
        if (null == sysUser) {
            throw new UsernameNotFoundException("用户名不存在！");
        }
        BeanCopyUtils.copy(sysUser, sysUserVO);

        if (sysUser.getStatus() == 0) {
            throw new RuntimeException("账号已停用");
        }
        List<String> userPermsList = sysMenuService.findUserPermsList(sysUser.getId() + "", username);
        for (String perm : userPermsList) {
            authorities.add(new SimpleGrantedAuthority(perm.trim()));
        }
        ThreadLocalUtils.addIgnoreTableName(true);
        CompanyDO companyDO = companyService.lambdaQuery().
                eq(CompanyDO::getTenantCode,tenantCode).
                one();

        //商家用户才有店铺
        ShopDO one = shopService.lambdaQuery().eq(ShopDO::getBusinessId,companyDO.getId()).one();
        sysUserVO.setShopId(one.getId());
        sysUserVO.setBusinessId(companyDO.getId());

        return new CustomUserBO(sysUserVO, authorities);

    }
}
