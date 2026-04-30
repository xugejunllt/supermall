package com.lanf.pay.mq.message;

import com.lanf.pay.model.entity.FundBillDetailDO;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class FundBillDetailCompensationMessage implements Serializable {

   private List<FundBillDetailDO> cachedDataList;

}
