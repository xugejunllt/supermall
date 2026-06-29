package com.lanf.api.user.model.vo;

import com.lanf.api.user.model.enums.UserStatusEnum;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class UserPageVO implements Serializable {

    /**
     * 用户id
     */
    private Long id;

    /**
     * 用户名
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
     * 状态：0.待审核 1.正常 2.禁用
     */
    private UserStatusEnum status;

    /**
     * 头像
     */
    private String headImageUrl;

    /**
     * 注册渠道 0: web 1:android 2:ios
     */
    private Integer registerChannel;

    /**
     * 注册时间
     */
    private Date registerTime;

}
