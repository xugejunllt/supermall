package com.lanf.pay.api;

import com.lanf.pay.model.dto.CreatePayOrderDTO;
import com.lanf.pay.model.dto.TransferAccountsDTO;
import com.lanf.pay.model.query.TradeOrderBathQuery;
import com.lanf.pay.model.vo.*;
import com.lanf.constant.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Component
@FeignClient(name = "service-pay",url = "localhost:9008") //调用的服务名称
public interface PayApiService {

    @PostMapping("/pay/payApi/createPayOrder")
    public Result<CreatePayOrderVO> createPayOrder(@RequestBody List<CreatePayOrderDTO> dto);
    @GetMapping("/pay/payApi/queryOrderTradeByOrderId")
    public Result<OrderTradeVO> queryOrderTradeByOrderId(@RequestParam("orderId") Long orderId);

    @PostMapping("/pay/payApi/transferAccounts")
    public Result<TransferAccountsVO> transferAccounts(@RequestBody TransferAccountsDTO dto);


    @PostMapping("/pay/payApi/tradeOrderBathQuery")
    public Result<List<TradeOrderBathVO>> tradeOrderBathQuery(@RequestBody TradeOrderBathQuery query);


    @GetMapping("/pay/payApi/queryTradeStatus")
    public Result<TradeStatusVO> queryTradeStatus(@RequestParam("orderId")Long orderId);
}
