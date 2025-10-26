package com.lanf.system.model.vo;

import lombok.Data;

@Data
public class SysOperLogQueryVO {

    private String title;
    private String operName;

    private String createTimeBegin;
    private String createTimeEnd;

}

