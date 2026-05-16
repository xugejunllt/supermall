package com.lanf.storage.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import lombok.Data;

/**
 * <p>
 * 仓库
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-05-30
 */
@Data
@TableName("warehouse")
public class WarehouseDO extends BaseEntity {

private static final long serialVersionUID=1L;

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

    private String areaCode;

    /** 详细地址 */
    private String detailAddress;

    /** 联系人 */
    private String contacts;

    /** 手机 */
    private String phone;

    /** 邮箱 */
    private String email;

    private Long  tenantId;
}
