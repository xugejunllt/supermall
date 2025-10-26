package com.lanf.system.model.vo;

import lombok.Data;

import java.util.Date;

/**
 * @author tanlingfei
 * @version 1.0
 * @description 国际化语言 vo类
 * @date 2023-10-31 13:47:32
 */
@Data
public class SysI18nQueryVO {
    private String name;
    private String val;
    private String type;
    private String typeName;
    private Date createTimeBegin;
    private Date createTimeEnd;
    private Date updateTimeBegin;
    private Date updateTimeEnd;
}

