package com.lanf.pay.controller.aip;

import com.lanf.pay.model.bo.CallbackResultBO;
import com.lanf.pay.model.dto.PayCallbackDTO;
import com.lanf.pay.service.impl.PayServiceAdapter;
import com.lanf.web.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@RequestMapping("/callback")
@Controller
public class PayCallbackController {

    @Autowired
    private PayServiceAdapter payServiceAdapter;

    @PostMapping("/aliPayCallback")
    public void aliPayCallback(HttpServletRequest request, HttpServletResponse response) {

        log.info("支付宝支付回调通知");
        PayCallbackDTO dto = new PayCallbackDTO();
        dto.setPayType(0);
        dto.setRequest(request);
        dto.setResponse(response);
        payServiceAdapter.payCallback(dto);
    }

}
