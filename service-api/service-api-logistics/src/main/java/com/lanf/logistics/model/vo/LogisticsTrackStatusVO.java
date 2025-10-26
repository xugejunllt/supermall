package com.lanf.logistics.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
public class LogisticsTrackStatusVO implements Serializable {


    //物流状态名称
    private String statusName;

    private Date maxFinishTime;

    private List<LogisticsTrackVO> logisticsTrackVOList;


}
