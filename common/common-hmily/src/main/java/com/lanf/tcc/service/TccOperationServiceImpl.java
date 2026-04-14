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
    public void tryOperation(String bizKey, String parameter) {

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
        tccOperationDO.setParameter(parameter);
        try {
            this.save(tccOperationDO);
        } catch (DuplicateKeyException e) {
            /**
             * 数据库压力大时  tcc_operation根据bizKey分表
             * 提高性能
             */

            log.info("bizKey重复");
            throw new BizException("bizKey重复");
        }

    }

    /**
     * 只有当 所有try执行成功后，才会执行confirm
     */
    @Override
    public boolean confirmOperation(String bizKey) {

        TccOperationDO one = this.lambdaQuery().eq(TccOperationDO::getBizKey, bizKey).one();
        if (one == null) {
            /**
             * try 阶段 操作DB时 已经return了表示执行成功
             */
            return false;
        }
        if (one.getStatus() == 1) {
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
     * 在准备执行try时前  hmily_transaction_participant写入记录
     * 当hmily_transaction_participant记录存在记录时 下面就会被调用
     */
    @Override
    public boolean cancelOperation(String bizKey) {

        TccOperationDO one = this.lambdaQuery().eq(TccOperationDO::getBizKey, bizKey).one();

        if (one == null) {
            /**
             *
             *
             * 防悬挂
             *
             * try 执行时 刚好服务挂了 此时tcc事务状态为 开始执行try状态
             *
             * 而实际try方法并没有执行 如果抛出异常 tcc方法会一直调用
             * 直到达到最大重试次数 人工去比对tcc全部事务与tcc_operation
             * 记录 如果tcc_operation无记录 则忽略不处理
             *
             */
            log.error("try阶段未执行");
            throw new BizException("try阶段未执行");
        }

        if (one.getStatus() == 2) {
            log.info("cancel重复执行");
            return false;
        }
        if (one.getStatus() == 1) {
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

    @Override
    public String getParameter(String bizKey) {

        TccOperationDO one = this.lambdaQuery().eq(TccOperationDO::getBizKey, bizKey).one();
        if (one != null) {

            return one.getParameter();
        }
        log.error("bizKey不存在[{}]",bizKey);
        throw new BizException("bizKey不存在");
    }
}
