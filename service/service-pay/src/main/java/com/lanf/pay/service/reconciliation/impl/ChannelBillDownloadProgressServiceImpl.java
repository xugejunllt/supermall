package com.lanf.pay.service.reconciliation.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.client.pay.model.enums.PayChannelEnum;
import com.lanf.common.utils.IStringUtils;
import com.lanf.constant.exception.BizException;
import com.lanf.pay.mapper.ChannelBillDownloadProgressMapper;
import com.lanf.pay.model.entity.ChannelBillDownloadProgressDO;
import com.lanf.pay.model.enums.BillDownloadStatusEnum;
import com.lanf.pay.service.reconciliation.IChannelBillDownloadProgressService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    @Override
    public void updateFinish(String batchId, PayChannelEnum payChannel) {
        ChannelBillDownloadProgressDO one = this.lambdaQuery().eq(ChannelBillDownloadProgressDO::getBatchId, batchId)
                .eq(ChannelBillDownloadProgressDO::getPayChannel, payChannel).one();
        if (one == null) {
            log.error("该批次不存在");
            throw new BizException("该批次不存在");
        }
        boolean update = this.lambdaUpdate()
                .eq(ChannelBillDownloadProgressDO::getBatchId, batchId)
                .eq(ChannelBillDownloadProgressDO::getVersion, one.getVersion())
                .eq(ChannelBillDownloadProgressDO::getStatus,0)
                .eq(ChannelBillDownloadProgressDO::getPayChannel, payChannel)
                .set(ChannelBillDownloadProgressDO::getStatus, 1)
                .set(ChannelBillDownloadProgressDO::getVersion, one.getVersion() + 1)
                .update();
        if (!update) {
            log.error("更新失败");
            throw new BizException("更新失败");
        }
    }

    @Override
    public boolean allFinish(String batchId) {
        // 获取可用的支付渠道列表
        List<PayChannelEnum> usableChannels = getUsablePayChannel();
        
        if (IStringUtils.isEmpty(usableChannels)) {
            log.warn("没有可用的支付渠道: batchId={}", batchId);
            return false;
        }
        
        // 查询该批次下所有状态为已完成（status=1）的记录
        List<ChannelBillDownloadProgressDO> finishedRecords = this.lambdaQuery()
                .eq(ChannelBillDownloadProgressDO::getBatchId, batchId)
                .eq(ChannelBillDownloadProgressDO::getStatus, 1)
                .list();
        
        if (finishedRecords == null || finishedRecords.isEmpty()) {
            log.info("该批次暂无完成的渠道: batchId={}", batchId);
            return false;
        }
        
        // 提取已完成的渠道编码集合
        Set<Integer> finishedChannelCodes = finishedRecords.stream()
                .map(record -> record.getPayChannel().getCode())
                .collect(Collectors.toSet());
        
        // 检查所有可用渠道是否都已完成
        boolean allComplete = usableChannels.stream()
                .allMatch(channel -> finishedChannelCodes.contains(channel.getCode()));
        
        if (allComplete) {
            log.info("所有渠道对账单下载完成: batchId={}, completed={}/{}", 
                    batchId, finishedChannelCodes.size(), usableChannels.size());
        } else {
            // 找出未完成的渠道
            List<PayChannelEnum> unfinishedChannels = usableChannels.stream()
                    .filter(channel -> !finishedChannelCodes.contains(channel.getCode()))
                    .collect(Collectors.toList());
            
            log.warn("部分渠道对账单未完成: batchId={}, 未完成渠道数={}, 未完成渠道={}", 
                    batchId, unfinishedChannels.size(), 
                    unfinishedChannels.stream().map(PayChannelEnum::name).collect(Collectors.toList()));
        }
        
        return allComplete;
    }

    @Override
    public List<PayChannelEnum> getUsablePayChannel() {
        return PayChannelEnum.AVAILABLE_CHANNELS;
    }
}
