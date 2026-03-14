package com.lanf.pay.service.pay;

import com.lanf.pay.model.dto.PrepayOrderDTO;
import com.lanf.pay.model.vo.PrepayOrderVO;

public interface PaymentService {

    /**
     * 创建预支付信息
     *
     *
     */
    PrepayOrderVO createPrepayOrder(PrepayOrderDTO dto);
}
