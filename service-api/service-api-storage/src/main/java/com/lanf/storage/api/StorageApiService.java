package com.lanf.storage.api;

import com.lanf.storage.model.dto.SalesInStockOrderAddDTO;
import com.lanf.storage.model.vo.StockVO;
import com.lanf.web.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Component
@FeignClient(name = "service-storage",url = "localhost:9004") //调用的服务名称
public interface StorageApiService {

    @PostMapping("/storage/storageApi/salesStockOrderAdd")
    public Result salesInStockOrderAdd(@RequestBody List<SalesInStockOrderAddDTO> dtoList);

    @PostMapping("/storage/storageApi/querySkuCodeList")
    public Result< List<StockVO>> querySkuCodeList(@RequestBody List<String> skuCodeList);

}
