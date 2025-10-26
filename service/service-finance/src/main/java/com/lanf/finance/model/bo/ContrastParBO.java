package com.lanf.finance.model.bo;

import com.lanf.order.model.vo.OrderVO;
import com.lanf.pay.model.vo.OrderTradeVO;
import com.lanf.pay.model.vo.TradeStatusVO;
import lombok.Data;

import java.io.Serializable;

@Data
public class ContrastParBO implements Serializable {

    private OrderVO orderVO;

    private  OrderTradeVO orderTradeVO;

    private TradeStatusVO statusVO;

    public ContrastParBO(OrderVO orderVO, OrderTradeVO orderTradeVO, TradeStatusVO statusVO) {
        this.orderVO = orderVO;
        this.orderTradeVO = orderTradeVO;
        this.statusVO = statusVO;
    }
}
