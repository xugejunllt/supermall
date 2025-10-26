package com.lanf.mybatis.base;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.util.List;

/**
 * @author tanlingfei
 * @version 1.0
 * @description TODO
 * @date 2020/3/27 10:49
 */
@Data
public class TreeEntity extends BaseEntity {
   //名称
    @TableField("name")
    private String name;

   //上级分类id
    @TableField("parent_id")
    private String parentId;

   //排序
    @TableField("sort_value")
    private Integer sortValue;

    //完整路径
    @TableField("tree_path")
    private String treePath;

    //级别
    @TableField("level")
    private Integer level;

    //下级所有分类
    @TableField(exist = false)
    private List<TreeEntity> children;
}
