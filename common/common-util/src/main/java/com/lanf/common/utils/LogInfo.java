package com.lanf.common.utils;

import lombok.Data;

import java.io.Serializable;

@Data
public class LogInfo implements Serializable {

    private final String key;

    private final String value;

    public LogInfo(String key, String value) {
        this.key = key;
        this.value = value;
    }
}
