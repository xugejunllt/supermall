package com.lanf.pay.api;

import com.lanf.constant.result.Result;
import com.lanf.pay.model.dto.CreateMergeTradeOrderDTO;
import com.lanf.pay.model.dto.CreateTradeOrderDTO;
import com.lanf.pay.model.dto.PlaceSinglePayOrderDTO;
import com.lanf.pay.model.dto.TransferAccountsDTO;
import com.lanf.pay.model.query.TradeOrderBathQuery;
import com.lanf.pay.model.vo.*;
import org.dromara.hmily.annotation.Hmily;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Component
@FeignClient(name = "service-pay",url = "localhost:9009")
public interface PayApiService {

    @Hmily
    @PostMapping("/pay/api/createPayOrder")
    public Result<Void> createPayOrder(@RequestBody CreateTradeOrderDTO dto);


    @Hmily
    @PostMapping("/pay/api/createMergeTradeOrder")
    public Result<CreateMergeTradeOrderVO> createMergeTradeOrder(@RequestBody CreateMergeTradeOrderDTO dto);




    @Deprecated
    @Hmily
    @PostMapping("/pay/api/pay/placeSinglePayOrder")
    public Result<CreatePayOrderVO> placeSinglePayOrder(@RequestBody PlaceSinglePayOrderDTO dto);

    @Deprecated
    @PostMapping("/pay/payApi/createPayOrder")
    public Result<CreatePayOrderVO> createPayOrder(@RequestBody PlaceSinglePayOrderDTO dto);

    @Deprecated
    @GetMapping("/pay/payApi/queryOrderTradeByOrderId")
    public Result<OrderTradeVO> queryOrderTradeByOrderId(@RequestParam("orderId") Long orderId);

    @Deprecated
    @PostMapping("/pay/payApi/transferAccounts")
    public Result<TransferAccountsVO> transferAccounts(@RequestBody TransferAccountsDTO dto);

    @Deprecated
    @PostMapping("/pay/payApi/tradeOrderBathQuery")
    public Result<List<TradeOrderBathVO>> tradeOrderBathQuery(@RequestBody TradeOrderBathQuery query);

    @Deprecated
    @GetMapping("/pay/payApi/queryTradeStatus")
    public Result<TradeStatusVO> queryTradeStatus(@RequestParam("orderId") Long orderId);
}

