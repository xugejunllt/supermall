package com.lanf.pay.service.pay;

import com.lanf.pay.model.bo.*;
import com.lanf.pay.model.dto.PayCallbackDTO;
import com.lanf.pay.model.dto.PrepayOrderDTO;
import com.lanf.pay.model.vo.PrepayOrderVO;
import com.lanf.rocketmq.exception.MessageRetryConsumeException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;

public interface PaymentService {


    /**
     * 支付回调
     *
     */
     void payCallback(PayCallbackDTO dto);

    /**
     * 支付回调 处理
     *
     *
     */
    PaySuccessHandleResultBO paySuccessHandleBO(PaySuccessHandleBO paySuccessHandleBO);
    /**
     * 创建预支付信息
     *
     *
     */
    PrepayOrderVO createPrepayOrder(PrepayOrderDTO dto);

    /**
     * 解析支付回调通知报文
     *
     */
    CallbackResultBO parse(HttpServletRequest request);
    /**
     * 查询支付订单支付状态
     *
     */
    TradeStatusBO queryTradeStatus(String outTradeNo);
    /**
     * 响应支付回调成功
     *
     */
    void responsePayOk(HttpServletResponse response);
    /**
     * 响应支付回调失败
     *
     */
    void responsePayFail(HttpServletResponse response);

    /**
     * 取消支付宝待支付订单
     *
     * @param outTradeNo 商户订单号
     */
    boolean cancelPendingOrder(String outTradeNo) throws MessageRetryConsumeException;

    /**
     * 取消支付宝已支付订单（发起退款）
     *
     * @param outTradeNo 商户订单号
     * @param refundAmount 退款金额
     * @param refundReason 退款原因
     * @return 是否成功
     */
    void cancelPaidOrder(String outTradeNo, BigDecimal refundAmount, String refundReason) throws MessageRetryConsumeException;

    /**
     * 转账
     *

     * @return 转账结果
     */
    TransferResult transfer(TransferBO transferBO) throws MessageRetryConsumeException;
    
    /**
     * 查询对账单下载URL
     *
     * @param billType 账单类型 trade-交易账单 signcustomer-签约客户账单
     * @param billDate 账单日期 格式 yyyy-MM-dd
     * @return 对账单下载URL结果
     */
    BillDownloadUrlResultBO queryBillDownloadUrl(String billType, String billDate);

    /**
     * 查询支付宝退款结果
     *
     * @param outTradeNo 商户订单号
     * @param outRequestNo 商户退款请求号
     * @return 退款查询结果
     */
    RefundQueryResultBO queryRefundResult(String outTradeNo, String outRequestNo);

    /**
     * 查询支付宝转账业务单据
     *
     * @param outBizNo 商户转账唯一订单号
     * @param orderId 支付宝转账单据号（与outBizNo二选一）
     * @return 转账查询结果
     */
    TransferQueryResultBO queryTransferResult(String outBizNo, String orderId);

}
