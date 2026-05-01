package com.lanf.pay.service.reconciliation.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.client.pay.model.enums.PayChannelEnum;
import com.lanf.constant.exception.BizException;
import com.lanf.pay.excel.AalPayFundBillDetailExcel;
import com.lanf.pay.excel.AalPayFundBillDetailReadListener;
import com.lanf.pay.mapper.SignCustomerFundBillDetailMapper;
import com.lanf.pay.model.entity.SignCustomerFundBillDetailDO;
import com.lanf.pay.service.reconciliation.SignCustomerIFundBillDetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;

/**
 * <p>
 * 资金账单明细表 服务实现类
 * </p>
 *
 * @author jarven
 * @since 2026-04-29
 */
@Slf4j
@Service
public class SignCustomerFundBillDetailServiceImpl extends ServiceImpl<SignCustomerFundBillDetailMapper, SignCustomerFundBillDetailDO> implements SignCustomerIFundBillDetailService {



    @Override
    public void importFromExcel(InputStream inputStream, String batchId, PayChannelEnum payChannel, File excelFile) {

        log.info("开始导入对账单 Excel: batchId={}, payChannel={}", batchId, payChannel);
        AalPayFundBillDetailReadListener listener = new AalPayFundBillDetailReadListener
                (batchId, payChannel.getCode().toString());
        Class<?> head = null;
        switch (payChannel){
            case ALI_PAY:
                head = AalPayFundBillDetailExcel.class;
                break;
            case WECHAT_PAY:
                log.error("不支持的支付渠道: {}", payChannel);
                break;
            default:
                log.error("不支持的支付渠道: {}", payChannel);
                throw new BizException("不支持的支付渠道");
        }

        try {

            EasyExcel.read(inputStream, head, listener)
                    .sheet()
                    .doRead();

            log.info("对账单 Excel 导入完成: batchId={}, payChannel={}", batchId, payChannel);

        } catch (Exception e) {
            log.error("对账单 Excel 导入失败: batchId={}, payChannel={}", batchId, payChannel, e);
            throw new BizException("对账单导入失败");
        } finally {
            // 无论成功或失败，都删除临时文件
            if (excelFile != null && excelFile.exists()) {
                boolean deleted = excelFile.delete();
                if (deleted) {
                    log.info("临时文件已清理: {}", excelFile.getAbsolutePath());
                } else {
                    log.warn("临时文件清理失败: {}", excelFile.getAbsolutePath());
                }
            }
        }
    }
}
