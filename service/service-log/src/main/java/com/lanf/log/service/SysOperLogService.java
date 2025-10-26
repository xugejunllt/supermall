package com.lanf.log.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.log.model.entity.SysOperLogDO;
import com.lanf.log.model.vo.SysOperLogQueryVo;

import java.util.List;
/**
* @author tanlingfei
* @version 1.0
* @description 操作日志记录 Service接口
* @date 2023-04-30 21:39:39
*/
public interface SysOperLogService extends IService<SysOperLogDO> {
    IPage<SysOperLogDO> selectPage(Page<SysOperLogDO> pageParam, SysOperLogQueryVo queryVo);
    List<SysOperLogDO> queryList(SysOperLogQueryVo queryVo);
    public boolean save(SysOperLogDO sysOperLog);
    public boolean updateById(SysOperLogDO sysOperLog);
    public SysOperLogDO getById(String id);
    public List<SysOperLogDO> getByIds(List<String> ids);
    //void updateStatus(String id, Integer status);
}
