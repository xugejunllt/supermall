package com.lanf.logistics.api;

import com.lanf.logistics.model.dto.LogisticsAddDTO;
import com.lanf.logistics.model.vo.LogisticsVO;
import com.lanf.constant.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Component
@FeignClient(name = "service-logistics", url = "localhost:9010") //调用的服务名称
public interface LogisticsApiService {


    @PostMapping("/logistics/api/logistics/logisticsAdd")
    Result logisticsAdd(@Validated @RequestBody LogisticsAddDTO addDTO);

    @GetMapping("/logistics/api/logistics/logisticsDetail")
    Result<LogisticsVO> logisticsDetail(@RequestParam("orderId") Long orderId);

}
