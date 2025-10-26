package com.lanf.rocketmq.model.message;

import lombok.Data;

import java.io.Serializable;

@Data
public class ContrastBillTaskDTO implements Serializable {

    //对账任务id
    private Long contrastBillTaskId;

}
