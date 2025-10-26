package com.lanf.system.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class BaseAreaVO implements Serializable {

    private Long id;
    private Long parentId;

    private String cityName;

    private Integer type;

    private List<BaseAreaVO> childList;

}
