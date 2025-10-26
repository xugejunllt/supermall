package com.lanf.sms.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 渠道模板
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-07-31
 */
@Data
@TableName("channel_template")
public class ChannelTemplateDO extends BaseEntity {

private static final long serialVersionUID=1L;

    private String channel;

    @ApiModelProperty(value = "模板id")
    private Long templateId;

    private String code;

    @ApiModelProperty(value = "使用场景")
    private String scene;




}
