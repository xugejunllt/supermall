package com.lanf.pay.model.bo;


import lombok.Data;

import java.io.Serializable;

/**
 * 支付宝对账单下载URL查询结果
 */
@Data
public class BillDownloadUrlResultBO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 对账单下载地址
     */
    private String billDownloadUrl;

    /**
     * 账单类型
     * trade: 基于支付宝交易产生的资金流水明细账
     * signcustomer: 基于商户号生成的针对所有资金交易的签名档明细账
     */
    private String billType;

    /**
     * 账单日期 格式 yyyy-MM-dd
     */
    private String billDate;

    /**
     * 原始账单url（需要登录支付宝账户后下载）
     */
    private String originalBillUrl;


}
