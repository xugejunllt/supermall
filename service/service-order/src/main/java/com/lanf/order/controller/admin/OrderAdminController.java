package com.lanf.order.controller.admin;


import com.lanf.constant.model.vo.PageResult;
import com.lanf.constant.result.Result;
import com.lanf.order.model.dto.AllowOutboundDTO;
import com.lanf.order.model.dto.DeliveryDTO;
import com.lanf.order.model.query.AdminOrderSearchQuery;
import com.lanf.order.model.vo.AdminOrderListVO;
import com.lanf.order.service.IOrderService;
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
@RequestMapping("/admin/order")
public class OrderAdminController {

    @Autowired
    private IOrderService orderService;
    @Autowired
    private InterfaceLayoutService interfaceLayoutService;

    @PostMapping("/allowOutbound")
    public Result<Void> allowOutbound(@Validated @RequestBody AllowOutboundDTO dto) {

        log.info("允许发货:{}dto", dto);
        orderService.allowOutbound(dto);
        return Result.ok();
    }

    @PostMapping("/delivery")
    public Result delivery(@Validated @RequestBody DeliveryDTO dto) {

        log.info("进行发货:{}dto", dto);
        interfaceLayoutService.delivery(dto);
        return Result.ok();
    }

    @PostMapping("/orderSearchQuery")
    public Result<PageResult<AdminOrderListVO>> orderSearchQuery(@Validated @RequestBody AdminOrderSearchQuery query) {

        log.info("分页查询订单列表:{}", query);

        return Result.ok(orderService.orderSearchQuery(query));
    }




}

