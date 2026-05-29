package com.lanf.finance.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class PayAccountPageVO implements Serializable {
    /**
     * 账户类型 0:支付宝
     */
    private Integer accountType;

    /**
     * 账户
     */
    private String account;

    //初期余额
    private BigDecimal startRemainMoney;

    //当前余额
    private BigDecimal remainMoney;
    private Long id;

    private Date createTime;

    private Date updateTime;


}
