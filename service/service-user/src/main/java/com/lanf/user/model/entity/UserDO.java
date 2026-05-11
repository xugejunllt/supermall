package com.lanf.user.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.user.model.enums.UserStatusEnum;
import lombok.Data;

import java.util.Date;

/**
 * <p>
 * User对象
 * </p>
 *
 * @author Jarven
 * @since 2025-10-27
 */
@Data
@TableName("user")
public class UserDO extends BaseEntity {

private static final long serialVersionUID=1L;


    /**
     * 用户名
     */
    private String account;

    /**
     * 密码
     */
    private String password;

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
     * 设备类型
     */
    private String deviceType;

    /**
     * 设备ID
     */
    private String deviceId;

    /**
     * 设备型号
     */
    private String deviceModel;

    /**
     * 应用版本
     */
    private String appVersion;

    /**
     * 注册时间
     */
    private Date registerTime;



}
