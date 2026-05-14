package com.lanf.storage.model.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class SupplierPageVO implements Serializable {


    /** 供应商编码 */
    private String code;

    /** 供应商名称 */
    private String name;

    /** 省 */
    private String province;

    /** 市 */
    private String city;

    /** 区 */
    private String area;

    /** 详细地址 */
    private String detailAddress;

    /** 手机 */
    private String phone;

    /** 职位 */
    private String position;

    /** 邮箱 */
    private String email;

    /** 税号 */
    private String dutyParagraph;

    /** 开户行 */
    private String bankName;

    /** 发票title */
    private String invoiceTitle;

    /** 银行账号 */
    private String bankAccount;

    /** 支付宝账号 */
    private String alipayAccount;

}
