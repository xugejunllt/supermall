package com.lanf.api.pay.model.vo;

import com.lanf.api.pay.model.enums.PayChannelEnum;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class PayAccountPageVO implements Serializable {

    /**
     * 账户类型 0:支付宝
     */
    private PayChannelEnum accountType;

    /**
     * 账户
     */
    private String account;


    private Long id;

    private Date createTime;

    private Date updateTime;


}
