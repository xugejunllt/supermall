package com.lanf.user.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
public class AddAddressDTO implements Serializable {


    private Long userId;

    /**
     * 联系人
     */
    @NotBlank(message = "联系人不能为空")
    private String consignee;

    /**
     * 手机号
     */
    @NotBlank(message = "手机号不能为空")
    private String phone;

    /**
     * 地区
     */
    @NotBlank(message = "区不能为空")
    private String area;

    /**
     * 地址
     */
    @NotBlank(message = "地址不能为空")
    private String address;

}
