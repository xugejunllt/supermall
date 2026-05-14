package com.lanf.storage.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
public class WarehouseAddDTO implements Serializable {

    @NotBlank(message = "仓库名称不能为空")
    /** 仓库名称 */
    private String name;

    //仓库组编码
    @NotBlank(message = "仓库组编码不能为空")
    private String groupCode;

    @NotBlank(message = "省不能为空")
    /** 省 */
    private String province;

    @NotBlank(message = "市能为空")
    /** 市 */
    private String city;

    @NotBlank(message = "区不能为空")
    /** 区 */
    private String area;

    @NotBlank(message = "详细地址不能为空")
    /** 详细地址 */
    private String detailAddress;

    @NotBlank(message = "联系人不能为空")
    /** 联系人 */
    private String contacts;

    @NotBlank(message = "手机不能为空")
    /** 手机 */
    private String phone;

    @NotBlank(message = "邮箱不能为空")
    /** 邮箱 */
    private String email;
}
