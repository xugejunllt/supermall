package com.lanf.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.system.model.entiry.SysDeptDO;
import com.lanf.system.model.vo.SysDeptQueryVO;

import java.util.List;
import java.util.Map;

public interface SysDeptService extends IService<SysDeptDO> {

    /**
     * 部门树形数据
     * @return
     */
    public List<SysDeptDO> findNodes(SysDeptQueryVO sysDeptQueryVo);
    public List<Map> findSelectNodes();
    public boolean save(SysDeptDO sysDept);
    public boolean updateById(SysDeptDO sysDept);
    public List<SysDeptDO> findNodesByParent(String parentId);
}
