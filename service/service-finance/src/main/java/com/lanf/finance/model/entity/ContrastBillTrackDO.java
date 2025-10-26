package com.lanf.finance.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-09-01
 */
@Data
@TableName("contrast_bill_track")
public class ContrastBillTrackDO extends BaseEntity {

private static final long serialVersionUID=1L;



    @ApiModelProperty(value = "对比账单id")
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

    @ApiModelProperty(value = "对比结果状态,0:成功,1:失败")
    private Integer resultStatus;

    @ApiModelProperty(value = "对比内容")
    private String content;




}
