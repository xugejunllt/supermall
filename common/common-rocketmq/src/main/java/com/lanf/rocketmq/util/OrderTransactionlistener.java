package com.lanf.rocketmq.util;

import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;

public class OrderTransactionlistener implements TransactionListener {


    @Override
    public LocalTransactionState executeLocalTransaction(Message message, Object o) {

        return null;
    }

    @Override
    public LocalTransactionState checkLocalTransaction(MessageExt messageExt) {
        return null;
    }
}
