package com.lanf.pay.model.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.api.pay.model.enums.PayChannelEnum;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.pay.model.enums.BillDownloadStatusEnum;
import com.lanf.pay.model.enums.BillTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 渠道对账单下载进度
 * </p>
 *
 * @author jarven
 * @since 2026-04-29
 */
@Data
@TableName("channel_bill_download_progress")
public class ChannelBillDownloadProgressDO extends BaseEntity {

    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "批次号，如 2026-04-29")
    private String batchId;

    private PayChannelEnum payChannel;

    @ApiModelProperty(value = "1:下载中，1：下载完成")
    private BillDownloadStatusEnum status;

    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String flowNo;

    private BillTypeEnum billType;


    private Long version;


}
