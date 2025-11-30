package com.lanf.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lanf.common.utils.LogFormatUtils;
import com.lanf.common.utils.LogInfo;
import com.lanf.log.annotation.Log;
import com.lanf.log.type.BusinessType;
import com.lanf.system.model.entiry.SysUserDO;
import com.lanf.system.model.vo.SysUserQueryVO;
import com.lanf.constant.result.Result;
import com.lanf.system.service.SysUserRoleService;
import com.lanf.system.service.SysUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

@Api(tags = "用户管理")
@RestController
@RequestMapping("/admin/system/sysUser")
@Slf4j
public class SysUserController {
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private SysUserRoleService sysUserRoleService;


    @PreAuthorize("hasAuthority('bnt.sysUser.list')")
    @ApiOperation(value = "获取分页列表")
    @GetMapping("/{page}/{limit}")
    public Result index(
            @ApiParam(name = "page", value = "当前页码", required = true)
            @PathVariable Long page,
            @ApiParam(name = "limit", value = "每页记录数", required = true)
            @PathVariable Long limit,
            @ApiParam(name = "userQueryVo", value = "查询对象", required = false)
            SysUserQueryVO userQueryVo, HttpServletRequest request) {
       LogFormatUtils.printFormatLog(log, "分页查询用户列表",Arrays.asList(new LogInfo("userId", "111")),userQueryVo);
        Page<SysUserDO> pageParam = new Page<>(page, limit);
        IPage<SysUserDO> pageModel = sysUserService.selectPage(pageParam, userQueryVo);
        return Result.ok(pageModel);
    }

    @PreAuthorize("hasAuthority('bnt.sysUser.list')")
    @ApiOperation(value = "获取用户")
    @GetMapping("/getUser/{id}")
    public Result get(@PathVariable String id) {
        SysUserDO user = sysUserService.getById(id);
        return Result.ok(user);
    }

    @Log(title = "用户管理", businessType = BusinessType.INSERT)
    @PreAuthorize("hasAuthority('bnt.sysUser.add')")
    @ApiOperation(value = "保存用户")
    @PostMapping("/save")
    public Result save(@RequestBody SysUserDO user) {

        sysUserService.saveSysUser(user);
        return Result.ok();
    }

    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PreAuthorize("hasAuthority('bnt.sysUser.update')")
    @ApiOperation(value = "更新用户")
    @PostMapping("/update")
    public Result updateById(@RequestBody SysUserDO user) {
        sysUserService.updateById(user);
        return Result.ok();
    }

    @Log(title = "用户管理", businessType = BusinessType.STATUS)
    @PreAuthorize("hasAuthority('bnt.sysUser.update')")
    @ApiOperation(value = "更新状态")
    @GetMapping("updateStatus/{id}/{status}")
    public Result updateStatus(@PathVariable String id, @PathVariable Integer status) {
        sysUserService.updateStatus(id, status);
        return Result.ok();
    }

    @Log(title = "用户管理", businessType = BusinessType.DELETE)
    @PreAuthorize("hasAuthority('bnt.sysUser.remove')")
    @ApiOperation(value = "根据id列表删除")
    @DeleteMapping("/batchRemove")
    public Result batchRemove(@RequestBody List<String> idList) {
        boolean b = sysUserService.removeByIds(idList);

        return Result.ok();

    }

}
