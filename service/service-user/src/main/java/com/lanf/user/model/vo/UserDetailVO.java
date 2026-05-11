package com.lanf.user.model.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserDetailVO implements Serializable {

    /**
     * 账号
     */
    private String account;


    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号码
     */
    private String phoneNumber;

    /**
     * 昵称
     */
    private String nickName;

    /**
     * 头像
     */
    private String headImageUrl;

    /**
     * 等级信息
     */
    /**
     * 等级
     */
    private Integer level;

    /**
     * 等级名称，如VIP1
     */
    private String levelName;

    /**
     * 等级图标
     */
    private String levelIcon;



}
