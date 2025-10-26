package com.lanf.storage.controller.stock;


import com.lanf.mybatis.base.PageResult;
import com.lanf.storage.model.entity.StockFlowDO;
import com.lanf.storage.model.query.StockFlowPageQuery;
import com.lanf.storage.service.stock.IStockFlowService;
import com.lanf.web.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 库存流水 前端控制器
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-05-30
 */
@Slf4j
@RestController
@RequestMapping("/stockFlow")
public class StockFlowController {

    @Autowired
    private IStockFlowService stockFlowService;

    @GetMapping("/stockFlowPage")
    public Result<PageResult<StockFlowDO>> stockFlowPage(StockFlowPageQuery query) {

        log.info("分页查询库存流水列表:query{}", query);

        return Result.ok(stockFlowService.stockFlowPage(query));
    }

}

