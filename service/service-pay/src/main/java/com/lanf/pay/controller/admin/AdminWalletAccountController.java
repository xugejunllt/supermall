package com.lanf.pay.controller.admin;


import com.lanf.constant.result.Result;
import com.lanf.pay.model.dto.ApproveWithdrawDTO;
import com.lanf.pay.service.wallet.IWalletAccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 钱包账户表 前端控制器
 * </p>
 *
 * @author jarven
 * @since 2026-04-27
 */
@Slf4j
@RestController
@RequestMapping("/admin/walletAccount")
public class AdminWalletAccountController {



    @Autowired
    private IWalletAccountService walletAccountService;


    @PostMapping("/applyWithdraw")
    public Result<Void> applyWithdraw(@RequestBody @Validated ApproveWithdrawDTO dto){

        log.info("同意提现申请:dto{}", dto);

        walletAccountService.approveWithdraw(dto.getWithdrawId());

        return Result.ok();
    }


}

