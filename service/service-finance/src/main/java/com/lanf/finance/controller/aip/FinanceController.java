package com.lanf.finance.controller.aip;

import com.lanf.constant.result.Result;
import com.lanf.finance.model.dto.PayAccountDTO;
import com.lanf.finance.model.vo.PayAccountApiVO;
import com.lanf.finance.mq.listener.ClearingOrderListener;
import com.lanf.finance.mq.message.ClearingOrderMessage;
import com.lanf.finance.service.IPayAccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api")
public class FinanceController {

    @Autowired
    private IPayAccountService payAccountService;
    @Autowired
    private ClearingOrderListener clearingOrderListener;

    @PostMapping("/payAccountQuery")
    public Result<PayAccountApiVO> payAccountQuery(@RequestBody PayAccountDTO dto) {

        log.info("查询支付账户:dto{}", dto);

        return Result.ok(payAccountService.payAccountQuery(dto));
    }

    @PostMapping("/onMessage")
    public Result<Void> onMessage(@RequestBody ClearingOrderMessage message) {

        log.info("手动进行结算:dto{}", message);
        clearingOrderListener.onMessage( message);
        return Result.ok();
    }
}
