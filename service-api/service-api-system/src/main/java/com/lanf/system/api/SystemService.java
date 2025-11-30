package com.lanf.system.api;

import com.lanf.system.model.vo.ShopVO;
import com.lanf.constant.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Component
@FeignClient(name = "service-system",url = "localhost:9002") //调用的服务名称
public interface SystemService {

    @GetMapping("/system/systemApi/shopQuery")
    public Result<List<ShopVO>> shopQuery(@RequestParam("idList")List<Long> idList);
    @GetMapping("/system/systemApi/getPlatformShopId")
    public Result<Long> getPlatformShopId();

    @GetMapping("/system/systemApi/getTenantCodeByShopId")
    public Result<String> getTenantCodeByShopId(@RequestParam("shopId")Long shopId);
}
