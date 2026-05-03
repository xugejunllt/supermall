package com.lanf.pay.model.bo;


import com.lanf.pay.model.enums.ReconciliationBusinessTypeEnum;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ReconciliationStart implements Serializable {
    /**
     * 该批次 最大的id
     */
    private Long bathMaxId;
    private String bathId;

    private ReconciliationBusinessTypeEnum reconciliationBusinessType;

    private List<ReconciliationTradeInfo> reconciliationTradeInfoList;

}
