package com.lanf.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.system.model.entiry.SysDicItemDO;
import com.lanf.system.model.vo.SysDicItemQueryVO;

import java.util.List;

/**
 * @author tanlingfei
 * @version 1.0
 * @description 字典选项 Service接口
 * @date 2020-04-13 16:12:32
 */
public interface SysDicItemService extends IService<SysDicItemDO> {
    IPage<SysDicItemDO> selectPage(Page<SysDicItemDO> pageParam, SysDicItemQueryVO queryVo);

    List<SysDicItemDO> queryList(SysDicItemQueryVO queryVo);
}
