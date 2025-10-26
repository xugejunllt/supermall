package com.lanf.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lanf.log.annotation.Log;
import com.lanf.log.type.BusinessType;

import com.lanf.system.model.entiry.SysRoleDO;
import com.lanf.system.model.vo.AssginMenuVO;
import com.lanf.system.model.vo.SysRoleQueryVO;
import com.lanf.web.result.Result;
import com.lanf.system.service.SysMenuService;
import com.lanf.system.service.SysRoleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "角色管理")
@RestController
@RequestMapping("/admin/system/sysRole")
public class SysRoleController {
    @Autowired
    private SysRoleService sysRoleService;

    @Autowired
    private SysMenuService sysMenuService;

    @PreAuthorize("hasAuthority('bnt.sysRole.list')")
    @ApiOperation(value = "获取全部角色列表")
    @GetMapping("/findAll")
    public Result findAll() {
        List<SysRoleDO> roleList = sysRoleService.list();
        return Result.ok(roleList);
    }

    @PreAuthorize("hasAuthority('bnt.sysRole.list')")
    @ApiOperation(value = "条件分页查询")
    @GetMapping("/{page}/{limit}")
    public Result findPageQueryRole(
            @ApiParam(name = "page", value = "当前页码", required = true)
            @PathVariable Long page,

            @ApiParam(name = "limit", value = "每页记录数", required = true)
            @PathVariable Long limit,

            @ApiParam(name = "roleQueryVo", value = "查询对象", required = false)
            SysRoleQueryVO roleQueryVo) {
        Page<SysRoleDO> pageParam = new Page<>(page, limit);
        IPage<SysRoleDO> pageModel = sysRoleService.selectPage(pageParam, roleQueryVo);
        return Result.ok(pageModel);
    }

    @PreAuthorize("hasAuthority('bnt.sysRole.list')")
    @ApiOperation(value = "获取角色")
    @GetMapping("/findRoleById/{id}")
    public Result get(@PathVariable String id) {
        SysRoleDO role = sysRoleService.getById(id);
        return Result.ok(role);
    }

    @Log(title = "角色管理", businessType = BusinessType.INSERT)
    @PreAuthorize("hasAuthority('bnt.sysRole.add')")
    @ApiOperation(value = "新增角色")
    @PostMapping("/save")
    public Result save(@RequestBody SysRoleDO role) {
        boolean b = sysRoleService.save(role);

        return Result.ok();

    }

    @Log(title = "角色管理", businessType = BusinessType.UPDATE)
    @PreAuthorize("hasAuthority('bnt.sysRole.update')")
    @ApiOperation(value = "修改角色")
    @PostMapping("/update")
    public Result updateById(@RequestBody SysRoleDO role) {
        boolean b = sysRoleService.updateById(role);

            return Result.ok();

    }

    @Log(title = "角色管理", businessType = BusinessType.DELETE)
    @PreAuthorize("hasAuthority('bnt.sysRole.remove')")
    @ApiOperation(value = "删除角色")
    @DeleteMapping("/remove/{id}")
    public Result remove(@PathVariable String id) {
        boolean b = sysRoleService.removeById(id);

        return Result.ok();

    }

    @Log(title = "角色管理", businessType = BusinessType.DELETE)
    @PreAuthorize("hasAuthority('bnt.sysRole.remove')")
    @ApiOperation(value = "根据id列表删除")
    @DeleteMapping("/batchRemove")
    public Result batchRemove(@RequestBody List<String> idList) {
        boolean b = sysRoleService.removeByIds(idList);

        return Result.ok();

    }

    @Log(title = "角色管理", businessType = BusinessType.ASSGIN)
    @PreAuthorize("hasAuthority('bnt.sysRole.assignAuth')")
    @ApiOperation(value = "给角色分配权限")
    @PostMapping("/doAuth")
    public Result doAuth(@RequestBody AssginMenuVO assginMenuVo) {
        sysMenuService.doAssign(assginMenuVo);
        return Result.ok();
    }
}
