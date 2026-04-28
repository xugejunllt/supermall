package com.lanf.aftersales.api;

import com.lanf.aftersales.model.dto.UnderAfterSaleDTO;
import com.lanf.constant.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Component
@FeignClient(name = "service-aftersales",url = "localhost:9009")
public interface AfterSalesOrderApiService {

    @PostMapping("/afterSales/orderApi/isUnderAfterSale")
    Result<Boolean> isUnderAfterSale(@RequestBody UnderAfterSaleDTO dto);
}
