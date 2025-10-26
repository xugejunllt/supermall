package com.lanf.sms.service.manager.impl;


import com.lanf.sms.service.manager.SmsService;
import com.lanf.sms.service.manager.impl.config.AliyunSmsConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * 可视界面 配置文件值动态修改--服务实时监听到
 */
@Service
public class SmsFactory {

    @Autowired
    private AliyunSmsConfig aliyunSmsConfig;
    @Autowired
    private AliyunSmsServiceImpl aliSmsService;

    private RoundRobinStrategy<SmsService> roundRobinStrategy;


    @PostConstruct
    public void init() {
        List<SmsService> servers = findActiveSmsService();
        roundRobinStrategy = new RoundRobinStrategy<>(servers);
    }

    /**
     *
     * 获取激活的服务
     * 通过nacos 实现动态剔除
     */
    public List<SmsService> findActiveSmsService() {


        List<SmsService> smsServices = new ArrayList<>();
        if (aliyunSmsConfig.isActive()) {

            smsServices.add(aliSmsService);
        }

        if (smsServices.isEmpty()) {
            //默认返回阿里云短信
            smsServices.add(aliSmsService);
        }

        //按order值进行升序
        return smsServices;

    }

    /**
     * 通过轮训算法 获取SmsService 实现并行发送
     * <p>
     * 通过nacos 动态切换sms服务 监听key变更 剔除服务
     */
    public SmsService next() {


        return roundRobinStrategy.next();
    }

}
