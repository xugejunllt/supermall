package com.lanf.system.model.vo;

import lombok.Data;

import java.util.Date;

/**
 * @author tanlingfei
 * @version 1.0
 * @description 字典选项 vo类
 * @date 2020-04-13 16:12:32
 */
@Data
public class SysDicItemQueryVO {
    private String dicCode;
    private String code;
    private String name;
    private Date createTimeBegin;
    private Date createTimeEnd;
    private Date updateTimeBegin;
    private Date updateTimeEnd;
    private String dicCodes;

}

