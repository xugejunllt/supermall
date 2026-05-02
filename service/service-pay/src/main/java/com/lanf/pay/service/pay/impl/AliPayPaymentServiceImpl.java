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
import com.lanf.pay.config.AliPayConfig;
import com.lanf.pay.model.bo.*;
import com.lanf.pay.model.dto.PrepayOrderDTO;
import com.lanf.pay.model.enums.TradeStatusEnum;
import com.lanf.pay.model.vo.PrepayOrderVO;
import com.lanf.pay.service.pay.AbstractPaymentCallbackService;
import com.lanf.rocketmq.exception.MessageRetryConsumeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Service
public class AliPayPaymentServiceImpl extends AbstractPaymentCallbackService {

    @Value("${pay.ali.notifyUrl}")
    private String notifyUrl;
    @Autowired
    private AliPayConfig aliPayConfig;
    private static final Set<String> TRADE_NOT_EXIST_CODES = new HashSet<>(Arrays.asList(
            "ACQ.TRADE_NOT_EXIST",
            "ACQ.ENTERPRISE_PAY_BIZ_ERROR",
            "ACQ.INVALID_PARAMETER",
            "ACQ.SYSTEM_ERROR"
    ));

    public AliPayPaymentServiceImpl() {
    }

    @Override
    public CallbackResultBO parse(HttpServletRequest request) {

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
            log.error("支付宝验签失败", e);
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
        String totalAmount = params.get("total_amount");
        String buyerLogonId = params.get("buyer_logon_id");
        String sellerEmail = params.get("seller_email");
        String notifyTime = params.get("notify_time");
        String tradeNo = params.get("trade_no");
        String outTradeNo = params.get("out_trade_no");
        String passbackParams = params.get("passback_params");

        /////////
        Date payFinishTime = DateUtils.parse(gmtPayment, DateUtils.DATE_TIME);
        BigDecimal receiptMoney = new BigDecimal(receiptAmount);
        Date notifyTimeDate = DateUtils.parse(notifyTime, DateUtils.DATE_TIME);
        CallbackResultBO callbackResultBO = new CallbackResultBO();
        callbackResultBO.setPayFinishTime(payFinishTime);
        callbackResultBO.setReceiptMoney(receiptMoney);
        callbackResultBO.setTotalAmount(new BigDecimal(totalAmount));
        callbackResultBO.setPayAccount(buyerLogonId);
        callbackResultBO.setIncomeAccount(sellerEmail);
        callbackResultBO.setNotifyTime(notifyTimeDate);
        callbackResultBO.setTradeNo(tradeNo);
        callbackResultBO.setOutTradeNo(outTradeNo);
        callbackResultBO.setStrPassbackParams(passbackParams);
        callbackResultBO.setAllParams(JsonUtils.toJsonString( params));
        return callbackResultBO;
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
    @Override
    public TradeStatusBO queryTradeStatus(String outTradeNo) {

        log.info("查询支付宝交易状态:outTradeNo={}", outTradeNo);

        AlipayClient alipayClient = null;
        AlipayTradeQueryResponse response = null;

        try {
            alipayClient = new DefaultAlipayClient(getAlipayConfig());

            AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
            AlipayTradeQueryModel model = new AlipayTradeQueryModel();
            model.setOutTradeNo(outTradeNo);
            request.setBizModel(model);

            response = alipayClient.certificateExecute(request);
        } catch (AlipayApiException e) {
            log.error("查询支付宝交易状态异常:outTradeNo={}", outTradeNo, e);
            throw new BizException("查询交易状态异常");
        }

        if (!response.isSuccess()) {
            log.error("查询支付宝交易状态失败:outTradeNo={},subCode={},subMsg={}",
                    outTradeNo, response.getSubCode(), response.getSubMsg());
            throw new BizException("查询交易状态失败:" + response.getSubMsg());
        }

        return buildTradeStatusBO(response, outTradeNo);
    }

    /**
     * 查询三方支付单交易状态
     *
     *
     *
     */

    private TradeStatusBO buildTradeStatusBO(AlipayTradeQueryResponse response, String outTradeNo) {

        String code = response.getCode();

        if (TRADE_NOT_EXIST_CODES.contains(code)) {
            log.info("交易不存在或查询异常:outTradeNo={},code={}", outTradeNo, code);
            TradeStatusBO tradeStatusBO = new TradeStatusBO();
            tradeStatusBO.setTradeStatus(TradeStatusEnum.NOT_EXIST);
            return tradeStatusBO;
        }

        String tradeStatusStr = response.getTradeStatus();
        TradeStatusEnum tradeStatusEnum = TradeStatusEnum.fromAlipayStatus(tradeStatusStr);
        if (tradeStatusEnum.equals(TradeStatusEnum.UNKNOWN)) {

            log.error("查询到交易状态未知:outTradeNo={}", outTradeNo);
            throw new BizException("查询到交易状态未知");
        }

        TradeStatusBO tradeStatusBO = new TradeStatusBO();
        tradeStatusBO.setTradeStatus(tradeStatusEnum);
        tradeStatusBO.setOutTradeNo(response.getOutTradeNo());
        tradeStatusBO.setTradeNo(response.getTradeNo());
        tradeStatusBO.setTotalAmount(new BigDecimal(response.getTotalAmount()));
        tradeStatusBO.setReceiptMoney(new BigDecimal(response.getReceiptAmount()));
        /**
         * 返回当前时间 待修改
         */
        Date date = new Date();
        tradeStatusBO.setPayFinishTime(response.getSendPayDate());
        tradeStatusBO.setNotifyTime(date);
        tradeStatusBO.setPayAccount(response.getBuyerLogonId());
        /**
         * 暂不返回收款账户
         */
        tradeStatusBO.setIncomeAccount(null);
        String passbackParams = response.getPassbackParams();
        tradeStatusBO.setStrPassbackParams(passbackParams);
        tradeStatusBO.setAllParams(JsonUtils.toJsonString( response));
        return tradeStatusBO;
    }




    @Override
    public PrepayOrderVO createPrepayOrder(PrepayOrderDTO dto) {

        log.info("创建预支付单开始[{}]", JsonUtils.toJsonString(dto));
        AlipayClient alipayClient = null;
        try {
            alipayClient = new DefaultAlipayClient(getAlipayConfig());
        } catch (AlipayApiException e) {
            log.error("创建预支付单异常", e);
            throw new BizException("创建预支付单异常");
        }
        AlipayTradeAppPayRequest request = new AlipayTradeAppPayRequest();
        AlipayTradeAppPayModel model = new AlipayTradeAppPayModel();
        model.setSubject("商品下单支付");
        model.setOutTradeNo(dto.getOutTradeNo());
        //超时时间 一小时 这里参数是分钟 改一下
        model.setTimeoutExpress(dto.getExpireInterval() + "m");
        model.setTotalAmount(dto.getTotalAmount().toString());
        model.setPassbackParams(JsonUtils.toJsonString(dto.getPassbackParams()));
        request.setBizModel(model);
        request.setNotifyUrl(notifyUrl);
        AlipayTradeAppPayResponse response = null;
        try {
            //这里和普通的接口调用不同，使用的是sdkExecute
            response = alipayClient.sdkExecute(request);
        } catch (AlipayApiException e) {
            log.error("生成支付信息异常", e);
            throw new BizException("生成支付信息异常");
        }
        PrepayOrderVO vo = new PrepayOrderVO();
        vo.setOrderStr(response.getBody());

        log.info("创建预支付单结束");
        return vo;
    }

    @Override
    public void responsePayOk(HttpServletResponse response) {
        try {
            response.getWriter().write("success");
        } catch (Exception e) {
            log.error("响应支付回调成功异常", e);
        }
    }

    @Override
    public void responsePayFail(HttpServletResponse response) {
        try {
            response.getWriter().write("fail");
        } catch (Exception e) {
            log.error("响应支付回调失败异常", e);
        }
    }

    @Override
    public boolean cancelPendingOrder(String outTradeNo) throws MessageRetryConsumeException {
        log.info("取消支付宝待支付订单开始:outTradeNo={}", outTradeNo);

        AlipayClient alipayClient = null;
        try {
            alipayClient = new DefaultAlipayClient(getAlipayConfig());

            AlipayTradeCloseRequest request = new AlipayTradeCloseRequest();
            AlipayTradeCloseModel model = new AlipayTradeCloseModel();
            model.setOutTradeNo(outTradeNo);
            request.setBizModel(model);

            AlipayTradeCloseResponse response = alipayClient.certificateExecute(request);
            String code = response.getCode();
            if ( !"10000".equals( code)) {
                log.error("取消支付宝订单失败:outTradeNo={},code={},msg={}",
                        outTradeNo, code, response.getSubMsg());
               return false;
            }
            log.info("取消支付宝订单成功:outTradeNo={}", outTradeNo);
            return true;

        } catch (AlipayApiException e) {
            log.warn("取消支付宝订单异常:outTradeNo={}", outTradeNo, e);
            throw new MessageRetryConsumeException("取消订单异常");
        }
    }

    @Override
    public CancelPaidOrderResultBO cancelPaidOrder(String outTradeNo, BigDecimal refundAmount, String refundReason) throws MessageRetryConsumeException {


        log.info("取消支付宝已支付订单开始:outTradeNo={},refundAmount={},refundReason={}", 
                outTradeNo, refundAmount, refundReason);

        AlipayClient alipayClient = null;
        try {
            alipayClient = new DefaultAlipayClient(getAlipayConfig());

            AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
            AlipayTradeRefundModel model = new AlipayTradeRefundModel();
            model.setOutTradeNo(outTradeNo);
            model.setRefundAmount(refundAmount.toString());
            model.setRefundReason(refundReason != null ? refundReason : "订单取消退款");
            request.setBizModel(model);

            AlipayTradeRefundResponse response = alipayClient.certificateExecute(request);
            String code = response.getCode();
            String subCode = response.getSubCode();
            /**
             * 只负责发送请求 最终通过查询保证退款成功
             */
            if (  ! "10000".equals(code) && "ACQ.SYSTEM_ERROR".equals(subCode)  ) {
                log.warn("支付宝退款失败:outTradeNo={},code={},subCode={},msg={}",
                        outTradeNo, code, response.getSubCode(), response.getSubMsg());
                /**
                 * 系统错误 抛出异常重试
                 */

                throw new MessageRetryConsumeException("退款异常");
            } else if ( ! "10000".equals(code)) {

                /**
                 * 其他业务错误
                 */
                log.error("支付宝退款失败:outTradeNo={},code={},subCode={},msg={}",
                        outTradeNo, code, response.getSubCode(), response.getSubMsg());
                CancelPaidOrderResultBO cancelPaidOrderResultBO = new CancelPaidOrderResultBO();
                cancelPaidOrderResultBO.setResult(false);
                cancelPaidOrderResultBO.setErrorMsg(subCode+":"+response.getSubMsg());
                return new CancelPaidOrderResultBO();
            }
            
            log.info("支付宝退款成功:outTradeNo={},tradeNo={},refundFee={}", 
                    outTradeNo, response.getTradeNo(), response.getRefundFee());
            CancelPaidOrderResultBO bo = new CancelPaidOrderResultBO();
            bo.setResult( true);

            return bo;

        } catch (AlipayApiException e) {
            log.warn("支付宝退款异常:outTradeNo={}", outTradeNo, e);
            throw new MessageRetryConsumeException("退款异常");
        }
    }

    @Override
    public TransferResult alipayTransfer(String outBizNo, String payeeAccount, BigDecimal amount, String remark)
            throws MessageRetryConsumeException {
        log.info("支付宝转账开始:outBizNo={},payeeAccount={},amount={},remark={}", 
                outBizNo, payeeAccount, amount, remark);

        TransferResult resultBO = new TransferResult();
        resultBO.setOutBizNo(outBizNo);
        resultBO.setTransferAmount(amount);
        resultBO.setPayeeAccount(payeeAccount);

        AlipayClient alipayClient = null;
        try {
            alipayClient = new DefaultAlipayClient(getAlipayConfig());

            AlipayFundTransUniTransferRequest request = new AlipayFundTransUniTransferRequest();
            AlipayFundTransUniTransferModel model = new AlipayFundTransUniTransferModel();
            
            model.setOutBizNo(outBizNo);
            model.setProductCode("TRANS_ACCOUNT_NO_PWD");
            model.setBizScene("DIRECT_TRANSFER");
            model.setRemark(remark != null ? remark : "转账");
            
            com.alipay.api.domain.Participant payeeInfo = new com.alipay.api.domain.Participant();
            payeeInfo.setIdentity(payeeAccount);
            payeeInfo.setIdentityType("ALIPAY_LOGON_ID");
            model.setPayeeInfo(payeeInfo);
            
            model.setTransAmount(amount.toString());
            
            request.setBizModel(model);

            AlipayFundTransUniTransferResponse response = alipayClient.certificateExecute(request);
            String code = response.getCode();
            String status = response.getStatus();
            if ("10000".equals(code) && "SUCCESS".equals(status)) {

                resultBO.setStatus("SUCCESS");
                resultBO.setOrderId(response.getOrderId());
                resultBO.setFinishTime(DateUtils.parse(response.getTransDate(), DateUtils.DATE_TIME));
                resultBO.setTransferSuccess( true);
                log.info("支付宝转账成功:outBizNo={},orderId={}", outBizNo, response.getOrderId());

            } else if ("10003".equals(code)) {
                log.info("支付宝转账处理中:outBizNo={},orderId={}", outBizNo, response.getOrderId());
                throw new MessageRetryConsumeException("支付宝转账处理中");
            } else {
                resultBO.setTransferSuccess( false);
                log.error("支付宝转账失败:outBizNo={},code={},subCode={},msg={}",
                        outBizNo, code, response.getSubCode(), response.getSubMsg());
            }
            return resultBO;

        } catch (AlipayApiException e) {
            log.warn("支付宝转账异常:outBizNo={}", outBizNo, e);
            throw new MessageRetryConsumeException("转账异常");
        }
    }
    @Override
    public BillDownloadUrlResultBO queryBillDownloadUrl(String billType, String billDate) {
        log.info("查询支付宝对账单下载URL:billType={},billDate={}", billType, billDate);

        AlipayClient alipayClient = null;
        try {
            alipayClient = new DefaultAlipayClient(getAlipayConfig());
            AlipayDataDataserviceBillDownloadurlQueryRequest request = new AlipayDataDataserviceBillDownloadurlQueryRequest();
            AlipayDataDataserviceBillDownloadurlQueryModel model = new AlipayDataDataserviceBillDownloadurlQueryModel();
            model.setBillType(billType);
            model.setBillDate(billDate);
            request.setBizModel(model);
            AlipayDataDataserviceBillDownloadurlQueryResponse response = alipayClient.certificateExecute(request);

            BillDownloadUrlResultBO result = new BillDownloadUrlResultBO();
            result.setBillType(billType);
            result.setBillDate(billDate);

            if ( !(response.isSuccess() && "10000".equals(response.getCode()))) {
                log.warn("查询支付宝对账单下载URL失败:billType={},billDate={},subCode={},subMsg={}",
                        billType, billDate, response.getSubCode(), response.getSubMsg());
               throw new MessageRetryConsumeException("查询对账单下载URL异常");

            }
            result.setBillDownloadUrl(response.getBillDownloadUrl());
            result.setOriginalBillUrl(response.getBillDownloadUrl());

            log.info("查询支付宝对账单下载URL成功:billType={},billDate={},url={}",
                    billType, billDate, response.getBillDownloadUrl());
            return result;

        } catch (AlipayApiException e) {
            log.warn("查询支付宝对账单下载URL异常:billType={},billDate={}", billType, billDate, e);
            throw new MessageRetryConsumeException("查询对账单下载URL异常" );
        }
    }



}
