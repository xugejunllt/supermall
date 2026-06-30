package com.lanf.pay.controller.admin;

import com.lanf.constant.model.vo.PageResult;
import com.lanf.constant.result.Result;
import com.lanf.pay.model.dto.ApproveWithdrawDTO;
import com.lanf.pay.model.query.WalletWithdrawPageQuery;
import com.lanf.pay.model.vo.WalletWithdrawPageVO;
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

}
