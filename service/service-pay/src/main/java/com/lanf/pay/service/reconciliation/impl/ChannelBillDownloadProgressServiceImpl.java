package com.lanf.pay.service.reconciliation.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.client.pay.model.enums.PayChannelEnum;
import com.lanf.mybatis.utils.IdUtils;
import com.lanf.common.utils.JsonUtils;
import com.lanf.pay.mapper.ChannelBillDownloadProgressMapper;
import com.lanf.pay.model.entity.ChannelBillDownloadProgressDO;
import com.lanf.pay.model.enums.BillDownloadStatusEnum;
import com.lanf.pay.mq.constant.PayMqTopicName;
import com.lanf.pay.mq.message.BillSynchronizerMessage;
import com.lanf.pay.service.reconciliation.IChannelBillDownloadProgressService;
import com.lanf.rocketmq.util.RocketMqClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * 渠道对账单下载进度 服务实现类
 * </p>
 *
 * @author jarven
 * @since 2026-04-29
 */
@Slf4j
@Service
public class ChannelBillDownloadProgressServiceImpl extends ServiceImpl<ChannelBillDownloadProgressMapper, ChannelBillDownloadProgressDO> implements IChannelBillDownloadProgressService {

    @Autowired
    private RocketMqClient rocketMqClient;
    @Override
    public boolean addChannelBillDownloadProgress(String batchId, PayChannelEnum payChannel) {

        ChannelBillDownloadProgressDO one = this.lambdaQuery().eq(ChannelBillDownloadProgressDO::getBatchId, batchId)
                .eq(ChannelBillDownloadProgressDO::getPayChannel, payChannel).one();
        if (one != null) {
            log.warn("该批次已存在");
            return false;
        }

        ChannelBillDownloadProgressDO channelBillDownloadProgress = new ChannelBillDownloadProgressDO();
        channelBillDownloadProgress.setBatchId(batchId);
        channelBillDownloadProgress.setPayChannel(payChannel);
        channelBillDownloadProgress.setStatus(BillDownloadStatusEnum.DOWNLOADING);
        channelBillDownloadProgress.setVersion(0L);
        try {
            this.save(channelBillDownloadProgress);

            return true;
        } catch (DuplicateKeyException e) {
            log.warn("该批次已存在");
            return false;
        }
    }

    @Override
    public boolean exist(String batchId, PayChannelEnum payChannel) {

        ChannelBillDownloadProgressDO one = this.lambdaQuery()
                .eq(ChannelBillDownloadProgressDO::getBatchId, batchId)
                .eq(ChannelBillDownloadProgressDO::getPayChannel, payChannel)
                .one();
        return one != null;
    }

    @Transactional
    @Override
    public void redeliverTask(ChannelBillDownloadProgressDO downloadProgressDO,String billTyp) {


        String batchId = downloadProgressDO.getBatchId();

        String flowNo = IdUtils.generateId() + "";
        BillSynchronizerMessage message = new BillSynchronizerMessage();
        message.setPayChannel(downloadProgressDO.getPayChannel());
        message.setBillDate(batchId);
        message.setBillType(billTyp);
        message.setFlowNo(flowNo);

        this.lambdaUpdate()
                .eq(ChannelBillDownloadProgressDO::getId, downloadProgressDO.getId())
                .set(ChannelBillDownloadProgressDO::getFlowNo, null)
                .update();
        rocketMqClient.sendMessage(PayMqTopicName.BILL_SYNCHRONIZER_TOPIC,
                JsonUtils.toJsonString(message));

    }


}
