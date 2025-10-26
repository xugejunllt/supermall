package com.lanf.rocketmq.model.message;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class AfterSalesOrderStatusChangeDTO implements Serializable {



    private List<Long> afterSalesOrderIdList;
    //0:换货出库成功
    private Integer event;


}
