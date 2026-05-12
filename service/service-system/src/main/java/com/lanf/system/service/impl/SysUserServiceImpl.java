package com.lanf.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.common.utils.MD5;

import com.lanf.security.custom.IBCryptPasswordEncoder;
import com.lanf.security.utils.AdminSessionCache;
import com.lanf.system.mapper.SysUserMapper;
import com.lanf.system.model.bo.SysUserBO;
import com.lanf.system.model.entiry.SysDeptDO;
import com.lanf.system.model.entiry.SysUserDO;
import com.lanf.system.model.entiry.SysUserRoleDO;
import com.lanf.system.model.vo.SysPwdVO;
import com.lanf.system.model.vo.SysUserQueryVO;
import com.lanf.system.service.SysDeptService;
import com.lanf.system.service.SysMenuService;
import com.lanf.system.service.SysUserRoleService;
import com.lanf.system.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.function.Function;

@Transactional
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUserDO> implements SysUserService {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysUserRoleService sysUserRoleService;

    @Autowired
    private SysDeptService sysDeptService;
    @Lazy
    @Autowired
    private SysMenuService sysMenuService;
    @Autowired
    private IBCryptPasswordEncoder customMd5PasswordEncoder;
    @Autowired
    private IBCryptPasswordEncoder cryptPasswordEncoder;
    @Override
    public IPage<SysUserDO> selectPage(Page<SysUserDO> pageParam, SysUserQueryVO userQueryVo) {
        //只查询当前登录所属部门数据
        SysUserBO sysUser = AdminSessionCache.getSysUser();

        if ("admin".equals(sysUser.getUsername())) {
            userQueryVo.setDeptId(null);
        } else {
            if (StringUtils.isEmpty(sysUser.getDeptId())) {
                return null;
            }
            userQueryVo.setDeptId(sysUser.getDeptId());
        }

        return sysUserMapper.selectPage(pageParam, userQueryVo);
    }

    @Override
    public void updateStatus(String id, Integer status) {
        SysUserDO sysUser = sysUserMapper.selectById(id);
        sysUser.setStatus(status);
        sysUserMapper.updateById(sysUser);
    }

    @Override
    public SysUserDO getByUsername(String username) {
        SysUserDO sysUser = sysUserMapper.selectOne(new QueryWrapper<SysUserDO>().eq("username", username));
        return sysUser;
    }

    @Override
    public boolean saveSysUser(SysUserDO sysUser) {
        if (sysUser.getDataStatus()){
            sysUser.setStatus(1);
        } else {
            sysUser.setStatus(0);
        }

        String pwd = customMd5PasswordEncoder.encode(sysUser.getPassword());
       // sysUser.setTenantCode(UserUtils.getTenantCode());
        sysUser.setPassword(pwd);
        sysUser.setStatus(sysUser.getStatus());
        int result = this.sysUserMapper.insert(sysUser);
        List<Long> roleList = sysUser.getRoleList();
        if (roleList != null && roleList.size() > 0) {
            List<SysUserRoleDO> saveRoles = new ArrayList<>();
            for (Long roleId : roleList) {
                SysUserRoleDO sysUserRole = new SysUserRoleDO();
                sysUserRole.setUserId(sysUser.getId());
                sysUserRole.setRoleId(roleId);
                saveRoles.add(sysUserRole);
            }
            this.sysUserRoleService.saveBatch(saveRoles);
        }
        return result > 0;
    }

    @Override
    public boolean updateById(SysUserDO sysUser) {
        if (!StringUtils.isEmpty(sysUser.getNewpassword()) && !"null".equals(sysUser.getNewpassword())) {
            String pwd = MD5.encrypt(sysUser.getNewpassword());
            sysUser.setPassword(pwd);
        }
        sysUser.setStatus(sysUser.getStatus() );
        int row = this.sysUserMapper.updateById(sysUser);
        List<Long> roleList = sysUser.getRoleList();
        if (roleList != null && roleList.size() > 0) {
            List<SysUserRoleDO> saveRoles = new ArrayList<>();
            for (Long roleId : roleList) {
                SysUserRoleDO sysUserRole = new SysUserRoleDO();
                sysUserRole.setUserId(sysUser.getId());
                sysUserRole.setRoleId(roleId);
                saveRoles.add(sysUserRole);
            }
            QueryWrapper queryWrapper = new QueryWrapper();
            queryWrapper.eq("user_id", sysUser.getId());
            sysUserRoleService.remove(queryWrapper);
            this.sysUserRoleService.saveBatch(saveRoles);
        }
        return row > 0;
    }

    @Override
    public SysUserDO getById(String id) {
        SysUserDO sysUser = sysUserMapper.selectById(id);
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.select("role_id");
        queryWrapper.eq("user_id", sysUser.getId());
        Function<Object, String> f = (o -> o.toString());
        List<Long> roleList = sysUserRoleService.listObjs(queryWrapper, f);
        sysUser.setRoleList(roleList);
        return sysUser;
    }

    @Override
    public Map<String, Object> getUserInfo(String username) {

        Map<String, Object> result = new HashMap<>();
        SysUserDO sysUser = this.getByUsername(username);
        if (sysUser != null) {
            result.put("username", sysUser.getUsername());
            result.put("name", sysUser.getName());
            result.put("mobile", sysUser.getMobile());
            result.put("email", sysUser.getEmail());

            result.put("avatar", "https://wpimg.wallstcn.com/f778738c-e4f8-4870-b634-56703b4acafe.gif");
            result.put("roles", new HashSet<>());
            List<String> buttons = sysMenuService.findUserPermsList(sysUser.getId()+"",username);
            result.put("buttons", buttons);
        }
        SysDeptDO sysDept = sysDeptService.getById(sysUser.getDeptId());
        if (sysDept != null){
            result.put("deptName", sysDept.getName());
        }
        return result;
    }

    @Override
    public void changePwd(SysPwdVO sysPwdVo) {
        SysUserDO sysUser = this.getById(UserUtils.getAdminUserId());
        if (!MD5.encrypt(sysPwdVo.getPassword()).equals(sysUser.getPassword())) {
            throw new RuntimeException();
        }
        if (!sysPwdVo.getCfpassword().equals(sysPwdVo.getNpassword())) {
            throw new RuntimeException();
        }
        sysUser.setPassword(MD5.encrypt(sysPwdVo.getCfpassword()));
        this.updateById(sysUser);
    }
}
