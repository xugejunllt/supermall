package com.lanf.seckill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.common.utils.BeanCopyUtils;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.seckill.mapper.SecKillRecordMapper;
import com.lanf.seckill.model.entity.SecKillRecordDO;
import com.lanf.seckill.service.ISecKillRecordService;
import com.lanf.seckill.model.query.SecKillRecordPageQuery;
import com.lanf.seckill.model.vo.SecKillRecordPageVO;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 秒杀记录表 服务实现类
 * </p>
 *
 * @author jarven
 * @since 2026-05-09
 */
@Service
public class SecKillRecordServiceImpl extends ServiceImpl<SecKillRecordMapper, SecKillRecordDO> implements ISecKillRecordService {

    @Override
    public PageResult<SecKillRecordPageVO> seckillRecordPageQuery(SecKillRecordPageQuery query) {
        Long userId = query.getUserId();
        Long secKillItemId = query.getSecKillItemId();
        long page = query.getPage();
        long pageSize = query.getPageSize();

        Page<SecKillRecordDO> pageParam = new Page<>(page, pageSize);

        LambdaQueryWrapper<SecKillRecordDO> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            wrapper.eq(SecKillRecordDO::getUserId, userId);
        }
        if (secKillItemId != null) {
            wrapper.eq(SecKillRecordDO::getSecKillItemId, secKillItemId);
        }
        wrapper.orderByDesc(SecKillRecordDO::getCreateTime);

        Page<SecKillRecordDO> resultPage = this.page(pageParam, wrapper);

        List<SecKillRecordDO> records = resultPage.getRecords();
        if (records.isEmpty()) {
            return PageResult.emptyResult();
        }

        return new PageResult<>(BeanCopyUtils.copyBeanList(records, SecKillRecordPageVO.class), pageSize, resultPage.getTotal());
    }

}
