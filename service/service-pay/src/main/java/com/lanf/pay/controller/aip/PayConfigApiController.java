package com.lanf.pay.controller.aip;

import com.lanf.constant.result.Result;
import com.lanf.pay.model.vo.PayCompensateOrderRetryPolicyVO;
import com.lanf.pay.service.trade.IPayCompensateOrderRetryPolicyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/pay/api")
public class PayConfigApiController {

    @Autowired
    private IPayCompensateOrderRetryPolicyService retryPolicyService;

    @GetMapping("/getRetryPolicy")
    public Result<List<PayCompensateOrderRetryPolicyVO>> getRetryPolicy() {

        log.info("获取重试策略配置");

        return Result.ok(retryPolicyService.getRetryPolicy());

    }
}
