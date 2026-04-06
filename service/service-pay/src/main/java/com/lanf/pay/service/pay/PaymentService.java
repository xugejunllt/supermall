package com.lanf.pay.service.pay;

import com.lanf.pay.model.bo.CallbackResultBO;
import com.lanf.pay.model.dto.PrepayOrderDTO;
import com.lanf.pay.model.vo.PrepayOrderVO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface PaymentService {

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
     * 响应支付回调成功
     *
     */
    void responsePayOk(HttpServletResponse response);
    /**
     * 响应支付回调失败
     *
     */
    void responsePayFail(HttpServletResponse response);
}
