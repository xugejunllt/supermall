package com.lanf.api.pay.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class TransferAccountsVO implements Serializable {


    private Date payFinishTime;

    //状态 0:转账完成，1：低于最低转账金额，无需转账
    private Integer status;

}
