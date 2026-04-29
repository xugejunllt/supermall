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
    CancelPaidOrderResultBO cancelPaidOrder(String outTradeNo, BigDecimal refundAmount, String refundReason) throws MessageRetryConsumeException;

    /**
     * 支付宝转账
     *
     * @param outBizNo 商户转账唯一订单号
     * @param payeeAccount 收款方支付宝账号
     * @param amount 转账金额
     * @param remark 转账备注
     * @return 转账结果
     */
    TransferResult alipayTransfer(String outBizNo, String payeeAccount, BigDecimal amount, String remark) throws MessageRetryConsumeException;
    
    /**
     * 查询对账单下载URL
     *
     * @param billType 账单类型 trade-交易账单 signcustomer-签约客户账单
     * @param billDate 账单日期 格式 yyyy-MM-dd
     * @return 对账单下载URL结果
     */
    BillDownloadUrlResultBO queryBillDownloadUrl(String billType, String billDate);

}
