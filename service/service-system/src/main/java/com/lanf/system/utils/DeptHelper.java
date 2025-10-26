package com.lanf.system.utils;


import com.lanf.system.model.entiry.SysDeptDO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 根据部门数据构建部门树的工具类
 * </p>
 */
public class DeptHelper {

    /**
     * 使用递归方法构建部门
     *
     * @param SysDeptList
     * @return
     */
    public static List<SysDeptDO> buildTree(List<SysDeptDO> SysDeptList) {
        List<SysDeptDO> trees = new ArrayList<>();
        int root = 0;
        for (SysDeptDO sysDept : SysDeptList) {
            if ("0".equals(sysDept.getParentId()) || root == 0) {
                root++;
                trees.add(findChildren(sysDept, SysDeptList));
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
    public static SysDeptDO findChildren(SysDeptDO sysDept, List<SysDeptDO> treeNodes) {
        sysDept.setChildren(new ArrayList<SysDeptDO>());

        for (SysDeptDO it : treeNodes) {
            if (sysDept.getId().equals(it.getParentId())) {
                if (sysDept.getChildren() == null) {
                    sysDept.setChildren(new ArrayList<>());
                }
                sysDept.getChildren().add(findChildren(it, treeNodes));
            }
        }
        return sysDept;
    }


    /**
     * 使用递归方法构建部门
     *
     * @param SysDeptList
     * @return
     */
    public static List<Map> buildTreeMap(List<Map> SysDeptList) {
        List<Map> trees = new ArrayList<>();
        int root = 0;
        for (Map map : SysDeptList) {
            if ("0".equals("" + map.get("parent_id")) || root == 0) {
                root++;
                trees.add(findChildrenMap(map, SysDeptList));
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
    public static Map findChildrenMap(Map sysDept, List<Map> treeNodes) {
        sysDept.put("children", new ArrayList<Map>());

        for (Map it : treeNodes) {
            if (sysDept.get("id").toString().equals(it.get("parent_id").toString())) {
                if (sysDept.get("children") == null) {
                    sysDept.put("children", new ArrayList<Map>());
                }
                ((List<Map>) sysDept.get("children")).add(findChildrenMap(it, treeNodes));
            }
        }
        return sysDept;
    }


}
