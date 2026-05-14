package com.lanf.api.storage.model.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class WarehousePageVO implements Serializable {

    /** 仓库编码 */
    private String code;

    //仓库组编码
    private String groupCode;

    /** 仓库名称 */
    private String name;

    /** 状态 0停用 1.正常 */
    private Integer status;

    /** 省 */
    private String province;

    /** 市 */
    private String city;

    /** 区 */
    private String area;

    /** 详细地址 */
    private String detailAddress;

    /** 联系人 */
    private String contacts;

    /** 手机 */
    private String phone;

    /** 邮箱 */
    private String email;




}
