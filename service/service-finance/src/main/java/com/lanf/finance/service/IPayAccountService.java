package com.lanf.finance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.finance.model.dto.PayAccountAddDTO;
import com.lanf.finance.model.dto.PayAccountDTO;
import com.lanf.finance.model.entity.PayAccountDO;
import com.lanf.finance.model.query.PayAccountPageQuery;
import com.lanf.finance.model.vo.PayAccountApiVO;
import com.lanf.mybatis.base.PageResult;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-21
 */
public interface IPayAccountService extends IService<PayAccountDO> {


    PayAccountApiVO payAccountQuery(PayAccountDTO dto);

    /**
     * 添加支付账户
     *
     *
     */
    void payAccountAdd(PayAccountAddDTO dto);

    PageResult<PayAccountDO> payAccountPage(PayAccountPageQuery query);
}
