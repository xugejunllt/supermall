package com.lanf.order.model.vo;

import com.lanf.order.model.enums.MainStatusEnum;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class AfterSalesOrderForUserPageVO implements Serializable {


    private Long id;
    /**
     *
     */
    private MainStatusEnum mainStatus;

    private List<AfterSalesOrderItemVO> afterSalesOrderItemVOList;


}
