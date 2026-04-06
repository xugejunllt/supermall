package com.lanf.pay.controller.app;


import com.lanf.constant.result.Result;
import com.lanf.pay.model.dto.CreatePrepayOrderDTO;
import com.lanf.pay.model.vo.CreatePrepayOrderVO;
import com.lanf.pay.service.trade.ITradeOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 交易订单 前端控制器
 * </p>
 *
 *
 * @since 2024-06-14
 */
@Slf4j
@RestController
@RequestMapping("/app/tradeOrder")
public class TradeOrderController {

    @Autowired
    private ITradeOrderService tradeOrderService;

    @PostMapping("/createPrepayOrder")
    public Result<CreatePrepayOrderVO> createPrepayOrder(@RequestBody CreatePrepayOrderDTO dto){

        log.info("创建预支付订单:dto{}", dto);
        return Result.ok(tradeOrderService.createPrepayOrder(dto));
    }






}

