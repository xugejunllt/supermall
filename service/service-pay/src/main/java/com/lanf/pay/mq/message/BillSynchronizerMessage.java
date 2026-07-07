package com.lanf.pay.mq.message;

import com.lanf.api.pay.model.enums.PayChannelEnum;
import com.lanf.constant.mq.base.BaseMessage;
import lombok.Data;

@Data
public class BillSynchronizerMessage extends BaseMessage {

    private PayChannelEnum payChannel;

    private String billType;

    private String billDate;

    private String flowNo;




}
