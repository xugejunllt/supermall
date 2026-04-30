package com.lanf.pay.service.reconciliation.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.pay.mapper.ReconciliationJobLogMapper;
import com.lanf.pay.model.entity.ReconciliationJobLogDO;
import com.lanf.pay.service.reconciliation.IReconciliationJobLogService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 对账任务执行记录表 服务实现类
 * </p>
 *
 * @author jarven
 * @since 2026-04-30
 */
@Service
public class ReconciliationJobLogServiceImpl extends ServiceImpl<ReconciliationJobLogMapper, ReconciliationJobLogDO> implements IReconciliationJobLogService {

}
