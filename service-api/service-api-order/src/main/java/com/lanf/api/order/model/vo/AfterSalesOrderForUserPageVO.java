package com.lanf.api.order.model.vo;

import com.lanf.constant.model.enums.order.MainStatusEnum;
import com.lanf.constant.model.enums.order.SubStatus;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class AfterSalesOrderForUserPageVO implements Serializable {


    private Long id;
    /**
     * 售后单编号
     */
    private String orderNumber;
    /**
     *
     */
    private MainStatusEnum mainStatus;
    /**
     * 子状态
     */
    private SubStatus subStatus;

    private Date createTime;

    private Date updateTime;
}
