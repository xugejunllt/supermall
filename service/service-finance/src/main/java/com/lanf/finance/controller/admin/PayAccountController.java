package com.lanf.finance.controller.admin;


import com.lanf.finance.model.dto.PayAccountAddDTO;
import com.lanf.finance.model.entity.PayAccountDO;
import com.lanf.finance.model.query.AccountMoneySumQuery;
import com.lanf.finance.model.query.PayAccountPageQuery;
import com.lanf.finance.model.vo.AccountMoneySumVO;
import com.lanf.finance.service.IMoneyFlowService;
import com.lanf.finance.service.IPayAccountService;
import com.lanf.mybatis.base.PageResult;
import com.lanf.constant.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/admin/payAccount")
public class PayAccountController {

    @Autowired
    private IPayAccountService payAccountService;
    @Autowired
    private IMoneyFlowService moneyFlowService;
    @PostMapping("/payAccountAdd")
    public Result payAccountAdd(@Validated @RequestBody PayAccountAddDTO dto) {

        log.info("添加支付账户");
        payAccountService.payAccountAdd(dto);
        return Result.ok();
    }

    @GetMapping("/payAccountPage")
    public Result<PageResult<PayAccountDO>> payAccountPage(PayAccountPageQuery query) {

        log.info("分页查询支付账户");
        return Result.ok(payAccountService.payAccountPage(query));
    }
    @GetMapping("/accountMoneySumQuery")
    public Result<AccountMoneySumVO> accountMoneySumQuery(AccountMoneySumQuery query) {

        log.info("账户资金汇总:query{}", query);

        return Result.ok(moneyFlowService.accountMoneySumQuery(query));
    }

}

