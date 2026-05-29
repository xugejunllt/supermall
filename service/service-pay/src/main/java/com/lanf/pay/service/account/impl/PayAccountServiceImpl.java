package com.lanf.pay.service.account.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.api.pay.model.dto.AddPayAccountDTO;
import com.lanf.api.pay.model.enums.PayChannelEnum;
import com.lanf.api.pay.model.query.PayAccountPageQuery;
import com.lanf.common.utils.BeanCopyUtils;
import com.lanf.constant.exception.BizException;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.constant.utils.UserContext;
import com.lanf.api.pay.model.vo.PayAccountPageVO;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.pay.mapper.PayAccountMapper;
import com.lanf.pay.model.entity.PayAccountDO;
import com.lanf.pay.service.account.IPayAccountService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-21
 */
@Service
public class PayAccountServiceImpl extends ServiceImpl<PayAccountMapper, PayAccountDO> implements IPayAccountService {


    @Override
    public String getByTenantIdAccount(Long tentId, PayChannelEnum accountType) {

        PayAccountDO accountDO = this.lambdaQuery()
                .eq(PayAccountDO::getTenantId, tentId)
                .one();
        if (accountDO == null){
            log.error("支付账户不存在");
            throw new BizException("支付账户不存在");
        }

        return accountDO.getAccount();
    }

    @Override
    public void addPayAccount(AddPayAccountDTO dto) {

        String account = dto.getAccount();

        //其他校验
        PayAccountDO payAccountDO1 = this.lambdaQuery().
                eq(PayAccountDO::getAccountType,dto.getAccountType()).
                one();

        if (payAccountDO1 != null) {
            log.warn("同个支付类型账户只能存在一个");
            throw new BizException("同个支付类型账户只能存在一个");
        }

        PayAccountDO payAccountDO = new PayAccountDO();
        BeanCopyUtils.copy(dto, payAccountDO);

        this.save(payAccountDO);

    }

    @Override
    public PageResult<PayAccountPageVO> payAccountPageQuery(PayAccountPageQuery query) {


        IPage<PayAccountDO> page = new Page<>(query.getPage(), query.getPageSize());
        IPage<PayAccountDO> payAccountPage = this.lambdaQuery().
                eq(PayAccountDO::getTenantId, UserContext.getTenantId()).
                eq(query.getAccountType() != null, PayAccountDO::getAccountType, query.getAccountType()).
                orderByDesc(BaseEntity::getUpdateTime).
                page(page);
        if (payAccountPage.getRecords().isEmpty()){

            return PageResult.emptyResult();
        }

        List<PayAccountPageVO> payAccountPageVOS = BeanCopyUtils.copyBeanList(payAccountPage.getRecords(), PayAccountPageVO.class);
        PageResult<PayAccountPageVO> result = new PageResult<>(payAccountPageVOS);
        result.setTotal(payAccountPage.getTotal());
        result.setSize(payAccountPage.getSize());
        result.setRecords(payAccountPageVOS);

        return result;
    }


}
