package com.lanf.constant.mq.base;

import com.lanf.constant.utils.MessageLevelUtils;
import com.lanf.constant.utils.TraceIdUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

@Slf4j
@Data
public class BaseMessage implements Serializable {

    protected String traceId;
    protected Integer level;
    protected String messageId;

    public BaseMessage() {

        traceId = TraceIdUtils.getTraceId();
        if (traceId == null){
            traceId = "sys:"+ TraceIdUtils.generateTraceId() ;
        }
        level = MessageLevelUtils.getLevel();
        messageId = TraceIdUtils.generateTraceId();
    }
}
