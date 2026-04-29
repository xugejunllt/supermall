package com.lanf.pay.model.bo;

import com.lanf.client.pay.model.enums.PayChannelEnum;
import lombok.Data;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

@Data
public class BillSynchronizer implements Serializable {

    private PayChannelEnum payChannel;

    private String billType;

    private String billDate;

    private AtomicInteger retryCount ;

}
