package com.lanf.pay.service.clearing.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.pay.mapper.ClearingDetailMapper;
import com.lanf.pay.model.entity.ClearingDetailDO;
import com.lanf.api.pay.model.query.ClearingDetailPageQuery;
import com.lanf.api.pay.model.vo.ClearingDetailPageVO;
import com.lanf.pay.service.clearing.ClearingDetailService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 * 平台清算流水 服务实现类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-20
 */
@Service
public class ClearingDetailServiceImpl extends ServiceImpl<ClearingDetailMapper, ClearingDetailDO> implements ClearingDetailService {

    @Override
    public PageResult<ClearingDetailPageVO> clearingDetailPageQuery(ClearingDetailPageQuery query) {
        Page<ClearingDetailDO> page = new Page<>(query.getPage(), query.getPageSize());
        LambdaQueryWrapper<ClearingDetailDO> wrapper = new LambdaQueryWrapper<>();


        Page<ClearingDetailDO> resultPage = baseMapper.selectPage(page, wrapper);
        List<ClearingDetailPageVO> records = resultPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return new PageResult<>(records, resultPage.getSize(), resultPage.getTotal());
    }

    private ClearingDetailPageVO convertToVO(ClearingDetailDO entity) {
        ClearingDetailPageVO vo = new ClearingDetailPageVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    public BigDecimal sumIncomeMoney(Date startTime, Date endTime) {
        return baseMapper.sumIncomeMoney(startTime, endTime);
    }
}
