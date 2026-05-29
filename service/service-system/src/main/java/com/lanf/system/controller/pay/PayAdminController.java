package com.lanf.system.controller.pay;

import com.lanf.api.pay.api.PayApiService;
import com.lanf.api.pay.model.dto.AddPayAccountDTO;
import com.lanf.api.pay.model.query.PayAccountPageQuery;
import com.lanf.api.pay.model.vo.PayAccountPageVO;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.constant.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/pay")
public class PayAdminController {

    @Autowired
    private PayApiService payApiService;

    @PostMapping("/addPayAccount")
    public Result<Void> addPayAccount(@Validated @RequestBody AddPayAccountDTO dto) {

        log.info("添加支付账户");
        return payApiService.addPayAccount(dto);
    }

    @GetMapping("/payAccountPageQuery")
    public Result<PageResult<PayAccountPageVO>> payAccountPageQuery(@Validated PayAccountPageQuery query) {
        log.info("分页查询支付账户");

        return payApiService.payAccountPageQuery(query);
    }


}
