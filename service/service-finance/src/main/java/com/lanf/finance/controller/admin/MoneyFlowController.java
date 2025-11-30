package com.lanf.finance.controller.admin;


import com.lanf.finance.model.entity.MoneyFlowDO;
import com.lanf.finance.model.query.AccountMoneySumQuery;
import com.lanf.finance.model.query.MoneyFlowPageQuery;
import com.lanf.finance.service.IMoneyFlowService;
import com.lanf.finance.task.ContrastBillTask;
import com.lanf.mybatis.base.PageResult;
import com.lanf.rocketmq.model.message.MoneyFlowDTO;
import com.lanf.rocketmq.util.RocketMqClient;
import com.lanf.constant.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
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
    @Autowired
    private ContrastBillTask contrastBillTask;

    @GetMapping("/moneyFlowPage")
    public Result<PageResult<MoneyFlowDO>> moneyFlowPage(MoneyFlowPageQuery query) {

        log.info("分页查询资金流水:{}",query);

        return Result.ok(moneyFlowService.moneyFlowPage(query));
    }





    @GetMapping("/sendMq")
    public String sendMq(AccountMoneySumQuery query) {

        log.info("手动发送mq:query{}", query);
        MoneyFlowDTO moneyFlowDTO = new MoneyFlowDTO();
        contrastBillTask.startContrastBillTask();

        return "ok";
    }

}

