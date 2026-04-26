package com.lanf.order.controller.app;


import com.lanf.common.utils.JsonUtils;
import com.lanf.constant.enums.CancelSourceEnum;
import com.lanf.constant.result.Result;
import com.lanf.mybatis.base.PageResult;
import com.lanf.order.model.dto.CalculateOrderAmountDTO;
import com.lanf.order.model.dto.CancelOrderDTO;
import com.lanf.order.model.dto.PlaceOrderDTO;
import com.lanf.order.model.dto.SignForDTO;
import com.lanf.order.model.query.OrderPageQuery;
import com.lanf.order.model.vo.CalculateOrderAmountVO;
import com.lanf.order.model.vo.OrderDetailVO;
import com.lanf.order.model.vo.OrderPageVO;
import com.lanf.order.model.vo.PlaceOrderVO;
import com.lanf.order.service.IOrderService;
import com.lanf.order.service.OrderManagerService;
import com.lanf.order.service.layout.InterfaceLayoutService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 订单表 前端控制器
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-13
 */
@Slf4j
@RestController
@RequestMapping("/app/order")
public class OrderController {

    @Autowired
    private IOrderService orderService;
    @Autowired
    private InterfaceLayoutService interfaceLayoutService;


    @Autowired
    private OrderManagerService orderManagerService;


    /**
     * 下单前计算订单金额
     *
     *
     */
    @PostMapping("/calculateOrderAmount")
    public Result<CalculateOrderAmountVO> calculateOrderAmount(@RequestBody @Validated CalculateOrderAmountDTO dto) {


        log.info("下单前计算订单金额[{}]", dto);

        return Result.ok(orderManagerService.calculateOrderAmount(dto));

    }

    /**
     * 立即下单接口
     *
     * @param orderDTO 下单请求参数
     * @return 下单结果
     */
    @PostMapping("/placeOrder")
    public Result<PlaceOrderVO> placeOrder(@RequestBody @Validated PlaceOrderDTO orderDTO) {


        log.info("立即下单[{}]", orderDTO);

        return Result.ok(orderManagerService.placeOrder(orderDTO));

    }
    @PostMapping("/cancelOrder")
    public Result<PlaceOrderVO> cancelOrder(@RequestBody @Validated CancelOrderDTO dto) {


        log.info("取消订单[{}]", JsonUtils.toJsonString(dto));
        dto.setCancelSource(CancelSourceEnum.USER_MANUAL.getCode());
        orderManagerService.cancelOrder(dto);
        return Result.ok();

    }


    @PostMapping("/signFor")
    public Result signFor(@Validated @RequestBody SignForDTO dto) {

        log.info("签收:{}dto", dto);
        orderService.signFor(dto);
        return Result.ok();
    }


    @GetMapping("/orderPage")
    public Result<PageResult<OrderPageVO>> orderPage(@Validated OrderPageQuery query) {

        log.info("取消订单:query{}", query);

        return Result.ok(orderService.orderPage(query));
    }

    @GetMapping("/orderDetail")
    public Result<OrderDetailVO> orderDetail(Long id) {

        log.info("查询订单详细:id{}", id);

        return Result.ok(orderService.orderDetail(id));
    }




}

