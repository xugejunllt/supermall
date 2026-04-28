package com.lanf.pay.controller.app;


import com.lanf.constant.result.Result;
import com.lanf.pay.model.dto.RechargeDTO;
import com.lanf.pay.model.dto.WithdrawApplyDTO;
import com.lanf.pay.model.vo.CreateRechargeTradeOrderVO;
import com.lanf.pay.service.trade.ITradeOrderService;
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
@RequestMapping("/walletAccount")
public class WalletAccountController {


    @Autowired
    private ITradeOrderService tradeOrderService;
    @Autowired
    private IWalletAccountService walletAccountService;

    /**
     * 创建充值交易单
     *
     *
     */
    @PostMapping("/createRechargeTradeOrder")
    public Result<CreateRechargeTradeOrderVO> createRechargeTradeOrder(@RequestBody RechargeDTO dto){

        log.info("创建充值交易单:dto{}", dto);

        return Result.ok(tradeOrderService.createRechargeTradeOrder(dto));
    }

    @PostMapping("/createRechargeTradeOrder")
    public Result<Void> applyWithdraw(@RequestBody @Validated WithdrawApplyDTO dto){

        log.info("申请提现:dto{}", dto);
        walletAccountService.applyWithdraw(dto);
        return Result.ok();
    }




}

