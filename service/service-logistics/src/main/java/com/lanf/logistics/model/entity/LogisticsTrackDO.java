package com.lanf.logistics.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * <p>
 * 物流轨迹
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-07-25
 */
@Data
@TableName("logistics_track")
public class LogisticsTrackDO extends BaseEntity {

private static final long serialVersionUID=1L;


    private Long orderId;

    @ApiModelProperty(value = "物流状态")
    private Integer status;

    @ApiModelProperty(value = "当前完成时间")
    private Date finishTime;

    @ApiModelProperty(value = "完成内容")
    private String finishContent;





}
