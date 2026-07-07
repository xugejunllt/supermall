package com.lanf.seckill.mq.message;

import com.lanf.constant.mq.base.BaseMessage;
import com.lanf.seckill.model.enums.SeckillModeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class SecKillMqExecuteMessage extends BaseMessage {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "用户ID")
    private Long userId;

    @ApiModelProperty(value = "秒杀商品ID")
    private Long secKillItemId;

    private SeckillModeEnum seckillModeEnum;




}
