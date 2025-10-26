package com.lanf.storage.controller.warehous;


import com.lanf.mybatis.base.PageResult;
import com.lanf.storage.model.dto.WarehouseAddDTO;
import com.lanf.storage.model.entity.WarehouseDO;
import com.lanf.storage.model.query.WarehousePageQuery;
import com.lanf.storage.service.warehous.IWarehouseService;
import com.lanf.web.result.Result;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 仓库 前端控制器
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-05-30
 */
@Slf4j
@RestController
@RequestMapping("/warehouse")
public class WarehouseController {

    @Autowired
    private IWarehouseService warehouseService;

    @ApiOperation(value = "分页查询仓库列表")
    @GetMapping("/ware" +
            "housePage")
    public Result<PageResult<WarehouseDO>> warehousePage(WarehousePageQuery query) {

        log.info("分页查询仓库列表:query{}",query);

        return Result.ok( warehouseService.warehousePage(query));
    }

    @ApiOperation(value = "添加仓库")
    @PostMapping("/addWarehouse")
    public Result addWarehouse(@Validated @RequestBody WarehouseAddDTO warehouse) {


        log.info("添加仓库:query{}",warehouse);
        warehouseService.addWarehouse(warehouse);

        return Result.ok( );
    }

}

