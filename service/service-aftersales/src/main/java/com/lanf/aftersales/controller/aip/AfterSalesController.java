package com.lanf.aftersales.controller.aip;

import com.lanf.aftersales.model.dto.UnderAfterSaleDTO;
import com.lanf.aftersales.service.IAfterSalesOrderService;
import com.lanf.constant.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/afterSales")
public class AfterSalesController {

    @Autowired
    private IAfterSalesOrderService afterSalesOrderService;

    /**
     * 是否存在售后 中
     *
     *
     */
    @PostMapping("/isUnderAfterSale")
    public Result<Boolean> isUnderAfterSale(@RequestBody UnderAfterSaleDTO dto) {

        log.info("查询支付账户:dto{}", dto);

        return Result.ok(afterSalesOrderService.isUnderAfterSale(dto));
    }


}
