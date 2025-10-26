package com.lanf.storage.controller.supplier;


import com.lanf.mybatis.base.PageResult;
import com.lanf.storage.model.dto.SupplierAddDTO;
import com.lanf.storage.model.entity.SupplierDO;
import com.lanf.storage.model.query.SupplierPageQuery;
import com.lanf.storage.service.supplier.ISupplierService;
import com.lanf.web.result.Result;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 供应商 前端控制器
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-05-30
 */
@Slf4j
@RestController
@RequestMapping("/supplier")
public class SupplierController {

    @Autowired
    private ISupplierService supplierService;

    @ApiOperation(value = "分页查询供应商列表")
    @GetMapping("/supplierDOPage")
    public Result<PageResult<SupplierDO>> supplierPage(SupplierPageQuery query) {

        log.info("分页查询供应商列表:query{}", query);

        return Result.ok(supplierService.supplierPage(query));
    }

    @ApiOperation(value = "添加供应商")
    @PostMapping("/addSupplier")
    public Result addSupplier(@Validated @RequestBody SupplierAddDTO warehouse) {

        log.info("添加供应商:query{}", warehouse);
        supplierService.addSupplier(warehouse);

        return Result.ok();
    }

   // @PreAuthorize("hasAuthority('bnt.supplier.supplierList')")
    @GetMapping("/supplierList")
    public Result<List<SupplierDO>> supplierList() {
        log.info("查询供应商列表");
        return Result.ok(supplierService.supplierList());
    }

}

