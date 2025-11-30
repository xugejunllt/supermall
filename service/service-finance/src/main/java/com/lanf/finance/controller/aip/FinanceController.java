package com.lanf.finance.controller.aip;

import com.lanf.finance.model.dto.PayAccountDTO;
import com.lanf.finance.model.vo.PayAccountApiVO;
import com.lanf.finance.service.IPayAccountService;
import com.lanf.constant.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/financeApi")
public class FinanceController {

    @Autowired
    private IPayAccountService payAccountService;

    @PostMapping("/payAccountQuery")
    public Result<PayAccountApiVO> payAccountQuery(@RequestBody PayAccountDTO dto) {

        log.info("查询支付账户:dto{}", dto);

        return Result.ok(payAccountService.payAccountQuery(dto));
    }


}
