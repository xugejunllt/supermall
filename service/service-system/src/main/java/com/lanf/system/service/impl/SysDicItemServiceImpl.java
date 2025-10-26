package com.lanf.system.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.system.mapper.SysDicItemMapper;
import com.lanf.system.model.entiry.SysDicItemDO;
import com.lanf.system.model.vo.SysDicItemQueryVO;
import com.lanf.system.service.SysDicItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author tanlingfei
 * @version 1.0
 * @description 字典选项 Service实现类
 * @date 2020-04-13 16:12:32
 */
@Transactional
@Service
public class SysDicItemServiceImpl extends ServiceImpl
        <SysDicItemMapper, SysDicItemDO> implements SysDicItemService {
    @Autowired
    private SysDicItemMapper sysDicItemMapper;

    @Override
    public IPage<SysDicItemDO> selectPage(Page<SysDicItemDO> pageParam, SysDicItemQueryVO sysDicItemQueryVo) {
        return sysDicItemMapper.selectPage(pageParam, sysDicItemQueryVo);
    }

    @Override
    public List<SysDicItemDO> queryList(SysDicItemQueryVO queryVo) {
        return sysDicItemMapper.queryList(queryVo);
    }

}
