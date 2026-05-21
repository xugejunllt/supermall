package com.lanf.constant.mq.base;

import com.lanf.constant.utils.TraceIdUtils;
import lombok.Data;

import java.io.Serializable;
@Data
public class BaseMessage implements Serializable {

    protected String traceId = TraceIdUtils.getTraceId();

}
