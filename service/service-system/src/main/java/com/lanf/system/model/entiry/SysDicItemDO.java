package com.lanf.system.model.entiry;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.lanf.mybatis.base.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author tanlingfei
 * @version 1.0
 * @description 字典选项 po类
 * @date 2020-04-13 16:12:32
 */
@Data
@ApiModel(description = "字典选项")
@TableName("sys_dic_item")
public class SysDicItemDO extends BaseEntity {

    private static final long serialVersionUID = 1L;
    @ApiModelProperty(value = "所属分类")
    @TableField("dic_code")
    private String dicCode;
    @ApiModelProperty(value = "所属分类名称")
    @TableField(exist = false)
    private String dicName;
    @ApiModelProperty(value = "编码")
    @TableField("code")
    private String code;
    @ApiModelProperty(value = "名称")
    @TableField("name")
    private String name;

    @TableField(fill = FieldFill.INSERT)
    private Long tenantId;
}
