package com.lanf.storage.service.reconciliation.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.storage.mapper.ReconciliationOrderDetailMapper;
import com.lanf.storage.model.entity.ReconciliationOrderDetailDO;
import com.lanf.storage.service.reconciliation.IReconciliationOrderDetailService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 库存对账订单详细 服务实现类
 * </p>
 *
 * @author jarven
 * @since 2026-05-06
 */
@Service
public class ReconciliationOrderDetailServiceImpl extends ServiceImpl<ReconciliationOrderDetailMapper, ReconciliationOrderDetailDO> implements IReconciliationOrderDetailService {

}
