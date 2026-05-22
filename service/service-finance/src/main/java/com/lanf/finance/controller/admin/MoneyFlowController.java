package com.lanf.finance.controller.admin;


import com.lanf.finance.service.IMoneyFlowService;
import com.lanf.rocketmq.util.RocketMqClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 资金流水 前端控制器
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-22
 */
@Slf4j
@RestController
@RequestMapping("/admin/moneyFlow")
public class MoneyFlowController {

    @Autowired
    private IMoneyFlowService moneyFlowService;
    @Autowired
    private RocketMqClient rocketMqClient;








}

