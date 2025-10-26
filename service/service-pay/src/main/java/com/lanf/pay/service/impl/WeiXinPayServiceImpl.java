package com.lanf.pay.service.impl;

import com.lanf.pay.model.bo.ReturnMoneyBO;
import com.lanf.pay.model.bo.TradeStatusBO;
import com.lanf.pay.model.bo.TransferAccountsBO;
import com.lanf.pay.model.dto.TradeOrderDTO;
import com.lanf.pay.model.bo.CallbackResultBO;
import com.lanf.pay.model.dto.TransferAccountsDTO;
import com.lanf.pay.model.vo.TradeOrderVO;
import com.lanf.pay.model.vo.TransferAccountsVO;
import com.lanf.pay.service.AbstractPayService;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;

@Service
public class WeiXinPayServiceImpl  extends AbstractPayService {


    @Override
    public TradeOrderVO createTradeOrder(TradeOrderDTO dto) {
        return null;
    }

    @Override
    public TradeStatusBO startQueryTradeStatus(String outTradeNo) {
        return null;
    }

    @Override
    public void closeTradeOrder(String outTradeNo) {

    }

    @Override
    public ReturnMoneyBO returnMoney(String outTradeNo, BigDecimal refundAmount,String outRequestNo) {
        return null;
    }


    @Override
    protected  void callbackResponse(HttpServletResponse response) {


    }

    @Override
    protected CallbackResultBO parse(HttpServletRequest request) {
        return null;
    }

    @Override
    public TransferAccountsBO doTransferAccounts(TransferAccountsDTO dto) {
        return null;
    }
}
