package com.lanf.pay.controller.admin;


import com.lanf.api.pay.model.dto.AddPayAccountDTO;
import com.lanf.api.pay.model.query.PayAccountPageQuery;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.constant.result.Result;
import com.lanf.api.pay.model.vo.PayAccountPageVO;
import com.lanf.pay.service.account.IPayAccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/admin/payAccount")
public class PayAccountController {

    @Autowired
    private IPayAccountService payAccountService;

    @PostMapping("/addPayAccount")
    public Result<Void> addPayAccount(@Validated @RequestBody AddPayAccountDTO dto) {

        log.info("添加支付账户:{}", dto);
        payAccountService.addPayAccount(dto);
        return Result.ok();
    }

    @GetMapping("/payAccountPageQuery")
    public Result<PageResult<PayAccountPageVO>> payAccountPageQuery(PayAccountPageQuery query) {

        log.info("分页查询支付账户");
        return Result.ok(payAccountService.payAccountPageQuery(query));
    }


}

