package com.lanf.system.model.entiry;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <p>
 * 地域信息
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-05-30
 */
@Data
@TableName("base_area")
public class BaseAreaDO extends BaseEntity {

private static final long serialVersionUID=1L;


    @ApiModelProperty(value = "父级id")
    private Long parentId;

    @ApiModelProperty(value = "地域名称")
    private String cityName;

    private Integer type;







}
