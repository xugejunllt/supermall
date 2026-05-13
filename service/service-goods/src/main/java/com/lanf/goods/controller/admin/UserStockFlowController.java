package com.lanf.goods.controller.admin;


import com.lanf.api.goods.model.query.UserStockFlowPageQuery;
import com.lanf.api.goods.model.vo.UserStockFlowPageVO;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.constant.result.Result;
import com.lanf.goods.service.stock.IUserStockFlowService;
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
 * @author jarven
 * @since 2026-01-03
 */
@Slf4j
@RestController
@RequestMapping("/admin/userStockFlow")
public class UserStockFlowController {

    @Autowired
    private IUserStockFlowService userStockFlowService;

    @GetMapping("/userStockFlowPageQuery")
    public Result<PageResult<UserStockFlowPageVO>> userStockFlowPageQuery(UserStockFlowPageQuery query) {
        log.info("分页查询库存流水:query{}", query);
        return Result.ok(userStockFlowService.userStockFlowPageQuery(query));
    }
}
