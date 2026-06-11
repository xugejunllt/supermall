package com.lanf.user.controller.app;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.lanf.constant.exception.BizException;
import com.lanf.constant.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/app/sentinel")
public class SentinelTestController {

    @SentinelResource(
            value = "createOrder"
            , blockHandler = "createOrderBlockHandler"
            , exceptionsToIgnore = {BizException.class}
    )
    @GetMapping("/exceptionTest")
    public Result<String> exceptionTest() {
        log.info("异常测试开始");
        // 模拟异常
        try {
            int i = 1 / 0;
        } catch (Exception e) {
            throw new BizException("业务异常");
        }
        return Result.ok("异常测试成功");
    }

    /**
     * blockHandler 降级方法
     */
    public Result<String> createOrderBlockHandler(BlockException e)  {

        log.error("触发降级方法");
        return Result.fail("熔断,接口降级");
    }


}
