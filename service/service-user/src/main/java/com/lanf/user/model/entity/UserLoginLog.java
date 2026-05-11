package com.lanf.user.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import lombok.Data;

/**
 * <p>
 * 
 * </p>
 *
 * @author jarven
 * @since 2025-11-15
 */
@Data
@TableName("user_login_log")
public class UserLoginLog extends BaseEntity {

private static final long serialVersionUID=1L;


    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 手机号码/账号
     */
    private String account;





    /**
     * 客户端IP地址
     */
    private String ipAddress;

    /**
     * 登入类型 0 sms
     */
    private Integer loginType;

    /**
     * 注册渠道 0: web 1:android 2:ios
     */
    private Integer loginChannel;

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




}
