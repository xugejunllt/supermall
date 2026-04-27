package com.lanf.pay.service.wallet.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.pay.mapper.WalletAccountMapper;
import com.lanf.pay.model.bo.AddWalletAccount;
import com.lanf.pay.model.entity.WalletAccountDO;
import com.lanf.pay.service.wallet.IWalletAccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 钱包账户表 服务实现类
 * </p>
 *
 * @author jarven
 * @since 2026-04-27
 */
@Slf4j
@Service
public class WalletAccountServiceImpl extends ServiceImpl<WalletAccountMapper, WalletAccountDO> implements IWalletAccountService {

    @Override
    public void addWalletAccount(AddWalletAccount dto) {

        WalletAccountDO walletAccountDO = new WalletAccountDO();
        walletAccountDO.setUserId(dto.getUserId());
        try {
            this.save(walletAccountDO);
        } catch (DuplicateKeyException e) {
            log.warn("该用户已存在钱包账户");
        }


    }
}
