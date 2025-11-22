package com.lanf.storage.model.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 * 采购单
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-05-30
 */
@Data
@TableName("purchase_order")
public class PurchaseOrderDO extends BaseEntity {

private static final long serialVersionUID=1L;



    @ApiModelProperty(value = "单据编码")
    private String code;

    @ApiModelProperty(value = "供应商id")
    private Long supplierId;



    @ApiModelProperty(value = " 期望到货日期")
    private Date expectTime;

    @ApiModelProperty(value = "采购运费")
    private BigDecimal freight;

    @ApiModelProperty(value = "其他费用")
    private BigDecimal otherFreight;


    @ApiModelProperty(value = "结算方式 0:现金,1:微信,2:支付宝,3:建设银行 4.工商银行")
    private Integer balanceType;

    @ApiModelProperty(value = "附件")
    private String annexUrl;

    @ApiModelProperty(value = "备注")
    private String remarks;

    @ApiModelProperty(value = "总计金额")
    private BigDecimal totalMoney;

    @ApiModelProperty(value = "状态:0:审核中 1.审核通过 2:审核不通过,3.部分入库 4.已完成")
    private Integer status;

    @ApiModelProperty(value = "审核人")
    private String reviewer;

    @ApiModelProperty(value = "审核时间")
    private Date reviewTime;
    @TableField( fill = FieldFill.INSERT)
    private Long  tenantId;

}
