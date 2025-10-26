package com.lanf.finance.model.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class ContrastBillTrackVO implements Serializable {

   //对比账单id
    private Long contrastBillId;

    /**
     * 轨迹状态
     * 0:对比订单状态
     * 1.对比订单金额、支付金额是否正确
     * 2.对比清分结算单是否正确
     * 3.资金流水是否正确
     *
     */
    private Integer trackStatus;

    private String trackStatusName;

    @ApiModelProperty(value = "对比结果状态,0:成功,1:失败")
    private Integer resultStatus;

    private String resultStatusName;

    @ApiModelProperty(value = "对比内容")
    private String content;
}
