package com.lanf.api.storage.model.vo;

import com.lanf.api.storage.model.bo.PurchaseOrderItem;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;


@Data
public class PurchaseOrderDetailVO implements Serializable {

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

    /** 预付金额 */
    private BigDecimal advanceMoney;

    /** 结算方式 0:现金,1:微信,2:支付宝,3:建设银行 4.工商银行 */
    private Integer balanceType;

    /** 附件 */
    private String annexUrl;

    /** 备注 */
    private String remarks;

    /** 总计金额 */
    private BigDecimal totalMoney;

    /** 状态:0:审核中 1.审核通过 2:审核不通过,3.部分入库 4.已完成 */
    private Integer status;

    /** 审核人 */
    private String reviewer;

    /** 审核时间 */
    private Date reviewTime;

    /** 供应商名称 */
    private String supplierName;
    //商品总数量
    private Integer totalQuantity;

    private List<PurchaseOrderItem> purchaseOrderItemList;


}
