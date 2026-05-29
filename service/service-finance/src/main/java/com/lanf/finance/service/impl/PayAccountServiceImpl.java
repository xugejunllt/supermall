package com.lanf.finance.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.common.utils.BeanCopyUtils;
import com.lanf.constant.exception.BizException;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.constant.utils.UserContext;
import com.lanf.finance.mapper.PayAccountMapper;
import com.lanf.finance.model.dto.PayAccountAddDTO;
import com.lanf.finance.model.dto.PayAccountDTO;
import com.lanf.finance.model.entity.PayAccountDO;
import com.lanf.finance.model.query.PayAccountPageQuery;
import com.lanf.finance.model.vo.PayAccountApiVO;
import com.lanf.finance.model.vo.PayAccountPageVO;
import com.lanf.finance.service.IPayAccountService;
import com.lanf.mybatis.base.BaseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public PayAccountApiVO payAccountQuery(PayAccountDTO dto) {

        PayAccountDO payAccountDO = this.lambdaQuery().
                eq(dto.getBusinessId() != null, PayAccountDO::getTenantId, dto.getBusinessId()).
                eq(PayAccountDO::getAccountType, dto.getAccountType()).one();

        if (payAccountDO == null) {
            throw new BizException("支付账户不存在");
        }
        PayAccountApiVO vo = new PayAccountApiVO();
        BeanCopyUtils.copy(payAccountDO, vo);

        return vo;
    }

    @Transactional
    @Override
    public void payAccountAdd(PayAccountAddDTO dto) {

        String account = dto.getAccount();
        Long businessId = UserContext.getTenantId();

        //其他校验
        PayAccountDO payAccountDO1 = this.lambdaQuery().
                eq(PayAccountDO::getAccount, account).
                eq(PayAccountDO::getAccountType,dto.getAccountType()).
                one();

        if (payAccountDO1 != null) {
            log.error("同个支付类型账户只能存在一个");
            throw new BizException("同个支付类型账户只能存在一个");
        }

        PayAccountDO payAccountDO = new PayAccountDO();
        BeanCopyUtils.copy(dto, payAccountDO);
        payAccountDO.setRemainMoney(dto.getStartRemainMoney());
        payAccountDO.setTenantId(businessId);

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

    @Override
    public PayAccountDO getByMerchantIdAccount(Long merchantId, Integer accountType) {


        return this.lambdaQuery()
                .eq(PayAccountDO::getTenantId, merchantId)
                .eq(PayAccountDO::getAccountType, accountType)
                .one();
    }
}
