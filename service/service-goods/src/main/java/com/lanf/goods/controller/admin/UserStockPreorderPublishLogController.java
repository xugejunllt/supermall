package com.lanf.goods.controller.admin;

import com.lanf.constant.result.Result;
import com.lanf.goods.model.dto.RecycleStockDTO;
import com.lanf.goods.service.stock.IUserStockPreorderPublishLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
