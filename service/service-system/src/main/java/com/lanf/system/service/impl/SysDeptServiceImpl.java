package com.lanf.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.security.utils.UserUtils;
import com.lanf.system.mapper.SysDeptMapper;
import com.lanf.system.model.bo.SysUserBO;
import com.lanf.system.model.entiry.SysDeptDO;
import com.lanf.system.model.vo.SysDeptQueryVO;
import com.lanf.system.service.SysDeptService;
import com.lanf.system.utils.DeptHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Transactional
@Service
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDeptDO> implements SysDeptService {
    @Autowired
    private SysDeptMapper sysDeptMapper;

    @Override
    public boolean save(SysDeptDO sysDept) {
        if (sysDept.getParentId() == null){
            sysDept.setParentId(0L);
        }

        this.sysDeptMapper.insert(sysDept);
        if (sysDept.getParentId() == 0L) {
            sysDept.setLevel(1);
        } else {
            SysDeptDO sysDept1 = this.getById(sysDept.getParentId());
            sysDept.setLevel(sysDept1.getLevel() + 1);
        }
        String treePath = this.getTreePath(sysDept);
        sysDept.setTreePath(treePath);
        this.sysDeptMapper.updateById(sysDept);
        return true;
    }

    public boolean updateById(SysDeptDO sysDept) {
        if ("0".equals(sysDept.getParentId()) || StringUtils.isEmpty(sysDept.getParentId())) {
            sysDept.setLevel(1);
        } else {
            SysDeptDO sysDept1 = this.getById(sysDept.getParentId());
            sysDept.setLevel(sysDept1.getLevel() + 1);
        }
        String treePath = this.getTreePath(sysDept);
        sysDept.setTreePath(treePath);
        this.sysDeptMapper.updateById(sysDept);
        return true;
    }

    private String getTreePath(SysDeptDO sysDept) {
        if (sysDept.getParentId() == null || sysDept.getParentId() == 0L) {
            return "," + sysDept.getId() + ",";
        } else {
            SysDeptDO sysDept1 = this.getById(sysDept.getParentId());
            if (!StringUtils.isEmpty(sysDept1.getTreePath())) {
                return sysDept1.getTreePath() + sysDept.getId() + ",";
            }
            return getTreePath(sysDept1) + sysDept.getId() + ",";
        }
    }

    @Override
    public List<SysDeptDO> findNodes(SysDeptQueryVO sysDeptQueryVo) {
        //全部部门列表
        SysUserBO sysUser = UserUtils.getUserInfo();
        if ("admin".equals(sysUser.getUsername())) {
            sysDeptQueryVo.setDeptId(null);
        } else {
            if (StringUtils.isEmpty(sysUser.getDeptId())) {
                return null;
            }
            sysDeptQueryVo.setDeptId(sysUser.getDeptId());
        }
        List<SysDeptDO> sysDptList = sysDeptMapper.queryList(sysDeptQueryVo);
        if (CollectionUtils.isEmpty(sysDptList)) return null;
        //构建树形数据
        List<SysDeptDO> result = DeptHelper.buildTree(sysDptList);
        return result;
    }

    @Override
    public List<SysDeptDO> findNodesByParent(String parentId) {
        QueryWrapper<SysDeptDO> queryWrapper = new QueryWrapper<>();
        SysUserBO sysUser = UserUtils.getUserInfo();
        String deptId = sysUser.getDeptId();
        if (!"1".equals(sysUser.getId()) && "0".equals(parentId)) {
            if (StringUtils.isEmpty(deptId)) {
                return null;
            }
            //找到当前用户所属部门根节点
            QueryWrapper<SysDeptDO> findWrapper = new QueryWrapper<>();
            findWrapper.eq("id", deptId);
            findWrapper.orderByAsc("level");
            List<SysDeptDO> findList = this.list(findWrapper);
            if (!CollectionUtils.isEmpty(findList)) {
                return null;
            }
            SysDeptDO sysDept = findList.get(0);
            queryWrapper.eq("id", sysDept.getId());
        } else {
            queryWrapper.eq("parent_id", parentId);
        }
        List<SysDeptDO> sysDptList = this.list(queryWrapper);
        return sysDptList;
    }

    @Override
    public List<Map> findSelectNodes() {
        //全部部门列表
        SysDeptQueryVO sysDeptQueryVo = new SysDeptQueryVO();
        SysUserBO sysUser = UserUtils.getUserInfo();
        if ("1".equals(sysUser.getId())) {
            sysDeptQueryVo.setDeptId(null);
        } else {
            if (StringUtils.isEmpty(sysUser.getDeptId())) {
                return null;
            }
            sysDeptQueryVo.setDeptId(sysUser.getDeptId());
        }
        List<Map> sysDptList = sysDeptMapper.findList(sysDeptQueryVo);
        if (CollectionUtils.isEmpty(sysDptList)) return null;
        //构建树形数据
        List<Map> result = DeptHelper.buildTreeMap(sysDptList);
        return result;
    }

    @Override
    public boolean removeById(Serializable id) {
        int count = this.count(new QueryWrapper<SysDeptDO>().eq("parent_id", id));
        if (count > 0) {
            throw new RuntimeException();
        }
        sysDeptMapper.deleteById(id);
        return false;
    }
}
