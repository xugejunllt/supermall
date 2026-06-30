package com.lanf.pay.controller.admin;

import com.lanf.api.pay.model.dto.ApproveWithdrawDTO;
import com.lanf.api.pay.model.query.WalletAccountFlowPageQuery;
import com.lanf.api.pay.model.query.WalletAccountPageQuery;
import com.lanf.api.pay.model.query.WalletWithdrawPageQuery;
import com.lanf.api.pay.model.vo.WalletAccountFlowPageVO;
import com.lanf.api.pay.model.vo.WalletAccountPageVO;
import com.lanf.api.pay.model.vo.WalletWithdrawPageVO;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.constant.result.Result;
import com.lanf.pay.service.wallet.IWalletAccountFlowService;
import com.lanf.pay.service.wallet.IWalletAccountService;
import com.lanf.pay.service.wallet.IWalletWithdrawService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/admin/walletWithdraw")
public class WalletWithdrawController {

    @Autowired
    private IWalletWithdrawService walletWithdrawService;

    @Autowired
    private IWalletAccountService walletAccountService;

    @Autowired
    private IWalletAccountFlowService walletAccountFlowService;
    /**
     * 分页查询钱包提现记录
     */
    @GetMapping("/walletWithdrawPageQuery")
    public Result<PageResult<WalletWithdrawPageVO>> walletWithdrawPageQuery(WalletWithdrawPageQuery query) {
        log.info("分页查询钱包提现记录:{}", query);
        return Result.ok(walletWithdrawService.walletWithdrawPageQuery(query));
    }

    @PostMapping("/applyWithdraw")
    public Result<Void> applyWithdraw(@RequestBody @Validated ApproveWithdrawDTO dto){

        log.info("同意提现申请:dto{}", dto);

        walletAccountService.approveWithdraw(dto.getWithdrawId());

        return Result.ok();
    }

    /**
     * 分页查询钱包账户
     */
    @GetMapping("/walletAccountPageQuery")
    public Result<PageResult<WalletAccountPageVO>> walletAccountPageQuery(WalletAccountPageQuery query) {
        log.info("分页查询钱包账户: {}", query);
        return Result.ok(walletAccountService.walletAccountPageQuery(query));
    }

    @GetMapping("/walletAccountFlowPageQuery")
    public Result<PageResult<WalletAccountFlowPageVO>> walletAccountFlowPageQuery(WalletAccountFlowPageQuery query) {
        log.info("分页查询钱包账户流水: {}", query);
        return Result.ok(walletAccountFlowService.walletAccountFlowPageQuery(query));
    }


}
