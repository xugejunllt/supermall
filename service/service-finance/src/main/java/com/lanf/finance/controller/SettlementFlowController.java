package com.lanf.finance.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 平台结算流水 前端控制器
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-20
 */
@Slf4j
@RestController
@RequestMapping("/settlementFlow")
public class SettlementFlowController {

    @PostMapping("/test")
    public String printTest(String si){

        log.info("[{}]开始,登入用户:[{}]","登入",si);
        ///
        log.info("[{}]结束","结束");


        return  "ok";
    }

    public static void main(String[] args) {

        String si = "test";

        log.info("[{}]开始,登入用户:[{}]","登入",si);
        ///
        log.info("[{}]成功","登入");

    }
}

