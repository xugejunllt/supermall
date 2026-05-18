package com.lanf.pay.controller.aip;

import com.lanf.api.pay.model.enums.PayChannelEnum;
import com.lanf.pay.model.dto.PayCallbackDTO;
import com.lanf.pay.service.pay.PaymentService;
import com.lanf.pay.service.pay.PaymentServiceFactory;
import com.lanf.pay.service.trade.ITradeOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@RequestMapping("/callback")
@Controller
public class PayCallbackController {

    @Autowired
    private ITradeOrderService tradeOrderService;


    @PostMapping("/aliPayCallback")
    public void aliPayCallback(HttpServletRequest request, HttpServletResponse response) {

        log.info("支付宝支付回调通知");
        PayCallbackDTO dto = new PayCallbackDTO();
        dto.setPayType(PayChannelEnum.ALI_PAY.getCode());
        dto.setRequest(request);
        dto.setResponse(response);
        PaymentService paymentService = PaymentServiceFactory.getPaymentService(PayChannelEnum.ALI_PAY.getCode());
        paymentService.payCallback(dto);


    }

}
