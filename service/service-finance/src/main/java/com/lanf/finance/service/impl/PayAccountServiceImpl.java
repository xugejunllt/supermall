package com.lanf.finance.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.common.utils.BeanCopyUtils;
import com.lanf.common.utils.IdUtils;
import com.lanf.finance.mapper.PayAccountMapper;
import com.lanf.finance.model.dto.PayAccountAddDTO;
import com.lanf.finance.model.dto.PayAccountDTO;
import com.lanf.finance.model.entity.PayAccountDO;
import com.lanf.finance.model.query.PayAccountPageQuery;
import com.lanf.finance.model.vo.PayAccountApiVO;
import com.lanf.finance.service.IPayAccountService;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.mybatis.base.PageResult;
import com.lanf.security.utils.UserUtil;
import com.lanf.system.model.bo.SysUserBO;
import com.lanf.web.exception.BizException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

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
                eq(dto.getBusinessId() != null, PayAccountDO::getBusinessId, dto.getBusinessId()).
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
        Integer accountType = dto.getAccountType();
        SysUserBO userInfo = UserUtil.getUserInfo();
        Long businessId = userInfo.getBusinessId();
        /**
         * 远程查询支付宝账户 初期余额是否相等
         */

        //其他校验
        PayAccountDO payAccountDO1 = this.lambdaQuery().
                eq(PayAccountDO::getAccount, account).
                eq(PayAccountDO::getAccountType,dto.getAccountType()).
                one();

        if (payAccountDO1 != null) {
            throw new BizException("同个支付类型账户只能存在一个");
        }
        payAccountDO1 = this.lambdaQuery().
                eq(PayAccountDO::getAccountType, accountType).
                eq(PayAccountDO::getUseTo,dto.getUseTo()).
                one();
        if (payAccountDO1 != null) {
            throw new BizException("同个用途账户只能有一个");
        }


        PayAccountDO payAccountDO = new PayAccountDO();
        BeanCopyUtils.copy(dto, payAccountDO);
        Long id = IdUtils.generateId();
        payAccountDO.setId(id);
        payAccountDO.setRemainMoney(dto.getStartRemainMoney());
        payAccountDO.setBusinessId(businessId);

        this.save(payAccountDO);

    }

    @Override
    public PageResult<PayAccountDO> payAccountPage(PayAccountPageQuery query) {


        IPage<PayAccountDO> page = new Page<>(query.getPage(), query.getPageSize());
        IPage<PayAccountDO> payAccountPage = this.lambdaQuery().
                eq(PayAccountDO::getBusinessId,UserUtil.getBusinessId()).
                eq(query.getAccountType() != null, PayAccountDO::getAccountType, query.getAccountType()).
                orderByDesc(BaseEntity::getId).
                page(page);


        return PageResult.toPageResult(payAccountPage);
    }
}
