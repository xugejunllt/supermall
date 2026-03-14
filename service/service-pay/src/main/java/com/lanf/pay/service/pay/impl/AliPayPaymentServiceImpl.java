package com.lanf.pay.service.pay.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayConfig;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.*;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.*;
import com.alipay.api.response.*;
import com.lanf.common.utils.DateUtils;
import com.lanf.common.utils.JsonUtils;
import com.lanf.constant.exception.BizException;
import com.lanf.pay.model.bo.CallbackResultBO;
import com.lanf.pay.model.bo.ReturnMoneyBO;
import com.lanf.pay.model.bo.TradeStatusBO;
import com.lanf.pay.model.bo.TransferAccountsBO;
import com.lanf.pay.model.dto.PrepayOrderDTO;
import com.lanf.pay.model.dto.TransferAccountsDTO;
import com.lanf.pay.model.vo.PrepayOrderVO;
import com.lanf.pay.service.pay.PaymentService;
import com.lanf.pay.service.pay.config.AliPayConfig;
import com.lanf.web.utils.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Slf4j
@Service
public class AliPayPaymentServiceImpl implements PaymentService {

    @Value("${pay.ali.notifyUrl}")
    private String notifyUrl;
    @Autowired
    private AliPayConfig aliPayConfig;





    protected CallbackResultBO parse(HttpServletRequest request) {

        Map<String, String> params = new HashMap<String, String>();
        Map requestParams = request.getParameterMap();
        for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext(); ) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            //乱码解决，这段代码在出现乱码时使用。
            //valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
            params.put(name, valueStr);
        }
        //切记alipaypublickey是支付宝的公钥，请去open.alipay.com对应应用下查看。
        AlipayConfig alipayConfig = getAlipayConfig();
        //boolean AlipaySignature.rsaCheckV1(Map<String, String> params, String publicKey, String charset, String sign_type)
        try {
            boolean flag = AlipaySignature.rsaCertCheckV1(params, alipayConfig.getAlipayPublicCertPath(), alipayConfig.getCharset(),
                    alipayConfig.getSignType());
            if (!flag) {
                log.error("支付宝验签失败:{}", params);
                throw new BizException("支付宝验签失败");
            }


        } catch (AlipayApiException e) {
            e.printStackTrace();
            throw new BizException("支付宝验签失败");
        }
        String tradeStatus = params.get("trade_status");
        if (!"TRADE_SUCCESS".equals(tradeStatus)) {
            log.error("交易付款失败:{}", params);
            throw new BizException("交易付款失败");
        }

        return buildCallbackResultBO(params);
    }


    private CallbackResultBO buildCallbackResultBO(Map<String, String> params) {

        String gmtPayment = params.get("gmt_payment");
        String receiptAmount = params.get("receipt_amount");

        String buyerLogonId = params.get("buyer_logon_id");
        String sellerEmail = params.get("seller_email");
        String notifyTime = params.get("notify_time");
        String tradeNo = params.get("trade_no");
        String outTradeNo = params.get("out_trade_no");
        String passbackParams = params.get("passback_params");

        /////////
        Date payFinishTime = DateUtils.parse(gmtPayment, DateUtils.DATE_TIME);
        BigDecimal receiptMoney = new BigDecimal(receiptAmount);
        String payAccount = buyerLogonId;
        String incomeAccount = sellerEmail;
        Date notifyTimeDate = DateUtils.parse(notifyTime, DateUtils.DATE_TIME);
        CallbackResultBO callbackResultBO = new CallbackResultBO();
        callbackResultBO.setPayFinishTime(payFinishTime);
        callbackResultBO.setReceiptMoney(receiptMoney);
        callbackResultBO.setPayAccount(payAccount);
        callbackResultBO.setIncomeAccount(incomeAccount);
        callbackResultBO.setNotifyTime(notifyTimeDate);
        callbackResultBO.setTradeNo(tradeNo);
        callbackResultBO.setOutTradeNo(outTradeNo);
        callbackResultBO.setBathPay(Boolean.parseBoolean(passbackParams));
        return callbackResultBO;
    }

    protected void callbackResponse(HttpServletResponse response) {
        ResponseUtil.out(response,"success");

    }


    public TransferAccountsBO doTransferAccounts(TransferAccountsDTO dto) {


        // 初始化SDK
        AlipayClient alipayClient = null;
        AlipayFundTransUniTransferResponse response = null;
        try {
            alipayClient = new DefaultAlipayClient(getAlipayConfig());
            // 构造请求参数以调用接口
            AlipayFundTransUniTransferRequest request = new AlipayFundTransUniTransferRequest();
            AlipayFundTransUniTransferModel model = new AlipayFundTransUniTransferModel();
            // 设置商家侧唯一订单号
            model.setOutBizNo(dto.getOutBizNo());
            // 设置订单总金额
            //model.setTransAmount(BigDecimalUtil.format(dto.getTransAmount()));
            // 设置描述特定的业务场景
            model.setBizScene("DIRECT_TRANSFER");
            // 设置业务产品码
            model.setProductCode("TRANS_ACCOUNT_NO_PWD");
            // 设置转账业务的标题
            model.setOrderTitle(dto.getOrderTitle());
            // 设置收款方信息
            Participant payeeInfo = new Participant();
            payeeInfo.setCertType("IDENTITY_CARD");
            payeeInfo.setCertNo(dto.getCertNo());
            payeeInfo.setIdentity(dto.getIncomeAccount());
            payeeInfo.setName(dto.getName());
            payeeInfo.setIdentityType("ALIPAY_LOGON_ID");
            model.setPayeeInfo(payeeInfo);
            // 设置转账业务请求的扩展参数
            model.setBusinessParams("{\"payer_show_name_use_alias\":\"true\"}");
            request.setBizModel(model);
            response = alipayClient.certificateExecute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
            throw new BizException("转账失败");
        }
        String status = response.getStatus();
        if (response.isSuccess()) {
            if (!"SUCCESS".equals(status)) {
                throw new BizException("转账失败");
            }
            log.info("转账成功");

            return buildTransferAccountsBO(response);

        } else {
            throw new BizException("转账失败");

        }

    }

    private TransferAccountsBO buildTransferAccountsBO(AlipayFundTransUniTransferResponse response) {

        TransferAccountsBO transferAccountsBO = new TransferAccountsBO();
        transferAccountsBO.setOrderId(response.getOrderId());
        transferAccountsBO.setPayFinishTime(DateUtils.parse(response.getTransDate(), DateUtils.DATE_TIME));
        return transferAccountsBO;
    }

    private AlipayConfig getAlipayConfig() {

        String privateKey = aliPayConfig.getPrivateKey();
        AlipayConfig alipayConfig = new AlipayConfig();
        alipayConfig.setPrivateKey(privateKey);
        alipayConfig.setServerUrl(aliPayConfig.getServerUrl());
        alipayConfig.setAppId(aliPayConfig.getAppId());
        alipayConfig.setCharset(aliPayConfig.getCharset());
        alipayConfig.setSignType(aliPayConfig.getSignType());
        alipayConfig.setFormat(aliPayConfig.getFormat());
        alipayConfig.setAppCertPath(aliPayConfig.getAppCertPath());
        alipayConfig.setAlipayPublicCertPath(aliPayConfig.getAlipayPublicCertPath());
        alipayConfig.setRootCertPath(aliPayConfig.getRootCertPath());
        return alipayConfig;
    }

    public TradeStatusBO startQueryTradeStatus(String outTradeNo) {

        // 初始化SDK
        AlipayClient alipayClient = null;
        AlipayTradeQueryResponse response = null;
        try {
            alipayClient = new DefaultAlipayClient(getAlipayConfig());

            // 构造请求参数以调用接口
            AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
            AlipayTradeQueryModel model = new AlipayTradeQueryModel();
            // 设置订单支付时传入的商户订单号
            model.setOutTradeNo(outTradeNo);
            request.setBizModel(model);
            response = alipayClient.certificateExecute(request);
        } catch (AlipayApiException e) {

            e.printStackTrace();
            log.error("支付异常");
            throw new BizException("支付异常");
        }
        if (response.isSuccess()) {
            Boolean exist = true;
            Integer tradeStatus = null;
            TradeStatusBO tradeOrderBO = new TradeStatusBO();
            String code = response.getCode();
            if ("ACQ.TRADE_NOT_EXIST".equals(code)) {
                exist = false;
                tradeOrderBO.setExist(exist);
                return tradeOrderBO;
            }
            String tradeStatus1 = response.getTradeStatus();
            if ("WAIT_BUYER_PAY".equals(tradeStatus1)) {
                tradeStatus = 0;
            } else if ("TRADE_SUCCESS".equals(tradeStatus1)) {
                tradeStatus = 1;
            } else if ("TRADE_FINISHED".equals(tradeStatus1)){

                tradeStatus = 2;
            } else if ("TRADE_CLOSED".equals(tradeStatus1)){

                tradeStatus = 3;
            }
            tradeOrderBO.setTradeStatus(tradeStatus);
            tradeOrderBO.setExist(exist);
            tradeOrderBO.setTotalAmount(new BigDecimal(response.getTotalAmount()));
            tradeOrderBO.setReceiptAmount(new BigDecimal(response.getReceiptAmount()));
            return tradeOrderBO;
        } else {

            throw new BizException("交易单查询异常");
        }


    }

    public void closeTradeOrder(String outTradeNo) {

        AlipayClient alipayClient = null;
        AlipayTradeCloseResponse response = null;
        try {
            alipayClient = new DefaultAlipayClient(getAlipayConfig());

            AlipayTradeCloseRequest request = new AlipayTradeCloseRequest();
            AlipayTradeCloseModel model = new AlipayTradeCloseModel();
            model.setTradeNo(outTradeNo);
            request.setBizModel(model);
            response = alipayClient.certificateExecute(request);
        } catch (AlipayApiException e) {
            throw new RuntimeException(e);
        }
        if (response.isSuccess()) {

            String code = response.getCode();
            if (!"10000".equals(code)) {
                throw new BizException("关闭交易单失败");
            }

        } else {
            throw new BizException("关闭交易单失败");
        }
    }

    public ReturnMoneyBO returnMoney(String outTradeNo,BigDecimal refundAmount,String outRequestNo) {
        AlipayClient alipayClient = null;
        AlipayTradeRefundResponse response = null;
        try {
            alipayClient = new DefaultAlipayClient(getAlipayConfig());

            AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
            AlipayTradeRefundModel model = new AlipayTradeRefundModel();
            model.setOutTradeNo(outTradeNo);
            model.setRefundAmount(refundAmount.toString());
            model.setOutRequestNo(outRequestNo);
            request.setBizModel(model);
            response = alipayClient.certificateExecute(request);
        } catch (AlipayApiException e) {
            throw new RuntimeException(e);
        }
        if (response.isSuccess()) {

            String code = response.getCode();
            if (!"10000".equals(code)) {
                throw new BizException("退款失败");
            }
            ReturnMoneyBO returnMoneyBO = new ReturnMoneyBO();
            returnMoneyBO.setRefundMoney(new BigDecimal(response.getRefundFee()));
            returnMoneyBO.setTradeNo(response.getTradeNo());
            return returnMoneyBO;

        } else {
            throw new BizException("退款失败");
        }
    }


    @Override
    public PrepayOrderVO createPrepayOrder(PrepayOrderDTO dto) {

        log.info("创建预支付单开始[{}]", JsonUtils.toJsonString(dto));
        AlipayClient alipayClient = null;
        try {
            alipayClient = new DefaultAlipayClient(getAlipayConfig());
        } catch (AlipayApiException e) {
            log.error("创建预支付单异常",e);
            throw new BizException("创建预支付单异常");
        }
        AlipayTradeAppPayRequest request = new AlipayTradeAppPayRequest();
        AlipayTradeAppPayModel model = new AlipayTradeAppPayModel();
        model.setSubject("商品下单支付");
        model.setOutTradeNo(dto.getOutTradeNo());
        //超时时间 一小时
        model.setTimeoutExpress(60 + "m");
        model.setTotalAmount(dto.getTotalAmount().toString());

        model.setPassbackParams(dto.getBathPay().toString());
        request.setBizModel(model);
        request.setNotifyUrl(notifyUrl);
        AlipayTradeAppPayResponse response = null;
        try {
            //这里和普通的接口调用不同，使用的是sdkExecute
            response = alipayClient.sdkExecute(request);
        } catch (AlipayApiException e) {
            log.error("生成支付信息异常",e);
            throw new BizException("生成支付信息异常");
        }
        PrepayOrderVO vo = new PrepayOrderVO();
        vo.setOrderStr(response.getBody());

        log.info("创建预支付单结束");
        return vo;
    }
}
