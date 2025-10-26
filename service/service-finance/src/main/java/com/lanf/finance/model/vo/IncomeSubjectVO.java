package com.lanf.finance.model.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class IncomeSubjectVO implements Serializable {

    private  Integer code;
    private  String name;
    private  Integer income;

}
