package com.lanf.logistics.controller.api;


import com.lanf.common.utils.LogFormatUtils;
import com.lanf.common.utils.LogInfo;
import com.lanf.logistics.model.dto.ExpressAddDTO;
import com.lanf.logistics.model.dto.LogisticsAddDTO;
import com.lanf.logistics.model.vo.LogisticsVO;
import com.lanf.logistics.service.ILogisticsService;
import com.lanf.web.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/api/logistics")
public class LogisticsApiController {

    @Autowired
    private ILogisticsService logisticsService;

    @PostMapping("/logisticsAdd")
    public Result logisticsAdd(@Validated @RequestBody LogisticsAddDTO addDTO) {

        LogFormatUtils.printFormatLog( log, "添加物流信息", Arrays.asList(new LogInfo("number",
                addDTO.getNumber())),addDTO);
        logisticsService.logisticsAdd(addDTO);

        return Result.ok();
    }

    @GetMapping("/logisticsDetail")
    public Result<LogisticsVO> logisticsDetail(@RequestParam("orderId") Long orderId) {

        LogFormatUtils.printFormatLog( log, "根据订单id查询物流详细", Arrays.asList(new LogInfo("orderId",
                orderId.toString())),"");

        return Result.ok( logisticsService.logisticsDetail( orderId));
    }

}

