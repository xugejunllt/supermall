package com.lanf.dynamicsrrefresh.service.model.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class ConfigVO implements Serializable {

    private String value;

    public ConfigVO() {
    }

    public ConfigVO(String value) {
        this.value = value;
    }
}
