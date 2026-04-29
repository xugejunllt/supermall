package com.lanf.pay.service.reconciliation;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.client.pay.model.enums.PayChannelEnum;
import com.lanf.pay.model.entity.ChannelBillDownloadProgress;

import java.util.List;

/**
 * <p>
 * 渠道对账单下载进度 服务类
 * </p>
 *
 * @author jarven
 * @since 2026-04-29
 */
public interface IChannelBillDownloadProgressService extends IService<ChannelBillDownloadProgress> {

    /**
     * 添加渠道对账单下载进度
     *
     *
     */
    boolean addChannelBillDownloadProgress(String batchId, PayChannelEnum payChannel);

    /**
     * 是否存在
     *
     *
     */
    boolean exist(String batchId, PayChannelEnum payChannel);

    /**
     *更新渠道对账单下载进度为已完成
     *
     */
    void  updateFinish(String batchId, PayChannelEnum payChannel);

    /**
     * 下载任务是否都完成
     *
     *
     */

    boolean allFinish(String batchId);

    /**
     * 获取可对账的支付渠道
     *
     */
    List<PayChannelEnum> getUsablePayChannel();
}
