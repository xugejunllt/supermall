package com.lanf.finance.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 平台结算流水
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-20
 */
@Data
@TableName("settlement_flow")
public class SettlementFlowDO extends BaseEntity {

private static final long serialVersionUID=1L;



    @ApiModelProperty(value = "平台清分单id")
    private Long liquidationFlowId;
    /**
     * 商户id
     */
    private Long merchantId;



}
