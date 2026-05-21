package com.lanf.finance.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

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



    @ApiModelProperty(value = "商家id")
    private Long tenantId;


    @ApiModelProperty(value = "账户类型 0:支付宝")
    private Integer accountType;

    @ApiModelProperty(value = "账户")
    private String account;

    //初期余额
    private BigDecimal startRemainMoney;
    //当前余额
    private BigDecimal remainMoney;



}
