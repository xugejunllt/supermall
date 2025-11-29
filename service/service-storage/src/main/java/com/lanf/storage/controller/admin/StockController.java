package com.lanf.storage.controller.admin;


import com.lanf.mybatis.base.PageResult;
import com.lanf.storage.model.query.StockPageQuery;
import com.lanf.storage.model.vo.StockPageQueryVO;
import com.lanf.storage.service.stock.IStockService;
import com.lanf.web.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-07
 */
@Slf4j
@RestController
@RequestMapping("/admin/stock")
public class StockController {

    @Autowired
    private IStockService stockService;
    @GetMapping("/stockPage")
    public Result<PageResult<StockPageQueryVO>> stockPage(StockPageQuery query) {

        log.info("分页查询库存列表:query{}",query);

        return Result.ok(stockService.stockPage(query));
    }
}

