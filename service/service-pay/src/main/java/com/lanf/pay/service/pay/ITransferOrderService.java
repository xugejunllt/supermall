package com.lanf.pay.service.pay;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.api.pay.model.query.TransferOrderPageQuery;
import com.lanf.api.pay.model.vo.TransferOrderPageVO;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.pay.model.entity.TransferOrderDO;

/**
 * <p>
 * 转账单 服务类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-08-03
 */
public interface ITransferOrderService extends IService<TransferOrderDO> {

    /**
     * 分页查询转账单
     *
     *
     */
    PageResult<TransferOrderPageVO> transferOrderPageQuery(TransferOrderPageQuery query);

}
