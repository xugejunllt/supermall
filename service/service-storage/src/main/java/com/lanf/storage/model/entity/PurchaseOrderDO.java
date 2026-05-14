package com.lanf.storage.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
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



    /** 单据编码 */
    private String code;

    /** 供应商id */
    private Long supplierId;



    /** 期望到货日期 */
    private Date expectTime;

    /** 采购运费 */
    private BigDecimal freight;

    /** 其他费用 */
    private BigDecimal otherFreight;


    /** 结算方式 0:现金,1:微信,2:支付宝,3:建设银行 4.工商银行 */
    private Integer balanceType;

    /** 附件 */
    private String annexUrl;

    /** 备注 */
    private String remarks;

    /** 总计金额 */
    private BigDecimal totalMoney;

    /** 状态:0:审核中 1.审核通过 2:审核不通过 */
    private Integer status;

    /** 审核人 */
    private String reviewer;

    /** 审核时间 */
    private Date reviewTime;

    private Long  tenantId;

}
