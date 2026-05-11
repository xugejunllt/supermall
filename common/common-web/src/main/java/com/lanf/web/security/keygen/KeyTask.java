package com.lanf.web.security.keygen;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author: Jarven
 * @date: 2026-02-25 13:46
 * @description:
 */

@Slf4j
@Component
public class KeyTask {

    @Autowired
    private KeyConfig keyConfig;

    @Resource(name = "encryptKeyManagerServiceImpl")
    private AbstractKeyManagerService keyManagerService;

    /**
     * 服务启动时 初始化Redis 队列秘钥数量
     * 初始数量为 KeyConfig.maxPoolSize
     *
     */
    @Scheduled(cron = "0 0 0 * * ?", zone = "Asia/Shanghai")
    public void keyGenInitTask(){

        log.info("准备启动生成密钥任务");

        if (!keyManagerService.canStart()){
            log.info("不满足启动条件");
            return;
        }
        keyManagerService.startKeyGen();

    }
    @Scheduled(cron = "0 0 * * * ?", zone = "Asia/Shanghai")
    public void scheduleKeyGenTask(){

        log.info("定时任务开始生成秘钥,生成数量[{}]", keyConfig.getNewKeysGenerated());
        keyManagerService.scheduleKeyGenTask();
        log.info("定时生成秘钥结束");


    }

}
