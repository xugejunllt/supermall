package com.lanf.pay.mq.message;

import com.lanf.constant.mq.base.BaseMessage;
import lombok.Data;

@Data
public class ClearingOrderMessage extends BaseMessage {

    private static final long serialVersionUID = 1L;

    /**
     * 清算单ID
     */
    private Long clearingDetailId;



}
