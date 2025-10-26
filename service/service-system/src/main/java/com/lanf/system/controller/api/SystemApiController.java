package com.lanf.system.controller.api;

import com.lanf.system.model.vo.ShopVO;
import com.lanf.system.service.company.IShopService;
import com.lanf.web.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/systemApi")
public class SystemApiController {

    @Autowired
    private IShopService shopService;

    @GetMapping("/shopQuery")
    public Result<List<ShopVO>> shopQuery(@RequestParam("idList") List<Long> idList) {

        log.info("根据id查询店铺信息:id{}", idList);
        return Result.ok(shopService.shopQuery(idList));
    }

    @GetMapping("/getPlatformShopId")
    public Result<Long> getPlatformShopId() {

        log.info("查询平台店铺id");
        return Result.ok(shopService.getPlatformShopId());
    }

    @GetMapping("/getTenantCodeByShopId")
    public Result<String> getTenantCodeByShopId(@RequestParam("shopId")Long shopId) {

        log.info("根据店铺id查询租户code:{}", shopId);

        return Result.ok(shopService.getTenantCodeByShopId(shopId));
    }

}
