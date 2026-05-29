package com.lanf.finance.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.common.utils.BeanCopyUtils;
import com.lanf.common.utils.BigDecimalUtil;
import com.lanf.constant.exception.BizException;
import com.lanf.constant.model.query.PageQuery;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.finance.mapper.MoneyFlowMapper;
import com.lanf.finance.model.bo.AddMoneyFlow;
import com.lanf.finance.model.entity.MoneyFlowDO;
import com.lanf.finance.model.entity.PayAccountDO;
import com.lanf.finance.model.enums.RecordTypeEnum;
import com.lanf.finance.model.query.SumIncomeMoneyQuery;
import com.lanf.finance.model.vo.MoneyFlowPageVO;
import com.lanf.finance.model.vo.SumIncomeMoneyVO;
import com.lanf.finance.service.ClearingDetailService;
import com.lanf.finance.service.IMoneyFlowService;
import com.lanf.finance.service.IPayAccountService;
import com.lanf.mybatis.base.BaseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * 资金流水 服务实现类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-22
 */
@Service
public class MoneyFlowServiceImpl extends ServiceImpl<MoneyFlowMapper, MoneyFlowDO> implements IMoneyFlowService {


    @Autowired
    private IPayAccountService payAccountService;


    @Autowired
    private MoneyFlowMapper moneyFlowMapper;

    @Autowired
    private ClearingDetailService liquidationFlowService;


    @Override
    public void addMoneyFlow(AddMoneyFlow addMoneyFlow) {
        Long businessId = addMoneyFlow.getTenantId();
        PayAccountDO payAccountDO = payAccountService
                .lambdaQuery().
                eq(PayAccountDO::getTenantId, businessId).one();

        if (payAccountDO == null) {
            log.error("收支账户不存在");
            return;
        }

        BigDecimal afterRemainMoney = calculateAfterRemainMoney(addMoneyFlow.getRecordType(),
                addMoneyFlow.getIncomeMoney(), payAccountDO.getRemainMoney());
        MoneyFlowDO moneyFlowDO = buildMoneyFlowDO(addMoneyFlow, payAccountDO, afterRemainMoney);

        try {
            this.save(moneyFlowDO);
        } catch (DuplicateKeyException e) {
            log.warn("资金流水已存在");

        }
    }


    private static MoneyFlowDO buildMoneyFlowDO(AddMoneyFlow addMoneyFlow, PayAccountDO payAccountDO, BigDecimal afterRemainMoney) {
        MoneyFlowDO moneyFlowDO = new MoneyFlowDO();
        // 生成流水号
        moneyFlowDO.setFlowNo(addMoneyFlow.getFlowNo());
        // 设置其他字段
        moneyFlowDO.setTenantId(addMoneyFlow.getTenantId());
        moneyFlowDO.setBizOrderId(addMoneyFlow.getBizOrderId());
        moneyFlowDO.setRecordType(addMoneyFlow.getRecordType());
        moneyFlowDO.setIncomeAccount(payAccountDO.getAccount());
        moneyFlowDO.setBeforeRemainMoney(payAccountDO.getRemainMoney());
        moneyFlowDO.setAfterRemainMoney(afterRemainMoney);
        moneyFlowDO.setChangeMoney(addMoneyFlow.getIncomeMoney());
        return moneyFlowDO;
    }

    /**
     * 生成资金流水号
     * 格式: 业务订单ID + 记录类型code
     * 例如: 123456789_0 (订单ID为123456789, 类型为下单)
     * 对于售后单 部分退款,一笔售后单 一笔退款
     */
    public static String generateFlowNo(Long bizOrderId, Integer recordTypeCode) {
        if (bizOrderId == null || recordTypeCode == null) {
            throw new IllegalArgumentException("业务订单ID和记录类型不能为空");
        }
        return bizOrderId + "_" + recordTypeCode;
    }

    private BigDecimal calculateAfterRemainMoney(RecordTypeEnum recordType, BigDecimal incomeMoney, BigDecimal beforeRemainMoney) {
        Integer code = recordType.getCode();

        if (RecordTypeEnum.INCOME_TYPE_SET.contains(code)) {
            return BigDecimalUtil.add(beforeRemainMoney, incomeMoney);
        } else if (RecordTypeEnum.EXPENSE_TYPE_SET.contains(code)) {
            return BigDecimalUtil.subtract(beforeRemainMoney, incomeMoney);
        } else {
            log.error("未知的记录类型");
            throw new BizException("未知的记录类型:" + code);
        }
    }

    @Override
    public PageResult<MoneyFlowPageVO> moneyFlowPageQuery(PageQuery query) {

        IPage<MoneyFlowDO> page = new Page<>(query.getPage(), query.getPageSize());
        IPage<MoneyFlowDO> payAccountPage = this.lambdaQuery().
                orderByDesc(BaseEntity::getUpdateTime).
                page(page);
        if (payAccountPage.getRecords().isEmpty()) {

            return PageResult.emptyResult();
        }

        List<MoneyFlowPageVO> payAccountPageVOS = BeanCopyUtils.copyBeanList(payAccountPage.getRecords(), MoneyFlowPageVO.class);
        PageResult<MoneyFlowPageVO> result = new PageResult<>(payAccountPageVOS);
        result.setTotal(payAccountPage.getTotal());
        result.setSize(payAccountPage.getSize());
        result.setRecords(payAccountPageVOS);

        return result;
    }

    @Override
    public SumIncomeMoneyVO sumIncomeMoneyQuery(SumIncomeMoneyQuery query) {


   this.lambdaQuery()
                .ge(query.getStartTime() != null, MoneyFlowDO::getCreateTime, query.getStartTime())
                .le(query.getEndTime() != null, MoneyFlowDO::getCreateTime, query.getEndTime());



        SumIncomeMoneyVO vo = new SumIncomeMoneyVO();

        return vo;
    }
}
