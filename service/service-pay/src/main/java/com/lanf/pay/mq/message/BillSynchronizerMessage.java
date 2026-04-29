package com.lanf.pay.mq.message;

import com.lanf.client.pay.model.enums.PayChannelEnum;
import lombok.Data;

import java.io.Serializable;

@Data
public class BillSynchronizerMessage implements Serializable {

    private PayChannelEnum payChannel;

    private String billType;

    private String billDate;
}
