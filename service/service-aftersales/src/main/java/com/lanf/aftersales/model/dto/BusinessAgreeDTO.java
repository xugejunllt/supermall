package com.lanf.aftersales.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class BusinessAgreeDTO implements Serializable {


    private Long id;
    //0:同意 1:拒绝
    private  Long agree;

}
