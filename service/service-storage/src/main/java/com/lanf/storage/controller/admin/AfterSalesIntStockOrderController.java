package com.lanf.storage.controller.admin;


import com.lanf.api.storage.model.dto.AfterSalesIntStockDTO;
import com.lanf.constant.result.Result;
import com.lanf.storage.service.storage.IAfterSalesIntStockOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 销售出库单 前端控制器
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-09
 */
@Slf4j
@RestController
@RequestMapping("/admin/afterSalesIntStockOrder")
public class AfterSalesIntStockOrderController {


    @Autowired
    private IAfterSalesIntStockOrderService afterSalesIntStockOrderService;

    @PostMapping("/inStock")
    public Result<Void> inStock(@Validated @RequestBody AfterSalesIntStockDTO dto) {

        log.info("销售入库单入库:dto{}", dto);
        afterSalesIntStockOrderService.inStock(dto);
        return Result.ok();
    }


}

