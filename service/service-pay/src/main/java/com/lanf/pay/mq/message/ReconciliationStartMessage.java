package com.lanf.pay.mq.message;

import com.lanf.pay.model.bo.ReconciliationTradeInfo;
import com.lanf.pay.model.enums.ReconciliationBusinessTypeEnum;
import com.lanf.pay.model.enums.ReconciliationJobTypeEnum;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ReconciliationStartMessage implements Serializable {
    /**
     * 该批次 最大的id
     */
    private Long bathMaxId;
    private String bathId;

    private  ReconciliationJobTypeEnum jobType;

    private ReconciliationBusinessTypeEnum reconciliationBusinessType;

    private List<ReconciliationTradeInfo> reconciliationTradeInfoList;



}
