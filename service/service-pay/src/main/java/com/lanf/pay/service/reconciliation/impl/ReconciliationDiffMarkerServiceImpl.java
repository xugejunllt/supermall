package com.lanf.pay.service.reconciliation.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.pay.mapper.ReconciliationDiffMarkerMapper;
import com.lanf.pay.model.entity.ReconciliationDiffMarkerDO;
import com.lanf.pay.service.reconciliation.IReconciliationDiffMarkerService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 去重标记 服务实现类
 * </p>
 *
 * @author jarven
 * @since 2026-04-30
 */
@Service
public class ReconciliationDiffMarkerServiceImpl extends ServiceImpl<ReconciliationDiffMarkerMapper, ReconciliationDiffMarkerDO> implements IReconciliationDiffMarkerService {

}
