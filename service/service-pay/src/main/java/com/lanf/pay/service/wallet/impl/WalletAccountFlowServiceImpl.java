package com.lanf.pay.service.wallet.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.api.pay.model.query.WalletAccountFlowPageQuery;
import com.lanf.api.pay.model.vo.WalletAccountFlowPageVO;
import com.lanf.common.utils.BeanCopyUtils;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.pay.mapper.WalletAccountFlowMapper;
import com.lanf.pay.model.entity.WalletAccountFlowDO;
import com.lanf.pay.service.wallet.IWalletAccountFlowService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 钱包账户表 服务实现类
 * </p>
 *
 * @author jarven
 * @since 2026-04-27
 */
@Service
public class WalletAccountFlowServiceImpl extends ServiceImpl<WalletAccountFlowMapper, WalletAccountFlowDO> implements IWalletAccountFlowService {

    @Override
    public PageResult<WalletAccountFlowPageVO> walletAccountFlowPageQuery(WalletAccountFlowPageQuery query) {
        Page<WalletAccountFlowDO> page = this.page(new Page<>(query.getPage(), query.getPageSize()),
                new LambdaQueryWrapper<WalletAccountFlowDO>()
                        .eq(query.getWalletAccountId() != null, WalletAccountFlowDO::getWalletAccountId, query.getWalletAccountId())
                        .orderByDesc(WalletAccountFlowDO::getCreateTime));
        List<WalletAccountFlowDO> records = page.getRecords();
        if (records.isEmpty()) {
            return null;
        }
        List<WalletAccountFlowPageVO> pageVOS = BeanCopyUtils.copyBeanList(records, WalletAccountFlowPageVO.class);
        PageResult<WalletAccountFlowPageVO> result = new PageResult<>();
        result.setTotal(page.getTotal());
        result.setRecords(pageVOS);
        result.setSize(page.getSize());
        return result;
    }
}
