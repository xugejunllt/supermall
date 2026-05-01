package com.lanf.pay.service.reconciliation.excel;


import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.lanf.common.utils.BeanUtil;
import com.lanf.pay.model.entity.ChannelBillDownloadProgressDO;
import com.lanf.pay.model.enums.BillDownloadStatusEnum;
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
public abstract class FundBillDetailReadListener<T,S> implements ReadListener<T> {

    /**
     * 批量插入阈值
     */
    private static final int BATCH_COUNT = 1000;

    /**
     * 缓存的数据列表
     */
    private List<S> cachedDataList = new ArrayList<>(BATCH_COUNT);

    protected final String batchId;
    protected final String payChannel;

    private final AtomicInteger currentParseCount ;
    private final ExcelParseProgressManager excelParseProgressManager;
    private final IChannelBillDownloadProgressService channelBillDownloadProgressService;

    /**
     * 保存是否出现过异常
     */
    private Boolean hasExc;



    public FundBillDetailReadListener(String batchId, String payChannel) {
        this.batchId = batchId;
        this.payChannel = payChannel;
        this.currentParseCount = new AtomicInteger(0);
        this.excelParseProgressManager = BeanUtil.getBean(ExcelParseProgressManager.class);
        this.channelBillDownloadProgressService = BeanUtil.getBean(IChannelBillDownloadProgressService.class);
        this.hasExc = false;

    }

    @Override
    public void invoke(T data, AnalysisContext context) {

        //数量+1
        currentParseCount.getAndIncrement();

        // 转换 Excel 数据为 DO 对象
        S detailDO = convertToDO(data);

        /////
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
            batchInsertIgnore(cachedDataList);
            excelParseProgressManager.addRows(rows, payChannel, batchId);

        } catch (Exception e) {
            log.warn("批量插入对账单明细失败", e);
            hasExc = true;

        }
    }
    abstract S convertToDO(T excel);

    abstract void batchInsertIgnore(List<S> list);


    /**
     * 解析 BigDecimal
     */
     BigDecimal parseBigDecimal(String str) {
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
     LocalDateTime parseLocalDateTime(String str) {
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
