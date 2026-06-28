package com.lanf.pay.controller.app;


import com.lanf.constant.model.vo.PageResult;
import com.lanf.constant.result.Result;
import com.lanf.constant.utils.UserContext;
import com.lanf.pay.model.dto.BalanceOrderDTO;
import com.lanf.pay.model.dto.RechargeDTO;
import com.lanf.pay.model.dto.WithdrawApplyDTO;
import com.lanf.pay.model.query.WalletAccountFlowPageQuery;
import com.lanf.pay.model.vo.CreateRechargeTradeOrderVO;
import com.lanf.pay.model.vo.WalletAccountFlowPageVO;
import com.lanf.pay.model.vo.WalletAccountVO;
import com.lanf.pay.mq.listener.ClearingOrderListener;
import com.lanf.pay.mq.message.ClearingOrderMessage;
import com.lanf.pay.service.trade.ITradeOrderService;
import com.lanf.pay.service.wallet.IWalletAccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
@RequestMapping("/app/walletAccount")
public class WalletAccountController {


    @Autowired
    private ITradeOrderService tradeOrderService;
    @Autowired
    private IWalletAccountService walletAccountService;
    @Autowired
    private ClearingOrderListener clearingOrderListener;


    /**
     * 创建充值交易单
     */
    @PostMapping("/createRechargeTradeOrder")
    public Result<CreateRechargeTradeOrderVO> createRechargeTradeOrder(@RequestBody RechargeDTO dto) {

        log.info("创建充值交易单:dto{}", dto);

        return Result.ok(tradeOrderService.createRechargeTradeOrder(dto));
    }

    /**
     * 钱包余额下单
     *
     * @param dto
     * @return
     */
    @PostMapping("/balanceOrder")
    public Result<Void> balanceOrder(@RequestBody @Validated BalanceOrderDTO dto) {

        log.info("创建充值交易单:dto{}", dto);
        walletAccountService.balanceOrder(dto);
        return Result.ok();
    }

    /**
     * 申请提现
     */
    @PostMapping("/applyWithdraw")
    public Result<Void> applyWithdraw(@RequestBody @Validated WithdrawApplyDTO dto) {

        log.info("申请提现:dto{}", dto);
        walletAccountService.applyWithdraw(dto);
        return Result.ok();
    }


    @GetMapping("/walletAccountQuery")
    public Result<WalletAccountVO> walletAccountQuery() {

        log.info("查询钱包余额");

        return Result.ok(walletAccountService.walletAccountQuery(UserContext.getUserId()));
    }


    @GetMapping("/walletAccountFlowPageQuery")
    public Result<PageResult<WalletAccountFlowPageVO>> walletAccountFlowPageQuery(WalletAccountFlowPageQuery query) {

        log.info("分页查询钱包账号流水,query:{} ",query);

        return Result.ok(walletAccountService.walletAccountFlowPageQuery(query));
    }

    @PostMapping("/onMessage")
    public Result<Void> onMessage(@RequestBody ClearingOrderMessage message) {

        log.info("手动进行结算:dto{}", message);
        clearingOrderListener.onMessage( message);
        return Result.ok();
    }
}

