package com.lanf.user.service.benefit;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.user.model.dto.AddBenefitDTO;
import com.lanf.user.model.dto.DisableBenefitDTO;
import com.lanf.user.model.dto.UseBenefitDTO;
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

    void addBenefit(AddBenefitDTO dto);

    /**
     * 使用权益
     *
     */
    void  useBenefit(UseBenefitDTO dto);

    /**
     * 禁用权益
     *
     */
    void disableBenefit(DisableBenefitDTO dto);

    /**
     * 获取可以使用权益列表code
     * @return
     */
    List<String> listUseBenefitCode();


    PageResult<BenefitDO> benefitPageQuery(BenefitPageQuery query);


}
