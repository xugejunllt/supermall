package com.lanf.pay.controller.aip;

import com.alipay.api.AlipayApiException;
import com.lanf.common.utils.BeanCopyUtils;
import com.lanf.pay.model.bo.TradeStatusBO;
import com.lanf.pay.model.dto.CreatePayOrderDTO;
import com.lanf.pay.model.dto.PlaceSinglePayOrderDTO;
import com.lanf.pay.model.dto.TradeOrderQuantitySumDTO;
import com.lanf.pay.model.dto.TransferAccountsDTO;
import com.lanf.pay.model.query.TradeOrderBathQuery;
import com.lanf.pay.model.query.TradeOrderQuery;
import com.lanf.pay.model.vo.*;
import com.lanf.pay.service.ITradeOrderService;
import com.lanf.pay.service.impl.PayServiceAdapter;
import com.lanf.constant.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api")
public class OrderController {

    @Autowired
    private ITradeOrderService tradeOrderService;
    @Autowired
    private PayServiceAdapter payServiceAdapter;


    @PostMapping("/placeSinglePayOrder")
    public Result<Void> placeSinglePayOrder(@RequestBody PlaceSinglePayOrderDTO dto)  {

        log.info("下达单笔支付订单:dto{}", dto);
        tradeOrderService.placeSinglePayOrder(dto);
        return Result.ok();
    }

    @PostMapping("/createPayOrder")
    public Result<CreatePayOrderVO> createPayOrder(@RequestBody List<CreatePayOrderDTO> dto) throws AlipayApiException {

        log.info("创建支付订单:dto{}", dto);
        CreatePayOrderVO payOrder = tradeOrderService.createPayOrder(dto);
        return Result.ok(payOrder);
    }

    @GetMapping("/queryOrderTradeByOrderId")
    public Result<OrderTradeVO> queryOrderTradeByOrderId(@RequestParam("orderId") Long orderId) {

        log.info("查询交易信息:orderId{}", orderId);

        return Result.ok(tradeOrderService.queryOrderTradeByOrderId(orderId));
    }

    /**
     * 单个查询 共用一个接口 批量查询--用一个接口
     *
     */
    @PostMapping("/tradeOrderQuery")
    public Result<TradeOrderApiVO> tradeOrderQuery(@RequestBody TradeOrderQuery query) {

        log.info("查询交易信息:query{}", query);

        return Result.ok(tradeOrderService.tradeOrderQuery(query));
    }

    @PostMapping("/transferAccounts")
    public Result<TransferAccountsVO> transferAccounts(@RequestBody TransferAccountsDTO dto) {

        log.info("进行转账:dto{}", dto);
        return Result.ok(payServiceAdapter.transferAccounts(dto));
    }



    @PostMapping("/tradeOrderQuantitySum")
    public Result<Integer> tradeOrderQuantitySum(@RequestBody TradeOrderQuantitySumDTO dto) {

        log.info("统计交易单数量:dto{}", dto);
        return Result.ok(tradeOrderService.tradeOrderQuantitySum(dto));
    }

    @PostMapping("/tradeOrderBathQuery")
    public Result<List<TradeOrderBathVO>> tradeOrderBathQuery(@RequestBody TradeOrderBathQuery query) {

        log.info("批量查询交易单信息:query{}", query);
        return Result.ok(tradeOrderService.tradeOrderBathQuery(query));
    }

    @GetMapping("/queryTradeStatus")
    public Result<TradeStatusVO> queryTradeStatus(@RequestParam("orderId")Long orderId) {

        log.info("查询交易单交易状态:orderId{}", orderId);
        TradeStatusBO tradeStatusBO = payServiceAdapter.queryTradeStatus(orderId);

        return Result.ok(BeanCopyUtils.copyBean(tradeStatusBO, TradeStatusVO.class));
    }

}
