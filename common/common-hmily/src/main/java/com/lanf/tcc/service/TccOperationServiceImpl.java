package com.lanf.tcc.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.constant.exception.BizException;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.tcc.mapper.TccOperationMapper;
import com.lanf.tcc.model.TccOperationDO;
import lombok.extern.slf4j.Slf4j;
import org.dromara.hmily.core.context.HmilyContextHolder;
import org.dromara.hmily.core.context.HmilyTransactionContext;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

/**
 * <p>
 * tcc操作记录 用于去重 服务实现类
 * </p>
 *
 * @author jarven
 * @since 2026-01-03
 */
@Slf4j
@Service
public class TccOperationServiceImpl extends ServiceImpl<TccOperationMapper, TccOperationDO> implements ITccOperationService {

    @Override
    public void tryOperation(String bizKey) {

        HmilyTransactionContext hmilyTransactionContext = HmilyContextHolder.get();

        TccOperationDO one = this.lambdaQuery().eq(TccOperationDO::getBizKey, bizKey).one();
        if (one != null) {
            log.info("bizKey重复");
            throw new BizException("bizKey重复");
        }

        TccOperationDO tccOperationDO = new TccOperationDO();
        tccOperationDO.setBizKey(bizKey);
        tccOperationDO.setStatus(0);
        tccOperationDO.setVersion(1L);
        tccOperationDO.setParticipantId(hmilyTransactionContext.getParticipantId());
        try {
            this.save(tccOperationDO);
        } catch (DuplicateKeyException e) {
            log.info("bizKey重复");
            throw new BizException("bizKey重复");
        }

    }
    /**
     *
     *
     *
     * 只有当 所有try执行成功后，才会执行confirm
     *
     */
    @Override
    public boolean confirmOperation(String bizKey) {

        TccOperationDO one = this.lambdaQuery().eq(TccOperationDO::getBizKey, bizKey).one();

        if (one.getStatus() == 1){
            log.info("confirm重复执行");
            return false;
        }
        boolean update = this.lambdaUpdate().eq(BaseEntity::getId, one.getId()).
                eq(TccOperationDO::getVersion, one.getVersion()).
                set(TccOperationDO::getStatus, 1).
                set(TccOperationDO::getVersion, one.getVersion() + 1).
                update();
        if (!update) {
            log.info("更新失败");
            throw new BizException("更新失败");
        }
        return true;
    }

    /**
     *
     *
     *
     * 只有当 try被执行后，才会被调用，即hmily_transaction_participant已经有记录了
     *
     */
    @Override
    public boolean cancelOperation(String bizKey) {

        TccOperationDO one = this.lambdaQuery().eq(TccOperationDO::getBizKey, bizKey).one();

        if (one == null) {
            throw new BizException("tcc操作记录不存在");
        }

        if (one.getStatus() == 2 ) {
            log.info("cancel重复执行");
            return false;
        }
        if (one.getStatus() == 1 ) {
            log.info("confirm已执行");
            return false;
        }
        /**
         * 隐含条件 当 status = 0时 才能更新
         */
        boolean update = this.lambdaUpdate().eq(BaseEntity::getId, one.getId()).
                eq(TccOperationDO::getVersion, one.getVersion()).
                set(TccOperationDO::getStatus, 2).
                set(TccOperationDO::getVersion, one.getVersion() + 1).
                update();

        if (!update) {
            log.info("更新失败");
            throw new BizException("更新失败");
        }
        return true;
    }
}
