package com.lanf.system.utils;


import com.lanf.mybatis.base.TreeEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * @author tanlingfei
 * @version 1.0
 * @description TODO
 * @date 2023-03-31 14:12:19
 */
public class TreeHelper {

    /**
     * 使用递归方法构建数据
     *
     * @param treeList
     * @return
     */
    public static <T extends TreeEntity> List<TreeEntity> buildTree(List<T> treeList) {
        List<TreeEntity> trees = new ArrayList<>();
        for (T treeEntity : treeList) {
            if ("0".equals(treeEntity.getParentId())) {
                trees.add(findChildren(treeEntity, treeList));
            }
        }
        return trees;
    }

    /**
     * 递归查找子节点
     *
     * @param treeNodes
     * @return
     */
    public static TreeEntity findChildren(TreeEntity treeEntity, List<? extends TreeEntity> treeNodes) {
        treeEntity.setChildren(new ArrayList<TreeEntity>());

        for (TreeEntity it : treeNodes) {
            if (treeEntity.getId().equals(it.getParentId())) {
                if (treeEntity.getChildren() == null) {
                    treeEntity.setChildren(new ArrayList<>());
                }
                treeEntity.getChildren().add(findChildren(it, treeNodes));
            }
        }
        return treeEntity;
    }
}
