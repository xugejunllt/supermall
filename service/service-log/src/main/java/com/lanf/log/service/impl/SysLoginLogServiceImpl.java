package com.lanf.log.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.log.mapper.SysLoginLogMapper;
import com.lanf.log.model.entity.SysLoginLogDO;
import com.lanf.log.model.vo.SysLoginLogQueryVo;
import com.lanf.log.service.SysLoginLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
/**
* @author tanlingfei
* @version 1.0
* @description 系统访问记录 Service实现类
* @date 2023-04-30 21:36:41
*/
@Transactional
@Service
public class SysLoginLogServiceImpl extends ServiceImpl
<SysLoginLogMapper, SysLoginLogDO> implements SysLoginLogService {
    @Autowired
    private SysLoginLogMapper sysLoginLogMapper;

    @Override
    public IPage<SysLoginLogDO> selectPage(Page<SysLoginLogDO> pageParam, SysLoginLogQueryVo sysLoginLogQueryVo) {
        //QueryWrapper<SysLoginLog> queryWrapper = new QueryWrapper<>();
        //return sysLoginLogMapper.selectPage(pageParam,queryWrapper);
        return sysLoginLogMapper.selectPage(pageParam,sysLoginLogQueryVo);
    }

    @Override
    public List<SysLoginLogDO> queryList(SysLoginLogQueryVo sysLoginLogQueryVo){
        return sysLoginLogMapper.queryList(sysLoginLogQueryVo);
    }
    @Override
    public boolean save(SysLoginLogDO sysLoginLog){
        int result = this.sysLoginLogMapper.insert(sysLoginLog);
        return result>0;
    }
    @Override
    public boolean updateById(SysLoginLogDO sysLoginLog){
        int row = this.sysLoginLogMapper.updateById(sysLoginLog);
        return row>0;
    }
    @Override
    public SysLoginLogDO getById(String id){
         SysLoginLogDO sysLoginLog = sysLoginLogMapper.selectById(id);
        return sysLoginLog;
    }
   @Override
   public List<SysLoginLogDO> getByIds(List<String> ids) {
      List<SysLoginLogDO> list = this.sysLoginLogMapper.selectBatchIds(ids);
           return list;
     }
    /**
    @Override
    public void updateStatus(String id, Integer status) {
        SysLoginLog sysLoginLog = sysLoginLogMapper.selectById(id);
        sysLoginLog.setStatus(status);
        sysLoginLogMapper.updateById(sysLoginLog);
    }**/
}
