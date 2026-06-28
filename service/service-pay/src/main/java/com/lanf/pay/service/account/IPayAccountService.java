package com.lanf.pay.service.account;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.api.pay.model.dto.AddPayAccountDTO;
import com.lanf.api.pay.model.enums.PayChannelEnum;
import com.lanf.api.pay.model.query.PayAccountPageQuery;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.api.pay.model.vo.PayAccountPageVO;
import com.lanf.pay.model.entity.PayAccountDO;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-21
 */
public interface IPayAccountService extends IService<PayAccountDO> {



    PayAccountDO getByTenantIdAccount(Long tentId, PayChannelEnum accountType);

    /**
     * 添加支付账户
     *
     *
     */
    void addPayAccount(AddPayAccountDTO dto);

    PageResult<PayAccountPageVO> payAccountPageQuery(PayAccountPageQuery query);



}
