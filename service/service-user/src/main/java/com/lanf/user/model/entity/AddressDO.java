package com.lanf.user.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import lombok.Data;

/**
 * <p>
 * 
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-10
 */
@Data
@TableName("address")
public class AddressDO extends BaseEntity {

private static final long serialVersionUID=1L;



    /**
     * 用户id
     */
    private Long userId;

    /**
     * 联系人
     */
    private String consignee;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 地区
     */
    private String area;
    
    /**
     * 详细地址
     */
    private String address;

    /**
     * 是否默认 0默认 1.不是
     */
    private Integer defaultAddress;


}
