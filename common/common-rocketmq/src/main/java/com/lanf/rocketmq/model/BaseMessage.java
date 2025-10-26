package com.lanf.rocketmq.model;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


@Data
public class BaseMessage implements Serializable {

    //延迟时间，单位分钟
    private int delayTime;


}
