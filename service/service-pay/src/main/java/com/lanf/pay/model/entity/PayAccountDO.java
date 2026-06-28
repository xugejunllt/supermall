package com.lanf.pay.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.api.pay.model.enums.PayChannelEnum;
import com.lanf.mybatis.base.BaseEntity;
import lombok.Data;

/**
 * <p>
 * 
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-21
 */
@Data
@TableName("pay_account")
public class PayAccountDO extends BaseEntity {

private static final long serialVersionUID=1L;


    private PayChannelEnum accountType;

    private String account;

    private String accountName;

    private Long tenantId;


}
