package com.lanf.system.controller;

import com.google.gson.JsonObject;
import com.lanf.constant.result.Result;
import com.lanf.constant.utils.UserContext;
import com.lanf.system.model.entiry.SysI18nDO;
import com.lanf.system.model.entiry.SysMenuDO;
import com.lanf.system.model.entiry.SysUserDO;
import com.lanf.system.model.vo.SysI18nQueryVO;
import com.lanf.system.model.vo.SysPwdVO;
import com.lanf.system.service.SysI18nService;
import com.lanf.system.service.SysMenuService;
import com.lanf.system.service.SysUserService;
import com.lanf.system.utils.JsonListEach;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 后台登录登出
 * </p>
 */
@Slf4j
@RestController
@RequestMapping("/admin/system/index")
public class IndexController {
    @Lazy
    @Autowired
    private SysUserService sysUserService;
    @Lazy
    @Autowired
    private SysMenuService sysMenuService;

    @Autowired
    private SysI18nService sysI18nService;

    @CrossOrigin
    @GetMapping("/getI18n")
    public Result getI18n(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Credentials", "true");
        SysI18nQueryVO sysI18nQueryVo = new SysI18nQueryVO();
        sysI18nQueryVo.setType("3001");
        List<SysI18nDO> list = sysI18nService.queryList(sysI18nQueryVo);
        JsonObject cn = JsonListEach.convertSysI18nToJson(list);
        sysI18nQueryVo.setType("3002");
        List<SysI18nDO> flist = sysI18nService.queryList(sysI18nQueryVo);
        JsonObject en = JsonListEach.convertSysI18nToJson(flist);
        Map<String, Object> map = new HashMap<>();
        map.put("cn", cn.toString());
        map.put("en", en.toString());
        return Result.ok(map);
    }

    /**
     * 获取用户信息
     *
     * @return
     */
    @GetMapping("/info")
    public Result info(HttpServletRequest request) {
        SysUserDO sysUser = sysUserService.getById(UserContext.getUserId());
        Map<String, Object> map = sysUserService.getUserInfo(sysUser.getUsername());

        return Result.ok(map);
    }
    /**
     * 获取用户菜单权限
     *
     * @param request
     * @return
     */
    @GetMapping("/menuTree")
    public Result menuTree(HttpServletRequest request) {
        SysUserDO sysUser = sysUserService.getById(UserContext.getUserId());

        List<SysMenuDO> menuList = sysMenuService.findUserMenuList(sysUser.getUsername());
        return Result.ok(menuList);
    }

    /**
     * 退出
     *
     * @return
     */
    @PostMapping("/logout")
    public Result logout() {

        SysUserDO sysUser = sysUserService.getById(UserContext.getUserId());

        return Result.ok();
    }
    @PostMapping("/changePwd")
    public Result changePwd(@RequestBody SysPwdVO sysPwdVo) {
        this.sysUserService.changePwd(sysPwdVo);
        return Result.ok();
    }


}
