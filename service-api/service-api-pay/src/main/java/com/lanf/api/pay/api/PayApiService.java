package com.lanf.api.pay.api;

import com.lanf.api.pay.model.dto.AddPayAccountDTO;
import com.lanf.api.pay.model.dto.CreateMergeTradeOrderDTO;
import com.lanf.api.pay.model.dto.CreateTradeOrderDTO;
import com.lanf.api.pay.model.query.PayAccountPageQuery;
import com.lanf.api.pay.model.vo.CreateMergeTradeOrderVO;
import com.lanf.api.pay.model.vo.PayAccountPageVO;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.constant.result.Result;
import org.dromara.hmily.annotation.Hmily;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Component
@FeignClient(name = "service-pay",url = "localhost:9009")
public interface PayApiService {

    @Hmily
    @PostMapping("/pay/api/createPayOrder")
    public Result<Void> createPayOrder(@RequestBody CreateTradeOrderDTO dto);

    @Hmily
    @PostMapping("/pay/api/createMergeTradeOrder")
    public Result<CreateMergeTradeOrderVO> createMergeTradeOrder(@RequestBody CreateMergeTradeOrderDTO dto);

    @PostMapping("/pay/admin/payAccount/addPayAccount")
    public Result<Void> addPayAccount(@Validated @RequestBody AddPayAccountDTO dto);


    @GetMapping("/pay/admin/payAccount/payAccountPageQuery")
    public Result<PageResult<PayAccountPageVO>> payAccountPageQuery(@SpringQueryMap PayAccountPageQuery query);





}

