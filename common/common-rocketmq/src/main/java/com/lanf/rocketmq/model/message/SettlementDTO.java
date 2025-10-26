package com.lanf.rocketmq.model.message;

import lombok.Data;

import java.io.Serializable;

@Data
public class SettlementDTO implements Serializable {

    private Long liquidationId;

}
