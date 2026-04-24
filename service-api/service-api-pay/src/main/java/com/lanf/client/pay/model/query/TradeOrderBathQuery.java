package com.lanf.client.pay.model.query;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 两种互斥查询条件 要么tradeOrderIdList 要么orderIdList
 */
@Data
public class TradeOrderBathQuery implements Serializable {

    private List<Long>   tradeOrderIdList;

    private List<Long> orderIdList;

}
