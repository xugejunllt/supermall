package com.lanf.storage.controller.api;

import com.lanf.storage.model.dto.SalesInStockOrderAddDTO;
import com.lanf.storage.model.vo.StockVO;
import com.lanf.storage.service.stock.IStockService;
import com.lanf.storage.service.storage.ISalesOutStockOrderService;
import com.lanf.constant.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/storageApi")
public class StorageApiController {

    @Autowired
    private ISalesOutStockOrderService salesOutStockOrderService;
    @Autowired
    private IStockService stockService;

    @PostMapping("/salesStockOrderAdd")
    public Result salesStockOrderAdd(@RequestBody List<SalesInStockOrderAddDTO> dtoList) {

        log.info("创建售后换货出入库单:dtoList{}", dtoList);
        salesOutStockOrderService.salesStockOrderAdd(dtoList);
        return Result.ok();
    }
    @PostMapping("/querySkuCodeList")
    public Result< List<StockVO>> querySkuCodeList(@RequestBody List<String> skuCodeList) {

        log.info("根据skuCode查询商品库存:skuCodeList{}", skuCodeList);
        return Result.ok( stockService.querySkuCodeList(skuCodeList));
    }
}
