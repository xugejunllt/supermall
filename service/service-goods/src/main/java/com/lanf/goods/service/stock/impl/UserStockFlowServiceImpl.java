package com.lanf.goods.service.stock.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.api.goods.model.query.ReconciliationStockFlowQuery;
import com.lanf.api.goods.model.query.UserStockFlowPageQuery;
import com.lanf.api.goods.model.vo.ReconciliationStockFlow;
import com.lanf.api.goods.model.vo.ReconciliationStockFlowVO;
import com.lanf.api.goods.model.vo.UserStockFlowPageVO;
import com.lanf.common.utils.BeanCopyUtils;
import com.lanf.common.utils.IStringUtils;
import com.lanf.constant.exception.BizException;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.goods.mapper.UserStockFlowMapper;
import com.lanf.goods.model.entity.UserStockFlowDO;
import com.lanf.goods.service.stock.IUserStockFlowService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 库存流水 服务实现类
 * </p>
 *
 * @author jarven
 * @since 2026-01-03
 */
@Service
public class UserStockFlowServiceImpl extends ServiceImpl<UserStockFlowMapper, UserStockFlowDO> implements IUserStockFlowService {

    @Override
    public ReconciliationStockFlowVO reconciliationStockFlowQuery(ReconciliationStockFlowQuery query) {


        List<UserStockFlowDO> stockFlowDOS = this.lambdaQuery()
                .eq(UserStockFlowDO::getOrderId, query.getOrderId())
                .eq(UserStockFlowDO::getEventType, query.getUserStockFlowEventType())
                .list();
        if (IStringUtils.isEmpty(stockFlowDOS)){

            throw new BizException("库存流水不存在");
        }
        List<ReconciliationStockFlow> reconciliationStockFlowList =
                stockFlowDOS.stream().map(item -> {
                    ReconciliationStockFlow reconciliationStockFlow = new ReconciliationStockFlow();
                    reconciliationStockFlow.setQuantity(item.getChangeQuantity());
                    reconciliationStockFlow.setSkuCode(item.getSkuCode());
                    reconciliationStockFlow.setWarehouseId(item.getWarehouseId());
                    return reconciliationStockFlow;

        }).collect(Collectors.toList());

        ReconciliationStockFlowVO vo = new ReconciliationStockFlowVO();
        vo.setReconciliationStockFlowList(reconciliationStockFlowList);


        return vo;
    }
    @Override
    public PageResult<UserStockFlowPageVO> userStockFlowPageQuery(UserStockFlowPageQuery query) {
        IPage<UserStockFlowDO> page = new Page<>(query.getPage(), query.getPageSize());

        LambdaQueryWrapper<UserStockFlowDO> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(!IStringUtils.isEmpty(query.getSkuCode()), UserStockFlowDO::getSkuCode, query.getSkuCode())
                .eq(query.getUserStockId() != null, UserStockFlowDO::getUserStockId, query.getUserStockId())
                .eq(query.getWarehouseId() != null, UserStockFlowDO::getWarehouseId, query.getWarehouseId())
                .eq(query.getOrderId() != null, UserStockFlowDO::getOrderId, query.getOrderId())
                .orderByDesc(UserStockFlowDO::getCreateTime);

        IPage<UserStockFlowDO> result = this.page(page, wrapper);

        PageResult<UserStockFlowPageVO> pageResult = new PageResult<>();
        pageResult.setRecords(BeanCopyUtils.copyBeanList(result.getRecords(), UserStockFlowPageVO.class));
        pageResult.setTotal(result.getTotal());
        pageResult.setSize(result.getSize());

        return pageResult;
    }

}
