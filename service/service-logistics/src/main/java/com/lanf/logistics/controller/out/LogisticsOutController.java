package com.lanf.logistics.controller.out;


import com.lanf.common.utils.LogFormatUtils;
import com.lanf.logistics.service.impl.LogisticsTrackServiceImpl;
import com.lanf.logistics.service.manager.LogisticsManagerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 物流信息 前端控制器
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-07-25
 */
@Slf4j
@Controller
@RequestMapping("/out")
public class LogisticsOutController {

    @Autowired
    private LogisticsManagerService logisticsManagerService;
    @Autowired
    private LogisticsTrackServiceImpl logisticsTrackService;

    @PostMapping("/expressPush")
    public void expressPush(HttpServletRequest request, HttpServletResponse response) {

        LogFormatUtils.printFormatLog(log, "快递信息推送开始");
        logisticsManagerService.expressPush(request, response, logisticsTrackService);

    }
}

