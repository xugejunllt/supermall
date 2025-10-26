package com.lanf.pay.model.bo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class TransferAccountsBO implements Serializable {




    @ApiModelProperty(value = "转账订单号")
    private String orderId;



    //转账完成时间
    private Date payFinishTime;


}
