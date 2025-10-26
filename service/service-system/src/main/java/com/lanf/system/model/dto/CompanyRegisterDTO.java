package com.lanf.system.model.dto;


import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * 公司注册dto
 */
@Data
public class CompanyRegisterDTO {

    /**
     * 公司名称
     */
    @NotBlank(message = "公司名称不能为空")
    private String company;

    /**
     * 姓名
     */
    @NotBlank(message = "姓名不能为空")
    private String userName;

    /**
     * 手机
     */
    @Pattern(regexp = "/^1\\d{10}$|^(0\\d{2,3}-?|\\(0\\d{2,3}\\))?[1-9]\\d{4,7}(-\\d{1,8})?$/\n",message = "手机号格式错误")
    private String phoneNumber;

    /**
     * 店铺名称
     */
    @NotBlank(message = "铺名称不能为空")
    private String shopName;
    /**
     * 手机验证码
     */
    @NotBlank(message = "手机验证码不能为空")
    private String code;
    /**
     * admin账号密码
     */
    @NotBlank(message = "admin密码不能为空")
    private String adminPassword;

    private String headUrl;

}
