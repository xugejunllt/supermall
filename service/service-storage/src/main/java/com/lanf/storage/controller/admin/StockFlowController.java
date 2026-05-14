package com.lanf.storage.controller.admin;


import com.lanf.constant.model.vo.PageResult;
import com.lanf.constant.result.Result;
import com.lanf.storage.model.query.StockFlowPageQuery;
import com.lanf.storage.model.vo.StockFlowPageVO;
import com.lanf.storage.service.stock.IStockFlowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping("/admin/stockFlow")
public class StockFlowController {

    @Autowired
    private IStockFlowService stockFlowService;

    @GetMapping("/stockFlowPageQuery")
    public Result<PageResult<StockFlowPageVO>> stockFlowPageQuery(StockFlowPageQuery query) {

        log.info("分页查询库存流水列表:query{}", query);

        return Result.ok(stockFlowService.stockFlowPageQuery(query));
    }

}

