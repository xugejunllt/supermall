package com.lanf.order.controller.app;


import com.lanf.constant.result.Result;
import com.lanf.api.goods.model.dto.ValidateCartDTO;
import com.lanf.order.model.dto.SubmitCartDTO;
import com.lanf.order.model.vo.SubmitCartVO;
import com.lanf.order.model.vo.ValidateCartVO;
import com.lanf.order.service.OrderManagerService;
import com.lanf.order.service.layout.InterfaceLayoutService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 主订单 前端控制器
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-13
 */
@Slf4j
@RestController
@RequestMapping("/app/mainOrder")
public class MainOrderController {

    @Autowired
    private InterfaceLayoutService interfaceLayoutService;
    @Autowired
    private OrderManagerService orderManagerService;



    @PostMapping("/validateCart")
    public Result<ValidateCartVO> validateCart(@Validated @RequestBody ValidateCartDTO dto) {

        log.info("购物车结算,进行校验:dto{}", dto);

        return Result.ok(orderManagerService.validateCart(dto));
    }

    @PostMapping("/submitCart")
    public Result<SubmitCartVO> submitCart(@Validated @RequestBody SubmitCartDTO dto) {

        log.info("购物车结算:dto{}", dto);

        return Result.ok(orderManagerService.submitCart(dto));
    }


}

