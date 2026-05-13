package com.lanf.system.controller.system;

import com.lanf.constant.result.Result;
import com.lanf.system.model.entiry.SysDeptDO;
import com.lanf.system.model.vo.SysDeptQueryVO;
import com.lanf.system.service.SysDeptService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/system/sysDept")
public class SysDeptController {
    @Lazy
    @Autowired
    private SysDeptService sysDeptService;

    @PreAuthorize("hasAuthority('bnt.sysDept.list')")
    @ApiOperation(value = "获取部门")
    @GetMapping("findNodes")
    public Result findNodes(SysDeptQueryVO sysDeptQueryVo) {
        List<SysDeptDO> list = sysDeptService.findNodes(sysDeptQueryVo);
        return Result.ok(list);
    }

    @PreAuthorize("hasAuthority('bnt.sysDept.list')")
    @ApiOperation(value = "获取部门")
    @GetMapping("findSelectNodes")
    public Result findSelectNodes() {
        List<Map> list = sysDeptService.findSelectNodes();
        return Result.ok(list);
    }


    @PreAuthorize("hasAuthority('bnt.sysDept.list')")
    @ApiOperation(value = "获取部门")
    @GetMapping("findNodesByParent/{parentId}")
    public Result findNodesByParent(@PathVariable String parentId) {
        List<SysDeptDO> list = sysDeptService.findNodesByParent(parentId);
        return Result.ok(list);
    }


    @PreAuthorize("hasAuthority('bnt.sysDept.list')")
    @ApiOperation(value = "获取部门详情")
    @GetMapping("/get/{id}")
    public Result get(@PathVariable String id) {
        SysDeptDO sysDept = sysDeptService.getById(id);
        SysDeptDO parent = sysDeptService.getById(sysDept.getParentId());
        if (parent != null) {
            sysDept.setParentName(parent.getName());
            sysDept.setParentId(parent.getId());
        }
        return Result.ok(sysDept);
    }


   // @Log(title = "部门管理", businessType = BusinessType.INSERT)
    @PreAuthorize("hasAuthority('bnt.sysDept.add')")
    @ApiOperation(value = "新增部门")
    @PostMapping("save")
    public Result save(@RequestBody SysDeptDO sysDept) {
        sysDeptService.createSysDept(sysDept);
        return Result.ok();
    }

    //@Log(title = "部门管理", businessType = BusinessType.UPDATE)
    @PreAuthorize("hasAuthority('bnt.sysDept.update')")
    @ApiOperation(value = "修改部门")
    @PutMapping("update")
    public Result updateById(@RequestBody SysDeptDO sysDept) {
        sysDeptService.updateById(sysDept);
        return Result.ok();
    }

    @PreAuthorize("hasAuthority('bnt.sysDept.remove')")

    @DeleteMapping("batchRemove")
    public Result batchRemove(@RequestBody List<String> idList) {
        boolean b = sysDeptService.removeByIds(idList);
        if (b) {
            return Result.ok();
        } else {
            return Result.fail();
        }
    }
}
