package com.lanf.storage.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class PurchaseOrderAddDTO implements Serializable {



    @NotNull(message = "供应商id不能为空")
    @ApiModelProperty(value = "供应商id")
    private Long supplierId;

    @Future(message = "期望到货日期不能小于当前时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = " 期望到货日期")
    private Date expectTime;

    @Max(value = 1000000,message = "采购运费超过最大值")
    @Min(value = 0,message = "采购运费小于最小值")
    @ApiModelProperty(value = "采购运费")
    private BigDecimal freight;

    @Max(value = 1000000,message = "其他费用超过最大值")
    @Min(value = 0,message = "其他费用小于最小值")
    @ApiModelProperty(value = "其他费用")
    private BigDecimal otherFreight;

    @NotNull(message = "结算方式不能为空")
    @ApiModelProperty(value = "结算方式 0:支付宝,1:微信")
    private Integer balanceType;

    @ApiModelProperty(value = "附件")
    private String annexUrl;

    @ApiModelProperty(value = "备注")
    private String remarks;

    @NotEmpty
    private List<PurchaseOrderItemAddDTO> purchaseOrderItemAdd;
}
