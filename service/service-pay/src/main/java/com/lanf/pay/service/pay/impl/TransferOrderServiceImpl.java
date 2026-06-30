package com.lanf.pay.service.pay.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.api.pay.model.query.TransferOrderPageQuery;
import com.lanf.api.pay.model.vo.TransferOrderPageVO;
import com.lanf.common.utils.BeanCopyUtils;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.pay.mapper.TransferOrderMapper;
import com.lanf.pay.model.entity.TransferOrderDO;
import com.lanf.pay.service.pay.ITransferOrderService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 转账单 服务实现类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-08-03
 */
@Service
public class TransferOrderServiceImpl extends ServiceImpl<TransferOrderMapper, TransferOrderDO> implements ITransferOrderService {

    @Override
    public PageResult<TransferOrderPageVO> transferOrderPageQuery(TransferOrderPageQuery query) {
        Page<TransferOrderDO> page = this.page(new Page<>(query.getPage(), query.getPageSize()),
                new LambdaQueryWrapper<TransferOrderDO>()
                        .orderByDesc(TransferOrderDO::getCreateTime));
        List<TransferOrderDO> records = page.getRecords();
        if (records.isEmpty()) {
            return null;
        }
        List<TransferOrderPageVO> pageVOS = BeanCopyUtils.copyBeanList(records, TransferOrderPageVO.class);
        PageResult<TransferOrderPageVO> result = new PageResult<>();
        result.setTotal(page.getTotal());
        result.setRecords(pageVOS);
        result.setSize(page.getSize());
        return result;
    }

}
