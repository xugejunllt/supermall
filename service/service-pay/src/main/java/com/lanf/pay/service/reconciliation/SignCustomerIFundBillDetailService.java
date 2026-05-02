package com.lanf.pay.service.reconciliation;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.client.pay.model.enums.PayChannelEnum;
import com.lanf.pay.model.entity.SignCustomerFundBillDetailDO;

import java.io.File;
import java.io.InputStream;

/**
 * <p>
 * 资金账单明细表 服务类
 * </p>
 *
 * @author jarven
 * @since 2026-04-29
 */
public interface SignCustomerIFundBillDetailService extends IService<SignCustomerFundBillDetailDO> {

    /**
     * 从 Excel 文件导入对账单明细
     *
     * @param inputStream Excel 文件输入流
     * @param batchId 批次ID（账单日期）
     * @param payChannel 支付渠道
     */
    void importFromExcel(InputStream inputStream, String batchId,
                         PayChannelEnum payChannel, File excelFile,String billType);
}
