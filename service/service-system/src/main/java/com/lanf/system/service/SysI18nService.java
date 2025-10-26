package com.lanf.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.system.model.entiry.SysI18nDO;
import com.lanf.system.model.vo.SysI18nQueryVO;

import java.util.List;

/**
 * @author tanlingfei
 * @version 1.0
 * @description 国际化语言 Service接口
 * @date 2023-10-31 13:47:32
 */
public interface SysI18nService extends IService<SysI18nDO> {
    IPage<SysI18nDO> selectPage(Page<SysI18nDO> pageParam, SysI18nQueryVO queryVo);

    List<SysI18nDO> queryList(SysI18nQueryVO queryVo);

    public boolean save(SysI18nDO sysI18n);

    public boolean updateById(SysI18nDO sysI18n);

    public SysI18nDO getById(String id);

    public List<SysI18nDO> getByIds(List<String> ids);
}
