package com.lanf.order.controller.app;


import com.lanf.order.model.dto.OnePlaceAnOrderDTO;
import com.lanf.order.model.dto.SubmitOrderDTO;
import com.lanf.order.model.vo.CreateOrderVO;
import com.lanf.order.service.layout.InterfaceLayoutService;
import com.lanf.constant.result.Result;
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

    @PostMapping("/submitOrder")
    public Result<CreateOrderVO> submitOrder(@Validated @RequestBody SubmitOrderDTO dto) {

        log.info("提交订单:dto{}", dto);

        return Result.ok(interfaceLayoutService.submitOrderDTO(dto));
    }

    @PostMapping("/onePlaceAnOrder")
    public Result<CreateOrderVO> onePlaceAnOrder(@Validated @RequestBody OnePlaceAnOrderDTO dto) {

        log.info("单笔下单:dto{}", dto);

        return Result.ok(interfaceLayoutService.onePlaceAnOrder(dto));
    }

}

