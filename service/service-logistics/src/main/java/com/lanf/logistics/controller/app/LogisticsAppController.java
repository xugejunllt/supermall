package com.lanf.logistics.controller.app;


import com.lanf.common.utils.LogFormatUtils;
import com.lanf.common.utils.LogInfo;
import com.lanf.logistics.model.dto.LogisticsAddDTO;
import com.lanf.logistics.model.vo.LogisticsVO;
import com.lanf.logistics.service.ILogisticsService;
import com.lanf.web.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

/**
 * <p>
 * 物流信息 前端控制器
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-07-25
 */
@Slf4j
@RestController
@RequestMapping("/app/logistics")
public class LogisticsAppController {

    @Autowired
    private ILogisticsService logisticsService;
    @GetMapping("/logisticsDetail")
    public Result<LogisticsVO> logisticsDetail(@Validated Long orderId) {

        LogFormatUtils.printFormatLog( log, "根据订单id查询物流详细", Arrays.asList(new LogInfo("orderId",
                orderId.toString())),"");

        return Result.ok( logisticsService.logisticsDetail( orderId));
    }
}

