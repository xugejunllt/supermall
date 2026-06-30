package com.lanf.pay.service.reconciliation.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.common.utils.BeanCopyUtils;
import com.lanf.common.utils.JsonUtils;
import com.lanf.pay.mapper.ReconciliationJobLogMapper;
import com.lanf.pay.model.bo.SendMessageAndUpdateResult;
import com.lanf.pay.model.entity.PayOrderFlowDO;
import com.lanf.pay.model.entity.ReconciliationDiffMarkerDO;
import com.lanf.pay.model.entity.ReconciliationJobLogDO;
import com.lanf.pay.model.entity.SignCustomerFundBillDetailDO;
import com.lanf.api.pay.model.enums.ReconciliationBusinessTypeEnum;
import com.lanf.api.pay.model.enums.ReconciliationDiffTypeEnum;
import com.lanf.api.pay.model.enums.ReconciliationJobStatusEnum;
import com.lanf.api.pay.model.enums.ReconciliationJobTypeEnum;
import com.lanf.api.pay.model.query.ReconciliationJobLogSumQuery;
import com.lanf.api.pay.model.vo.ReconciliationJobLogSumVO;
import com.lanf.pay.mq.constant.PayMqTopicName;
import com.lanf.pay.mq.message.ReconciliationStartMessage;
import com.lanf.pay.service.pay.IPayOrderFlowService;
import com.lanf.pay.service.pay.IRefundOrderFlowService;
import com.lanf.pay.service.pay.ITransferOrderFlowService;
import com.lanf.pay.service.reconciliation.IReconciliationDiffMarkerService;
import com.lanf.pay.service.reconciliation.IReconciliationJobLogService;
import com.lanf.pay.service.reconciliation.SignCustomerIFundBillDetailService;
import com.lanf.rocketmq.util.RocketMqClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p>
 * 对账任务执行记录表 服务实现类
 * </p>
 *
 * @author jarven
 * @since 2026-04-30
 */
@Slf4j
@Service
public class ReconciliationJobLogServiceImpl extends ServiceImpl<ReconciliationJobLogMapper, ReconciliationJobLogDO> implements IReconciliationJobLogService {


    @Autowired
    private RocketMqClient rocketMqClient;
    @Lazy
    @Autowired
    private IReconciliationJobLogService reconciliationJobLogService;

    @Autowired
    private IPayOrderFlowService payOrderFlowService;
    @Autowired
    private IRefundOrderFlowService refundOrderFlowService;
    @Autowired
    private ITransferOrderFlowService transferOrderFlowService;
    @Autowired
    private SignCustomerIFundBillDetailService signCustomerIFundBillDetailService ;
    @Autowired
    private IReconciliationDiffMarkerService reconciliationDiffMarkerService;

    /**
     *
     * mq发送 与更新 在同个事务里 异常进行回滚
     */
    @Transactional
    @Override
    public SendMessageAndUpdateResult sendMessageAndUpdate(ReconciliationStartMessage message, ReconciliationJobTypeEnum jobType, String bathId, long currentPage, long pages) throws Exception {
        rocketMqClient.sendMessage(PayMqTopicName.RECONCILIATION_START_TOPIC, JsonUtils.
                toJsonString(message));

        if (currentPage >= pages) {
            /**
             * 更新jbs状态为已完成
             */
            reconciliationJobLogService.lambdaUpdate()
                    .eq(ReconciliationJobLogDO::getBatchId, bathId)
                    .eq(ReconciliationJobLogDO::getJobType, jobType)
                    .set(ReconciliationJobLogDO::getStatus, ReconciliationJobStatusEnum.SCAN_COMPLETED)
                    .update();

            SendMessageAndUpdateResult result = new SendMessageAndUpdateResult();
            result.setToBreak(true);

            log.info("批次号 {} 批次 {} 扫描任务已完成", bathId, jobType);
            return result;
        }
        SendMessageAndUpdateResult result = new SendMessageAndUpdateResult();
        result.setToBreak(false);


        return result;
    }

    @Override
    public  List<ReconciliationJobLogSumVO> reconciliationJobLogSumQuery(ReconciliationJobLogSumQuery query) {


        List<ReconciliationJobLogDO> jobLogDOList = this.lambdaQuery()
                .eq(ReconciliationJobLogDO::getBatchId, query.getBatchId())
                .list();
        List<ReconciliationJobLogSumVO> reconciliationJobLogSumVOS = BeanCopyUtils.copyBeanList(jobLogDOList, ReconciliationJobLogSumVO.class);

        for (ReconciliationJobLogSumVO re : reconciliationJobLogSumVOS) {

            ReconciliationJobTypeEnum jobType = re.getJobType();
            if (ReconciliationJobTypeEnum.TRADE_LONG_CHECK.equals(jobType) ){

                Integer fundBillDetailCount = signCustomerIFundBillDetailService.lambdaQuery()
                        .eq(SignCustomerFundBillDetailDO::getBusinessType, ReconciliationBusinessTypeEnum.PAYMENT)
                        .eq(SignCustomerFundBillDetailDO::getPayFinishDate, query.getBatchId())
                        .count();

                Integer diffMarkerCount = reconciliationDiffMarkerService.lambdaQuery()
                        .eq(ReconciliationDiffMarkerDO::getBatchId, query.getBatchId())
                        .eq(ReconciliationDiffMarkerDO::getBusinessType, ReconciliationBusinessTypeEnum.PAYMENT)
                        .eq(ReconciliationDiffMarkerDO::getDiffType, ReconciliationDiffTypeEnum.LONG)
                        .count();
                re.setFlowCount(fundBillDetailCount);
                re.setDiffMarker(diffMarkerCount);
            }
            if (ReconciliationJobTypeEnum.TRADE_SHORT_CHECK.equals(jobType) ){

                Integer orderFlowCount = payOrderFlowService.lambdaQuery()
                        .eq(PayOrderFlowDO::getPayFinishDate, query.getBatchId())
                        .count();
                Integer diffMarkerCount = reconciliationDiffMarkerService.lambdaQuery()
                        .eq(ReconciliationDiffMarkerDO::getBusinessType, ReconciliationBusinessTypeEnum.PAYMENT)
                        .eq(ReconciliationDiffMarkerDO::getBatchId, query.getBatchId())
                        .eq(ReconciliationDiffMarkerDO::getDiffType, ReconciliationDiffTypeEnum.SHORT)
                        .count();
                re.setFlowCount(orderFlowCount);
                re.setDiffMarker(diffMarkerCount);
            }


        }

        return reconciliationJobLogSumVOS;
    }
}
