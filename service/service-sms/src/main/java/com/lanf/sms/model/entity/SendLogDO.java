package com.lanf.sms.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import lombok.Data;

/**
 * <p>
 * 
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-07-30
 */
@Data
@TableName("send_log")
public class SendLogDO extends BaseEntity {

private static final long serialVersionUID=1L;



    private String templateCode;
    private String channel;
    private String scene;

    private String phone;

    private String sendContent;

    //0:发送成功,1:发送失败
    private Integer status;

    private String failMessage;
}
