package com.lanf.log.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.log.mapper.SysOperLogMapper;
import com.lanf.log.model.entity.SysOperLogDO;
import com.lanf.log.model.vo.SysOperLogQueryVo;
import com.lanf.log.service.SysOperLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
/**
* @author tanlingfei
* @version 1.0
* @description 操作日志记录 Service实现类
* @date 2023-04-30 21:39:39
*/
@Transactional
@Service
public class SysOperLogServiceImpl extends ServiceImpl
<SysOperLogMapper, SysOperLogDO> implements SysOperLogService {
    @Autowired
    private SysOperLogMapper sysOperLogMapper;

    @Override
    public IPage<SysOperLogDO> selectPage(Page<SysOperLogDO> pageParam, SysOperLogQueryVo sysOperLogQueryVo) {
        //QueryWrapper<SysOperLog> queryWrapper = new QueryWrapper<>();
        //return sysOperLogMapper.selectPage(pageParam,queryWrapper);
        return sysOperLogMapper.selectPage(pageParam,sysOperLogQueryVo);
    }

    @Override
    public List<SysOperLogDO> queryList(SysOperLogQueryVo sysOperLogQueryVo){
        return sysOperLogMapper.queryList(sysOperLogQueryVo);
    }
    @Override
    public boolean save(SysOperLogDO sysOperLog){
        int result = this.sysOperLogMapper.insert(sysOperLog);
        return result>0;
    }
    @Override
    public boolean updateById(SysOperLogDO sysOperLog){
        int row = this.sysOperLogMapper.updateById(sysOperLog);
        return row>0;
    }
    @Override
    public SysOperLogDO getById(String id){
         SysOperLogDO sysOperLog = sysOperLogMapper.selectById(id);
        return sysOperLog;
    }
   @Override
   public List<SysOperLogDO> getByIds(List<String> ids) {
      List<SysOperLogDO> list = this.sysOperLogMapper.selectBatchIds(ids);
           return list;
     }
    /**
    @Override
    public void updateStatus(String id, Integer status) {
        SysOperLog sysOperLog = sysOperLogMapper.selectById(id);
        sysOperLog.setStatus(status);
        sysOperLogMapper.updateById(sysOperLog);
    }**/
}
