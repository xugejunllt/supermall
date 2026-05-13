package com.lanf.goods.controller.admin;


import com.lanf.api.goods.model.query.StockPageQuery;
import com.lanf.api.goods.model.vo.StockPageVO;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.constant.result.Result;
import com.lanf.goods.service.stock.IStockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 库存 前端控制器
 * </p>
 *
 * @author jarven
 * @since 2025-11-29
 */
@Slf4j
@RestController
@RequestMapping("/admin/stock")
public class StockForAdminController {

    @Autowired
    private IStockService stockService;

    @GetMapping("/stockPageQuery")
    public Result<PageResult<StockPageVO>> stockPageQuery(StockPageQuery query) {
        log.info("分页查询库存:query{}", query);
        return Result.ok(stockService.stockPageQuery(query));
    }
}
