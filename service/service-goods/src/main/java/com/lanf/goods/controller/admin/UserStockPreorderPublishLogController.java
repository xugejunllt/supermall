package com.lanf.goods.controller.admin;

import com.lanf.api.goods.model.dto.RecycleStockDTO;
import com.lanf.api.goods.model.query.UserStockPreorderPublishLogPageQuery;
import com.lanf.api.goods.model.vo.UserStockPreorderPublishLogPageVO;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.constant.result.Result;
import com.lanf.goods.service.stock.IUserStockPreorderPublishLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/admin/userStockPreorderPublishLog")
public class UserStockPreorderPublishLogController {

    @Autowired
    private IUserStockPreorderPublishLogService userStockPreorderPublishLogService;

    @PostMapping("/recycleStock")
    public Result<Void> recycleStock(@Validated @RequestBody RecycleStockDTO recycleStockDTO) {
        log.info("回收库存:dto{}", recycleStockDTO);
        userStockPreorderPublishLogService.recycleStock(recycleStockDTO);
        return Result.ok();
    }

    @GetMapping("/userStockPreorderPublishLogPageQuery")
    public Result<PageResult<UserStockPreorderPublishLogPageVO>> userStockPreorderPublishLogPageQuery(UserStockPreorderPublishLogPageQuery query) {
        log.info("分页查询库存预售发布日志:query{}", query);
        return Result.ok(userStockPreorderPublishLogService.userStockPreorderPublishLogPageQuery(query));
    }
}
