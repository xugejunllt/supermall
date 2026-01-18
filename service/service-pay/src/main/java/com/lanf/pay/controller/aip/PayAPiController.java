package com.lanf.pay.controller.aip;

import com.lanf.constant.result.Result;
import com.lanf.pay.model.dto.CreateTradeOrderDTO;
import com.lanf.pay.service.ITradeOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Slf4j
public class PayAPiController {
    @Autowired
    private ITradeOrderService tradeOrderService;

    @PostMapping("/createPayOrder")
    public Result<Void> createPayOrder(@Validated @RequestBody CreateTradeOrderDTO dto)  {

        log.info("创建交易单:dto{}", dto);
        tradeOrderService.createTradeOrder(dto);
        return Result.ok();
    }





}
