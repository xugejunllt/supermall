package com.lanf.log.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.log.model.entity.SysLoginLogDO;
import com.lanf.log.model.vo.SysLoginLogQueryVo;

import java.util.List;
/**
* @author tanlingfei
* @version 1.0
* @description 系统访问记录 Service接口
* @date 2023-04-30 21:36:41
*/
public interface SysLoginLogService extends IService<SysLoginLogDO> {
    IPage<SysLoginLogDO> selectPage(Page<SysLoginLogDO> pageParam, SysLoginLogQueryVo queryVo);
    List<SysLoginLogDO> queryList(SysLoginLogQueryVo queryVo);
    public boolean save(SysLoginLogDO sysLoginLog);
    public boolean updateById(SysLoginLogDO sysLoginLog);
    public SysLoginLogDO getById(String id);
    public List<SysLoginLogDO> getByIds(List<String> ids);
    //void updateStatus(String id, Integer status);
}
