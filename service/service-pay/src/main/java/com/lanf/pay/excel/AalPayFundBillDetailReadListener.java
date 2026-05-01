package com.lanf.pay.excel;


import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.lanf.client.pay.model.enums.PayChannelEnum;
import com.lanf.common.utils.BeanUtil;
import com.lanf.pay.mapper.SignCustomerFundBillDetailMapper;
import com.lanf.pay.model.entity.ChannelBillDownloadProgressDO;
import com.lanf.pay.model.entity.SignCustomerFundBillDetailDO;
import com.lanf.pay.model.enums.BillDownloadStatusEnum;
import com.lanf.pay.model.enums.ReconciliationBusinessTypeEnum;
import com.lanf.pay.service.reconciliation.IChannelBillDownloadProgressService;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 对账单 Excel 读取监听器
 * 实现分批插入数据库
 */
@Slf4j
public class AalPayFundBillDetailReadListener implements ReadListener<AalPayFundBillDetailExcel> {

    /**
     * 批量插入阈值
     */
    private static final int BATCH_COUNT = 1000;

    /**
     * 缓存的数据列表
     */
    private List<SignCustomerFundBillDetailDO> cachedDataList = new ArrayList<>(BATCH_COUNT);

    private final SignCustomerFundBillDetailMapper fundBillDetailMapper;
    private final String batchId;
    private final String payChannel;
    private final AtomicInteger currentParseCount ;
    private final ExcelParseProgressManager excelParseProgressManager;
    private final IChannelBillDownloadProgressService channelBillDownloadProgressService;
    /**
     * 保存是否出现过异常
     */
    private Boolean hasExc;

    public AalPayFundBillDetailReadListener(String batchId, String payChannel) {
        this.fundBillDetailMapper = BeanUtil.getBean(SignCustomerFundBillDetailMapper.class);
        this.batchId = batchId;
        this.payChannel = payChannel;
        this.currentParseCount = new AtomicInteger(0);
        this.excelParseProgressManager = BeanUtil.getBean(ExcelParseProgressManager.class);
        this.channelBillDownloadProgressService = BeanUtil.getBean(IChannelBillDownloadProgressService.class);
        this.hasExc = false;

    }

    @Override
    public void invoke(AalPayFundBillDetailExcel data, AnalysisContext context) {

        //数量+1
        currentParseCount.getAndIncrement();

        // 转换 Excel 数据为 DO 对象
        SignCustomerFundBillDetailDO detailDO = convertToDO(data);
        
        // 设置批次信息

        detailDO.setPayChannel( PayChannelEnum.getByCode(Integer.parseInt(payChannel)));
        detailDO.setPayFinishDate(batchId);
        
        cachedDataList.add(detailDO);

        // 达到批量阈值，执行插入
        if (cachedDataList.size() >= BATCH_COUNT) {
            saveBatch();
            cachedDataList.clear();
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        // 处理剩余数据
        if (!cachedDataList.isEmpty()) {
            saveBatch();
            cachedDataList.clear();
        }
        log.info("Excel 解析完成");
        /**
         * 更新任务状态为已完成
         */
        if (hasExc){
            log.warn("解析保存出现异常");
            return;
        }
        boolean update = channelBillDownloadProgressService.lambdaUpdate()
                .eq(ChannelBillDownloadProgressDO::getBatchId, batchId)
                .eq(ChannelBillDownloadProgressDO::getPayChannel, payChannel)
                .set(ChannelBillDownloadProgressDO::getStatus, BillDownloadStatusEnum.COMPLETED)
                .update();
        if (!update) {
            log.warn("更新渠道对账单下载进度失败");
        }
    }

    /**
     * 批量保存数据
     */
    private void saveBatch() {

        if (cachedDataList.isEmpty()) {
            return;
        }

        int rows = currentParseCount.get();

        if (excelParseProgressManager.isSaveBath(rows, payChannel, batchId)) {
            log.info("已存储DB中");
            return;
        }
        try {

            fundBillDetailMapper.batchInsertIgnore(cachedDataList);
            excelParseProgressManager.addRows(rows, payChannel, batchId);

        } catch (Exception e) {
            log.warn("批量插入对账单明细失败", e);
            hasExc = true;

        }
    }

    /**
     * 转换 Excel 对象为 DO 对象
     */
    private SignCustomerFundBillDetailDO convertToDO(AalPayFundBillDetailExcel excel) {
        SignCustomerFundBillDetailDO detailDO = new SignCustomerFundBillDetailDO();
        
        detailDO.setMerchantOrderNo(excel.getMerchantOrderNo());
        detailDO.setFinancialSerialNo(excel.getFinancialSerialNo());
        detailDO.setBusinessSerialNo(excel.getBusinessSerialNo());
        detailDO.setCounterpartyAccount(excel.getCounterpartyAccount());
        detailDO.setTransactionChannel(excel.getTransactionChannel());
        ReconciliationBusinessTypeEnum byCode = ReconciliationBusinessTypeEnum.getByCode(Integer.parseInt(excel.getBusinessType()));
        detailDO.setBusinessType(byCode);
        detailDO.setRemark(excel.getRemark());
        
        // 转换金额
        detailDO.setIncomeAmount(parseBigDecimal(excel.getIncomeAmountStr()));
        detailDO.setExpenseAmount(parseBigDecimal(excel.getExpenseAmountStr()));
        detailDO.setAccountBalance(parseBigDecimal(excel.getAccountBalanceStr()));
        
        // 转换时间
        detailDO.setOccurTime(parseLocalDateTime(excel.getOccurTimeStr()));
        
        return detailDO;
    }

    /**
     * 解析 BigDecimal
     */
    private BigDecimal parseBigDecimal(String str) {
        if (str == null || str.trim().isEmpty() || "0".equals(str)) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(str);
        } catch (Exception e) {
            log.warn("金额解析失败: {}", str);
            return BigDecimal.ZERO;
        }
    }

    /**
     * 解析 LocalDateTime
     */
    private LocalDateTime parseLocalDateTime(String str) {
        if (str == null || str.trim().isEmpty()) {
            return null;
        }
        try {
            // 根据实际 Excel 中的时间格式调整
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return LocalDateTime.parse(str, formatter);
        } catch (Exception e) {
            log.warn("时间解析失败: {}", str);
            return null;
        }
    }
}
