package com.lanf.api.storage.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
public class AddSupplierDTO implements Serializable {


    @NotBlank(message = "供应商名称不能为空")
    /** 供应商名称 */
    private String name;

    @NotBlank(message = "省不能为空")
    /** 省 */
    private String province;

    @NotBlank(message = "市不能为空")
    /** 市 */
    private String city;

    @NotBlank(message = "区不能为空")
    /** 区 */
    private String area;

    @NotBlank(message = "详细地址不能为空")
    /** 详细地址 */
    private String detailAddress;

    @NotBlank(message = "手机不能为空")
    /** 手机 */
    private String phone;

    @NotBlank(message = "职位不能为空")
    /** 职位 */
    private String position;

    @NotBlank(message = "邮箱不能为空")
    /** 邮箱 */
    private String email;

    @NotBlank(message = "税号不能为空")
    /** 税号 */
    private String dutyParagraph;

    @NotBlank(message = "开户行不能为空")
    /** 开户行 */
    private String bankName;

    @NotBlank(message = "发票title不能为空")
    /** 发票title */
    private String invoiceTitle;

    @NotBlank(message = "银行账号不能为空")
    /** 银行账号 */
    private String bankAccount;

    @NotBlank(message = "支付宝账号不能为空")
    /** 支付宝账号 */
    private String alipayAccount;

}
