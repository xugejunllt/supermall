package com.lanf.sms.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class TemplateAddDTO implements Serializable {

    private String code;

    private String name;

    private Integer type;

    private String scene;

    private String content;

}
