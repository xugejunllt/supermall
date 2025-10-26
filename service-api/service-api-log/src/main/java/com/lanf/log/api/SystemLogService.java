package com.lanf.log.api;

import com.lanf.log.model.dto.SysLoginLogDTO;
import com.lanf.log.model.dto.SysOperLogDTO;
import com.lanf.web.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Component
@FeignClient(name = "service-log") //调用的服务名称
public interface SystemLogService {
    @PostMapping("/log/sysOperLog/save")
    public Result save(@RequestBody SysOperLogDTO sysOperLog);

    @PostMapping("/log/sysLoginLog/save")
    public Result save(@RequestBody SysLoginLogDTO sysLoginLog);
}
