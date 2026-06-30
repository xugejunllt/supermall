package com.lanf.pay.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lanf.pay.model.entity.ChannelBillDownloadProgressDO;

/**
 * <p>
 * 渠道对账单下载进度 Mapper 接口
 * </p>
 *
 * @author jarven
 * @since 2026-04-29
 */
public interface ChannelBillDownloadProgressMapper extends BaseMapper<ChannelBillDownloadProgressDO> {

    /**
     * 物理删除全表数据
     */
    int deleteAll();

}
