package com.lanf.user.service.benefit;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.mybatis.base.PageResult;
import com.lanf.user.model.dto.CreateBenefitDTO;
import com.lanf.user.model.entity.BenefitDO;
import com.lanf.user.model.query.BenefitPageQuery;

import java.util.List;

/**
 * <p>
 * 权益表
 服务类
 * </p>
 *
 * @author jarven
 * @since 2025-11-19
 */
public interface IBenefitService extends IService<BenefitDO> {

    void createBenefit(CreateBenefitDTO dto);

    /**
     * 使用权益
     *
     */
    void  useBenefit(Long id);

    /**
     * 禁用权益
     *
     */
    void disableBenefit(Long id);

    /**
     * 获取可以使用权益列表code
     * @return
     */
    List<String> listUseBenefitCode();


    PageResult<BenefitDO> pageBenefit(BenefitPageQuery query);


}
