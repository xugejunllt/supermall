package com.lanf.pay.service.pay;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.api.pay.model.query.TransferOrderFlowPageQuery;
import com.lanf.api.pay.model.vo.TransferOrderFlowPageVO;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.pay.model.entity.TransferOrderFlowDO;

import java.math.BigDecimal;

/**
 * <p>
 * 转账单 服务类
 * </p>
 *
 * @author jarven
 * @since 2026-05-02
 */
public interface ITransferOrderFlowService extends IService<TransferOrderFlowDO> {

    /**
     * 根据日期统计转账金额
     */
    BigDecimal sumTotalAmount(String payFinishDate);

    /**
     * 分页查询转账单流水
     *
     *
     */
    PageResult<TransferOrderFlowPageVO> transferOrderFlowPageQuery(TransferOrderFlowPageQuery query);
}
