package com.lanf.pay.model.vo;

import com.lanf.api.pay.model.enums.PayChannelEnum;
import com.lanf.pay.model.enums.BillDownloadStatusEnum;
import com.lanf.pay.model.enums.BillTypeEnum;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class ChannelBillDownloadProgressListVO implements Serializable {

    private Long id;
    private String batchId;

    private PayChannelEnum payChannel;

    /**
     * 1:下载中，1：下载完成
     */
    private BillDownloadStatusEnum status;



    private BillTypeEnum billType;

    private Date createTime;

}
