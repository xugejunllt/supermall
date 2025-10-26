package com.lanf.logistics.model.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class LogisticsVO implements Serializable {

    //收货人
    private String contacts;
    //收货人地址
    private String address;
    //快递公司
    private String expressCompany;
    //快递单号
    private String number;

    private List<LogisticsTrackStatusVO> logisticsTrackStatusVOList;

}
