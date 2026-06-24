package com.lanf.dynamicsrrefresh.core.model.enums;

import lombok.Getter;

@Getter
public enum BizCodeEnum {


    CLOSE_SERVICE(0,"closeService","CLOSE_SERVICE","关闭服务"),

    CLOSE_INTERFACE(1,"closeInterface","CLOSE_INTERFACE","关闭接口"),
    //blacklist
    IP_BLACK_LIST(2,"ipBlackList","IP_BLACK_LIST","ip黑名单");

    private final Integer code;

    private final String dateId;

    private final String group;

    private final String desc;

    BizCodeEnum(Integer code, String dateId, String group, String desc) {
        this.code = code;
        this.dateId = dateId;
        this.group = group;
        this.desc = desc;
    }

   public static  BizCodeEnum getByCode(Integer code){

         for (BizCodeEnum codeEnum : BizCodeEnum.values()){
             if (codeEnum.code.equals(code)){

                 return codeEnum;
             }
         }

         throw new RuntimeException("不支持的业务code");
   }


}
