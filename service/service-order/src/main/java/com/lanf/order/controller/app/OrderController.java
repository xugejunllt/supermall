package com.lanf.order.controller.app;


import com.lanf.api.goods.model.dto.ValidateCartDTO;
import com.lanf.api.order.model.query.OrderDetailQuery;
import com.lanf.api.order.model.vo.OrderDetailForAdminVO;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.constant.result.Result;
import com.lanf.constant.utils.IdUtils;
import com.lanf.constant.utils.UserContext;
import com.lanf.order.model.dto.*;
import com.lanf.order.model.query.AppOrderSearchQuery;
import com.lanf.order.model.query.OrderPageQuery;
import com.lanf.order.model.vo.*;
import com.lanf.order.service.OrderManagerService;
import com.lanf.order.service.order.IOrderService;
import com.lanf.order.service.shipping.IShippingTrackService;
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
    private OrderManagerService orderManagerService;
    @Autowired
    private IShippingTrackService shippingTrackService;


    /**
     * 生成订单编号
     *
     */
    @PostMapping("/generateOrderNumber")
    public Result<String> generateOrderNumber() {

        log.info("生成订单编号");
        return Result.ok(IdUtils.generateId()+"");
    }
    /**
     * 下单前计算订单金额
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

    @PostMapping("/cancelOrder")
    public Result<PlaceOrderVO> cancelOrder(@RequestBody @Validated CancelOrderDTO dto) {
        log.info("取消订单:{}", dto);
        dto.setUserId(UserContext.getUserId());
        orderManagerService.cancelOrder(dto);
        return Result.ok();

    }


    @PostMapping("/signFor")
    public Result<Void> signFor(@Validated @RequestBody SignForDTO dto) {

        log.info("签收订单:{}", dto);
        orderService.signFor(dto);
        return Result.ok();
    }

    @PostMapping("/orderSearchQuery")
    public Result<PageResult<OrderListVO>> orderSearchQuery(@Validated @RequestBody AppOrderSearchQuery query) {

        log.info("搜索订单列表:{}dto", query);

        return Result.ok(orderService.orderSearchQuery(query));
    }
    @PostMapping("/orderPageQuery")
    public Result<PageResult<OrderPageVO>> orderPageQuery(@Validated @RequestBody OrderPageQuery query) {

        log.info("分页查询订单列表:{}dto", query);

        return Result.ok(orderService.orderPageQuery(query));
    }
    @GetMapping("/orderDetailQuery")
    public Result<OrderDetailForAdminVO> orderDetailQuery(@Validated OrderDetailQuery query) {

        log.info("查询订单详细:{}", query);

        return Result.ok(orderService.orderDetailForAdminQuery(query));
    }

}

