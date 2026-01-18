package com.lanf.welfare.api;


import com.lanf.constant.result.Result;
import com.lanf.welfare.model.dto.CalculateDiscountAmountDTO;
import com.lanf.welfare.model.dto.UseCouponDTO;
import com.lanf.welfare.model.dto.UseMultipleCouponDTO;
import com.lanf.welfare.model.vo.CalculateDiscountAmountVO;
import com.lanf.welfare.model.vo.CouponVO;
import com.lanf.welfare.model.vo.UseCouponVO;
import org.dromara.hmily.annotation.Hmily;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Set;

@Component
@FeignClient(name = "service-welfare",url = "localhost:9015") //调用的服务名称
public interface WelfareApiService {

    /**
     * 优惠券计算
     *
     *
     */
    @PostMapping("/welfare/api/calculateDiscountAmount")
    public Result<CalculateDiscountAmountVO> calculateDiscountAmount(@RequestBody
                                                                         CalculateDiscountAmountDTO dto);
    /**
     * 使用多张优惠卷
     *
     *
     */
    @Hmily
    @PostMapping("/welfare/api/useMultipleCoupon")
    public Result<CalculateDiscountAmountVO> useMultipleCoupon(@RequestBody
                                                               UseMultipleCouponDTO dto);


    @PostMapping("/welfare/api/bathUseCoupon")
    public Result<List<UseCouponVO>> bathUseCoupon(@Validated @RequestBody List<UseCouponDTO> dtoList);

    @PostMapping("/welfare/api/queryByIdSet")
    public Result<List<CouponVO>> queryByIdSet(@Validated @RequestBody Set<Long> idSet);
}
