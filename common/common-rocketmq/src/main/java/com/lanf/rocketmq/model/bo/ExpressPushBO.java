package com.lanf.rocketmq.model.bo;

import lombok.Data;
import org.bouncycastle.cms.PasswordRecipientId;

import java.io.Serializable;


@Data
public class ExpressPushBO implements Serializable {


    private String message;

    private ExpressPushLastResultBO lastResult;

}
