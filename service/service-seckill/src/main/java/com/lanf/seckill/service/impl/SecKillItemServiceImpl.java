package com.lanf.seckill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.common.utils.BeanCopyUtils;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.seckill.mapper.SecKillItemMapper;
import com.lanf.seckill.model.entity.SecKillItemDO;
import com.lanf.seckill.service.ISecKillItemService;
import com.lanf.seckill.model.query.SecKillItemPageQuery;
import com.lanf.seckill.model.vo.SecKillItemPageVO;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 秒杀商品表 服务实现类
 * </p>
 *
 * @author jarven
 * @since 2026-05-07
 */
@Service
public class SecKillItemServiceImpl extends ServiceImpl<SecKillItemMapper, SecKillItemDO> implements ISecKillItemService {

    @Override
    public PageResult<SecKillItemPageVO> seckillItemPageQuery(SecKillItemPageQuery query) {

        long page = query.getPage();
        long pageSize = query.getPageSize();

        Page<SecKillItemDO> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<SecKillItemDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(SecKillItemDO::getCreateTime);
        Page<SecKillItemDO> resultPage = this.page(pageParam, wrapper);
        List<SecKillItemDO> records = resultPage.getRecords();
        if (records.isEmpty()) {
            return PageResult.emptyResult();
        }

        return new PageResult<>(BeanCopyUtils.copyBeanList(records, SecKillItemPageVO.class), pageSize, resultPage.getTotal());
    }

}
