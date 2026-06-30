package com.lanf.pay.service.wallet.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.pay.mapper.WalletWithdrawMapper;
import com.lanf.pay.model.entity.WalletWithdrawDO;
import com.lanf.api.pay.model.query.WalletWithdrawPageQuery;
import com.lanf.api.pay.model.vo.WalletWithdrawPageVO;
import com.lanf.pay.service.wallet.IWalletWithdrawService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 钱包提现记录表 服务实现类
 * </p>
 *
 * @author jarven
 * @since 2026-04-28
 */
@Service
public class WalletWithdrawServiceImpl extends ServiceImpl<WalletWithdrawMapper, WalletWithdrawDO> implements IWalletWithdrawService {

    @Override
    public PageResult<WalletWithdrawPageVO> walletWithdrawPageQuery(WalletWithdrawPageQuery query) {
        Page<WalletWithdrawDO> page = new Page<>(query.getPage(), query.getPageSize());
        LambdaQueryWrapper<WalletWithdrawDO> wrapper = new LambdaQueryWrapper<>();


        Page<WalletWithdrawDO> resultPage = baseMapper.selectPage(page, wrapper);
        List<WalletWithdrawPageVO> records = resultPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return new PageResult<>(records, resultPage.getSize(), resultPage.getTotal());
    }

    private WalletWithdrawPageVO convertToVO(WalletWithdrawDO entity) {
        WalletWithdrawPageVO vo = new WalletWithdrawPageVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
}
