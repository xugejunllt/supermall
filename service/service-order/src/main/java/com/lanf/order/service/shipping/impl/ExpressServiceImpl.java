package com.lanf.order.service.shipping.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.common.utils.BeanCopyUtils;
import com.lanf.constant.model.query.PageQuery;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.order.mapper.ExpressMapper;
import com.lanf.order.model.dto.AddExpressDTO;
import com.lanf.order.model.entity.ExpressDO;
import com.lanf.order.model.vo.ExpressPageVO;
import com.lanf.order.service.shipping.IExpressService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-17
 */
@Service
public class ExpressServiceImpl extends ServiceImpl<ExpressMapper, ExpressDO> implements IExpressService {

    @Override
    public void addExpress(AddExpressDTO dto) {

        ExpressDO expressDO = new ExpressDO();
        BeanCopyUtils.copy(dto, expressDO);

        this.save(expressDO);

    }

    @Override
    public PageResult<ExpressPageVO> expressPageQuery(PageQuery query) {

        IPage<ExpressDO> page = new Page<>(query.getPage(), query.getPageSize());
        IPage<ExpressDO> result = this.lambdaQuery()
                .orderByDesc(BaseEntity::getUpdateTime)
                .page(page);

        if (result.getRecords().isEmpty()) {
            return PageResult.emptyResult();
        }

        PageResult<ExpressPageVO> resultVo = new PageResult<>();
        resultVo.setTotal(result.getTotal());
        resultVo.setSize(result.getSize());
        resultVo.setRecords(BeanCopyUtils.copyBeanList(result.getRecords(), ExpressPageVO.class));

        return resultVo;
    }

}
