package com.lanf.sms.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import lombok.Data;

/**
 * <p>
 * 短信模板 只允许新增，不允许修改 不然全部模板内参数都有可能出错
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-07-30
 */
@Data
@TableName("template")
public class TemplateDO extends BaseEntity {

private static final long serialVersionUID=1L;



    private String code;
    private String channel;
    private String name;

    private Integer type;

    private String scene;

    private String content;

    private String placeholderKey;

}
