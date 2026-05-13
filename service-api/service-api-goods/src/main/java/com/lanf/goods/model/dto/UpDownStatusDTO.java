package com.lanf.goods.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class UpDownStatusDTO implements Serializable {

    private Long id;

    private Integer upDownStatus;
}
