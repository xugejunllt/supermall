package com.lanf.user.model.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserLoginVO implements Serializable {

    private String token;

    private String refreshToken;
    private Long userId;
    private String userName;
    private String headImageUrl;
    /**
     * 兼容app页面
     */
    private String sId ="112";
    private  String tel="112";
    private  String password="112";
    private  String salt="112";
    private  int gold;
    private  int coupon;
    private  int redPacket;
    private  int quota;
    private  int collect;
    private  int footmark;
    private  int follow;

}
