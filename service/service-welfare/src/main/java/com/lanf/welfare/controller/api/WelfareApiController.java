package com.lanf.welfare.controller.api;


import com.lanf.web.result.Result;
import com.lanf.welfare.model.dto.ReceiveCouponDTO;
import com.lanf.welfare.model.dto.UseCouponDTO;
import com.lanf.welfare.model.vo.CouponVO;
import com.lanf.welfare.model.vo.UseCouponVO;
import com.lanf.welfare.service.biz.ICouponService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/api")
public class WelfareApiController {

    @Autowired
    private ICouponService couponService;

    @PostMapping("/bathUseCoupon")
    public Result<List<UseCouponVO>> bathUseCoupon(@Validated @RequestBody List<UseCouponDTO> dtoList) {

        log.info("批量使用优惠券:{}:dtoList:",dtoList);

        return Result.ok(couponService.bathUseCoupon(dtoList));
    }
    @PostMapping("/queryByIdSet")
    public Result<List<CouponVO>> queryByIdSet(@Validated @RequestBody Set<Long> idSet) {

        log.info("id批量查询优惠券:{}:idSet:",idSet);

        return Result.ok(couponService.queryByIdSet(idSet));
    }

}
