package com.lanf.pay.controller;


import com.lanf.pay.model.dto.BathPayDTO;
import com.lanf.pay.model.dto.OnePayDTO;
import com.lanf.pay.model.dto.TransferAccountsDTO;
import com.lanf.pay.model.vo.TradeOrderVO;
import com.lanf.pay.model.vo.TransferAccountsVO;
import com.lanf.pay.service.trade.impl.PayServiceAdapter;
import com.lanf.constant.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 支付订单 前端控制器
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-14
 */
@Slf4j
@RestController
@RequestMapping("/app/payOrder")
public class PayOrderController {

    @Autowired
    private PayServiceAdapter payServiceAdapter;

    @Deprecated
    @PostMapping("/preBathPay")
    public Result<TradeOrderVO> preBathPay(@RequestBody BathPayDTO dto) {

        log.info("批量支付，生成预支付信息:dto{}", dto);

        return Result.ok(payServiceAdapter.bathPay(dto));
    }
    @Deprecated
    @PostMapping("/preOnePay")
    public Result<TradeOrderVO> preOnePay(@RequestBody OnePayDTO dto) {

        log.info("单笔支付，生成预支付信息:dto{}", dto);

        return Result.ok(payServiceAdapter.onePay(dto));
    }
    @Deprecated
    @PostMapping("/transferAccounts")
    public Result<TransferAccountsVO> transferAccounts(@RequestBody TransferAccountsDTO dto) {

        log.info("进行转账dto：{}", dto);

        return Result.ok(payServiceAdapter.transferAccounts(dto));
    }

}

