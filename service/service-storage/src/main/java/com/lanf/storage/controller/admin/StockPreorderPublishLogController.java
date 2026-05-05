package com.lanf.storage.controller.admin;

import com.lanf.constant.result.Result;
import com.lanf.storage.model.dto.PublishStockDTO;
import com.lanf.storage.service.stock.IStockPreorderPublishLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/admin/stockPreorderPublishLog")
public class StockPreorderPublishLogController {

    @Autowired
    private IStockPreorderPublishLogService stockPreorderPublishLogService;

    @PostMapping("/recycleStock")
    public Result<Void> recycleStock(@Validated @RequestBody PublishStockDTO publishStock) {

        log.info("发布预售库存:dto{}", publishStock);
        stockPreorderPublishLogService.publishStock(publishStock);
        return Result.ok();
    }

}
