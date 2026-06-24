//package com.lanf.dynamicsrrefresh.service.fegin;
//
//import com.lanf.dynamicsrrefresh.service.model.vo.ConfigVO;
//import com.lanf.dynamicsrrefresh.service.result.PageResult;
//import com.lanf.dynamicsrrefresh.service.result.Result;
//import org.springframework.cloud.openfeign.FeignClient;
//import org.springframework.stereotype.Component;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//
//@Component
//@FeignClient(name = "service-dynamicsrrefresh") //调用的服务名称
//public interface SystemLogService {
//    @GetMapping("/dynamicsrrefresh/pushConfig/getConfig")
//    public Result<PageResult<ConfigVO>> getConfig(@RequestParam("bizCode") Integer bizCode);
//
//
//}
