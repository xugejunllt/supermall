package com.lanf.pay.excel;


import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.lanf.common.utils.BeanUtil;
import com.lanf.common.utils.JsonUtils;
import com.lanf.pay.mapper.FundBillDetailMapper;
import com.lanf.pay.model.entity.FundBillDetailDO;
import com.lanf.pay.mq.constant.PayMqTopicName;
import com.lanf.rocketmq.util.RocketMqClient;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

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
    private List<FundBillDetailDO> cachedDataList = new ArrayList<>(BATCH_COUNT);

    private final FundBillDetailMapper fundBillDetailMapper;
    private final String batchId;
    private final String payChannel;

    public AalPayFundBillDetailReadListener(FundBillDetailMapper fundBillDetailMapper,
                                            String batchId,
                                            String payChannel) {
        this.fundBillDetailMapper = fundBillDetailMapper;
        this.batchId = batchId;
        this.payChannel = payChannel;
    }

    @Override
    public void invoke(AalPayFundBillDetailExcel data, AnalysisContext context) {
        // 转换 Excel 数据为 DO 对象
        FundBillDetailDO detailDO = convertToDO(data);
        
        // 设置批次信息
        detailDO.setPayChannel(payChannel);
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
    }

    /**
     * 批量保存数据
     */
    private void saveBatch() {
        if (cachedDataList.isEmpty()) {
            return;
        }

        try {

            fundBillDetailMapper.batchInsertIgnore(cachedDataList);

        } catch (Exception e) {
            log.warn("批量插入对账单明细失败", e);
            /**
             * 解析账单任务 并发批量插入 DB压力大 可能超时
             * 发送mq补偿
             */
            try {
                RocketMqClient rocketMqClient = BeanUtil.getBean(RocketMqClient.class);
                rocketMqClient.sendMessage(PayMqTopicName.FUND_BILL_DETAIL_COMPENSATION_TOPIC,
                        JsonUtils.toJsonString(cachedDataList));
            } catch (Exception ex) {
                log.error("发送对账单补偿失败", ex);
            }
        }
    }

    /**
     * 转换 Excel 对象为 DO 对象
     */
    private FundBillDetailDO convertToDO(AalPayFundBillDetailExcel excel) {
        FundBillDetailDO detailDO = new FundBillDetailDO();
        
        detailDO.setMerchantOrderNo(excel.getMerchantOrderNo());
        detailDO.setFinancialSerialNo(excel.getFinancialSerialNo());
        detailDO.setBusinessSerialNo(excel.getBusinessSerialNo());
        detailDO.setCounterpartyAccount(excel.getCounterpartyAccount());
        detailDO.setTransactionChannel(excel.getTransactionChannel());
        detailDO.setBusinessType(excel.getBusinessType());
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
