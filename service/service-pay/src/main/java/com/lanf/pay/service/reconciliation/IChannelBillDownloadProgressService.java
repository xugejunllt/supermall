package com.lanf.pay.service.reconciliation;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.api.pay.model.enums.PayChannelEnum;
import com.lanf.pay.model.entity.ChannelBillDownloadProgressDO;
import com.lanf.api.pay.model.query.ChannelBillDownloadProgressListQuery;
import com.lanf.api.pay.model.vo.ChannelBillDownloadProgressListVO;

import java.util.List;

/**
 * <p>
 * 渠道对账单下载进度 服务类
 * </p>
 *
 * @author jarven
 * @since 2026-04-29
 */
public interface IChannelBillDownloadProgressService extends IService<ChannelBillDownloadProgressDO> {

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
     * 重新投递mq 解析任务
     *
     */
    void redeliverTask(ChannelBillDownloadProgressDO downloadProgressDO,String billTyp);

    List<ChannelBillDownloadProgressListVO> channelBillDownloadProgressListQuery(ChannelBillDownloadProgressListQuery query);
}
