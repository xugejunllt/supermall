package com.lanf.storage.controller.admin;


import com.lanf.constant.model.vo.PageResult;
import com.lanf.constant.result.Result;
import com.lanf.storage.model.dto.AddWarehouseDTO;
import com.lanf.storage.model.query.WarehousePageQuery;
import com.lanf.storage.model.vo.WarehousePageVO;
import com.lanf.storage.service.warehous.IWarehouseService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping("/admin/warehouse")
public class WarehouseController {

    @Autowired
    private IWarehouseService warehouseService;

    @GetMapping("/warehousePageQuery")
    public Result<PageResult<WarehousePageVO>> warehousePageQuery(WarehousePageQuery query) {

        log.info("分页查询仓库列表:query{}", query);

        return Result.ok(warehouseService.warehousePageQuery(query));
    }

    @PostMapping("/addWarehouse")
    public Result<Void> addWarehouse(@Validated @RequestBody AddWarehouseDTO warehouse) {


        log.info("添加仓库:query{}", warehouse);
        warehouseService.addWarehouse(warehouse);

        return Result.ok();
    }

}

