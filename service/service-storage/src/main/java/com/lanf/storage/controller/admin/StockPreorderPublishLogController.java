package com.lanf.storage.controller.admin;

import com.lanf.constant.model.vo.PageResult;
import com.lanf.constant.result.Result;
import com.lanf.api.storage.model.dto.PublishStockDTO;
import com.lanf.api.storage.model.query.StockPreorderPublishLogPageQuery;
import com.lanf.api.storage.model.vo.StockPreorderPublishLogPageVO;
import com.lanf.storage.service.stock.IStockPreorderPublishLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
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

    @PostMapping("/publishStock")
    public Result<Void> publishStock(@Validated @RequestBody PublishStockDTO publishStock) {

        log.info("发布预售库存:dto{}", publishStock);
        stockPreorderPublishLogService.publishStock(publishStock);
        return Result.ok();
    }

    @GetMapping("/stockPreorderPublishLogPageQuery")
    public Result<PageResult<StockPreorderPublishLogPageVO>> stockPreorderPublishLogPageQuery(StockPreorderPublishLogPageQuery query) {
        log.info("分页查询库存预售发布日志:query{}", query);
        return Result.ok(stockPreorderPublishLogService.stockPreorderPublishLogPageQuery(query));
    }

}
