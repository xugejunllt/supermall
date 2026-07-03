package com.lanf.rocketmq.controller;


import com.lanf.constant.model.vo.PageResult;
import com.lanf.constant.result.Result;
import com.lanf.rocketmq.model.entity.MqConsumeMessageDO;
import com.lanf.rocketmq.model.entity.MqSendMessageDO;
import com.lanf.rocketmq.model.query.MqConsumeMessagePageQuery;
import com.lanf.rocketmq.model.query.MqSendMessagePageQuery;
import com.lanf.rocketmq.sevice.IMqConsumeMessageService;
import com.lanf.rocketmq.sevice.IMqSendMessageService;
import com.lanf.rocketmq.task.MqConsumeMessageRetryTask;
import com.lanf.rocketmq.task.MqMessageRetryTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author jarven
 * @since 2026-06-20
 */
@RestController
@RequestMapping("/mqMessage")
public class MqMessageController {

    @Autowired
    private IMqConsumeMessageService mqConsumeMessageService;

    @Autowired
    private IMqSendMessageService mqSendMessageService;
    @Autowired
    private MqConsumeMessageRetryTask mqConsumeMessageRetryTask;
    @Autowired
    private MqMessageRetryTask mqMessageRetryTask;


    /**
     * 分页查询MQ消费消息
     */
    @GetMapping("/mqConsumeMessagePageQuery")
    public Result<PageResult<MqConsumeMessageDO>> mqConsumeMessagePageQuery(MqConsumeMessagePageQuery query) {
        return Result.ok(mqConsumeMessageService.mqConsumeMessagePageQuery(query));
    }

    /**
     * 分页查询MQ发送消息
     */
    @GetMapping("/mqSendMessagePageQuery")
    public Result<PageResult<MqSendMessageDO>> mqSendMessagePageQuery(MqSendMessagePageQuery query) {
        return Result.ok(mqSendMessageService.mqSendMessagePageQuery(query));
    }

    /**
     * 手动开启消费消息扫描任务
     */
    @GetMapping("/consumeRetryPendingMessages")
    public Result<Void> consumeRetryPendingMessages() {
        mqConsumeMessageRetryTask.retryPendingMessages();
        return Result.ok();
    }

    /**
     * 手动开启发送消息扫描任务
     */
    @GetMapping("/sendRetryPendingMessages")
    public Result<Void> sendRetryPendingMessages() {
        mqMessageRetryTask.retryPendingMessages();
        return Result.ok();
    }


}

