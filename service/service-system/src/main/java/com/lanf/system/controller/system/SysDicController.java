package com.lanf.system.controller.system;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.lanf.system.model.entiry.SysDicDO;
import com.lanf.system.model.vo.SysDicQueryVO;
import com.lanf.constant.result.Result;
import com.lanf.system.service.SysDicService;
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
 * @description 字典分类
 * @date 2020-04-13 09:55:26
 */
@Api(tags = "字典分类")
@RestController
@RequestMapping("/system/sysDic")
public class SysDicController {
    @Autowired
    private SysDicService sysDicService;

    @PreAuthorize("hasAuthority('bnt.sysDic.list')")
    @ApiOperation(value = "获取分页列表")
    @GetMapping("/{page}/{limit}")
    public Result index(@ApiParam(name = "page", value = "当前页码", required = true)
                        @PathVariable Long page,
                        @ApiParam(name = "limit", value = "每页记录数", required = true)
                        @PathVariable Long limit,
                        @ApiParam(name = "sysDicQueryVo", value = "查询对象", required = false)
                        SysDicQueryVO sysDicQueryVo) {
        Page<SysDicDO> pageParam = new Page<>(page, limit);
        IPage<SysDicDO> pageModel = sysDicService.selectPage(pageParam, sysDicQueryVo);
        return Result.ok(pageModel);
    }

    @PreAuthorize("hasAuthority('bnt.sysDic.list')")
    @ApiOperation(value = "所有字典分类列表")
    @GetMapping("findAll")
    public Result findAll() {
        //调用service的方法实现查询所有的操作
        List<SysDicDO> list = sysDicService.list(null);
        return Result.ok(list);
    }

    @PreAuthorize("hasAuthority('bnt.sysDic.list')")
    @ApiOperation(value = "获取字典分类")
    @GetMapping("/get/{id}")
    public Result get(@PathVariable String id) {
        SysDicDO sysDic = sysDicService.getById(id);
        return Result.ok(sysDic);
    }

    @PreAuthorize("hasAuthority('bnt.sysDic.add')")
    @ApiOperation(value = "保存字典分类")
    @PostMapping("/save")
    public Result save(@RequestBody SysDicDO sysDic) {
        sysDicService.save(sysDic);
        return Result.ok();
    }

    @PreAuthorize("hasAuthority('bnt.sysDic.update')")
    @ApiOperation(value = "更新字典分类")
    @PutMapping("/update")
    public Result updateById(@RequestBody SysDicDO sysDic) {
        sysDicService.updateById(sysDic);
        return Result.ok();
    }


    @PreAuthorize("hasAuthority('bnt.sysDic.remove')")
    @ApiOperation(value = "删除字典分类")
    @DeleteMapping("/batchRemove")
    public Result batchRemove(@RequestBody List<String> idList) {
        boolean b = sysDicService.removeByIds(idList);

        return Result.ok();

    }
}
