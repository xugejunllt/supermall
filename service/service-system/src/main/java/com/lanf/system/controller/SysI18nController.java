package com.lanf.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lanf.log.annotation.Log;
import com.lanf.log.type.BusinessType;

import com.lanf.system.model.entiry.SysI18nDO;
import com.lanf.system.model.vo.SysI18nQueryVO;
import com.lanf.web.result.Result;
import com.lanf.system.service.SysI18nService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author tanlingfei
 * @version 1.0
 * @description 国际化语言
 * @date 2023-10-31 13:47:32
 */
@Api(tags = "国际化语言")
@RestController
@RequestMapping("/system/sysI18n")
public class SysI18nController {
    @Autowired
    private SysI18nService sysI18nService;



    @PreAuthorize("hasAuthority('bnt.sysI18n.list')")
    @ApiOperation(value = "获取分页列表")
    @GetMapping("/{page}/{limit}")
    public Result index(@ApiParam(name = "page", value = "当前页码", required = true)
                        @PathVariable Long page,
                        @ApiParam(name = "limit", value = "每页记录数", required = true)
                        @PathVariable Long limit,
                        @ApiParam(name = "sysI18nQueryVo", value = "查询对象", required = false)
                        SysI18nQueryVO sysI18nQueryVo) {
        Page<SysI18nDO> pageParam = new Page<>(page, limit);
        IPage<SysI18nDO> pageModel = sysI18nService.selectPage(pageParam, sysI18nQueryVo);
        return Result.ok(pageModel);
    }

    @PreAuthorize("hasAuthority('bnt.sysI18n.list')")
    @ApiOperation(value = "查询列表")
    @GetMapping("/list")
    public Result list(@ApiParam(name = "sysI18nQueryVo", value = "查询对象", required = false)
                       SysI18nQueryVO sysI18nQueryVo) {
        List<SysI18nDO> list = sysI18nService.queryList(sysI18nQueryVo);

        return Result.ok(list);
    }

    @PreAuthorize("hasAuthority('bnt.sysI18n.list')")
    @ApiOperation(value = "所有国际化语言列表")
    @GetMapping("findAll")
    public Result findAllSysI18n() {
        //调用service的方法实现查询所有的操作
        List<SysI18nDO> list = sysI18nService.list(null);
        return Result.ok(list);
    }

    @PreAuthorize("hasAuthority('bnt.sysI18n.list')")
    @ApiOperation(value = "获取国际化语言")
    @GetMapping("/get/{id}")
    public Result get(@PathVariable String id) {
        SysI18nDO sysI18n = sysI18nService.getById(id);
        return Result.ok(sysI18n);
    }

    @PreAuthorize("hasAuthority('bnt.sysI18n.list')")
    @ApiOperation(value = "获取国际化语言集合")
    @PostMapping("/getByIds")
    public Result getByIds(@RequestBody List<String> idList) {
        List<SysI18nDO> list = sysI18nService.getByIds(idList);
        return Result.ok(list);
    }

    @Log(title = "国际化语言", businessType = BusinessType.INSERT)
    @PreAuthorize("hasAuthority('bnt.sysI18n.add')")
    @ApiOperation(value = "保存国际化语言")
    @PostMapping("/save")
    public Result save(@RequestBody SysI18nDO sysI18n) {
        sysI18nService.save(sysI18n);
        return Result.ok();
    }

    @Log(title = "国际化语言", businessType = BusinessType.UPDATE)
    @PreAuthorize("hasAuthority('bnt.sysI18n.update')")
    @ApiOperation(value = "更新国际化语言")
    @PutMapping("/update")
    public Result updateById(@RequestBody SysI18nDO sysI18n) {
        sysI18nService.updateById(sysI18n);
        return Result.ok();
    }

    @Log(title = "国际化语言", businessType = BusinessType.DELETE)
    @PreAuthorize("hasAuthority('bnt.sysI18n.remove')")
    @ApiOperation(value = "删除国际化语言")
    @DeleteMapping("/remove/{id}")
    public Result remove(@PathVariable String id) {
        sysI18nService.removeById(id);
        return Result.ok();
    }

    @Log(title = "国际化语言", businessType = BusinessType.DELETE)
    @PreAuthorize("hasAuthority('bnt.sysI18n.remove')")
    @ApiOperation(value = "根据id列表删除")
    @DeleteMapping("/batchRemove")
    public Result batchRemove(@RequestBody List<String> idList) {
        boolean b = sysI18nService.removeByIds(idList);

            return Result.ok();

    }


}
