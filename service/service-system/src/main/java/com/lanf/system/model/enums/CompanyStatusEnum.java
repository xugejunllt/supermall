package com.lanf.system.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CompanyStatusEnum {

    //审核状态  0:审核中,1: 已审核,2: 审核失败
    IN(0,"审核中"),
    //already
    ALREADY(1,"已审核"),
    FAIL(2,"审核不通过");

    private final int code;
    private final  String desc;

    public static  boolean include(int code){

        for (CompanyStatusEnum companyStatus: CompanyStatusEnum.values()){

            if (companyStatus.code == code){
                return  true;
            }

        }
        return  false;
    }

}
