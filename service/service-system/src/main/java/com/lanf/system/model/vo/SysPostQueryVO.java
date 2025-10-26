package com.lanf.system.model.vo;

import lombok.Data;

import java.util.Date;

/**
* @author tanlingfei
* @version 1.0
* @description 岗位信息表 vo类
* @date 2023-04-30 12:37:35
*/
@Data
public class SysPostQueryVO {
       private String postCode;
       private String name;
       private String description;
       private String status;
       private String statusName;
       private Date createTimeBegin;
       private Date createTimeEnd;
       private Date updateTimeBegin;
       private Date updateTimeEnd;
}

