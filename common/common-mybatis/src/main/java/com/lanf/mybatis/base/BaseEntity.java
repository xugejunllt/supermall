package com.lanf.mybatis.base;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class BaseEntity implements Serializable {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    @TableField( fill = FieldFill.INSERT)
    private Date createTime;
    @TableField( fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    @TableLogic  //逻辑删除 默认效果 0 没有删除 1 已经删除
    @TableField( fill = FieldFill.INSERT)
    private Integer isDeleted;

}
