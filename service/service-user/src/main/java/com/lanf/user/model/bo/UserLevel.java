package com.lanf.user.model.bo;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserLevel implements Serializable {

    /**
     * 等级
     */
    private Integer level;

    /**
     * 等级名称，如VIP1
     */
    private String name;

    /**
     * 等级图标
     */
    private String icon;


}
