package com.lanf.system.controller.order;

import com.lanf.api.order.api.OrderApiService;
import com.lanf.api.order.model.dto.AddExpressDTO;
import com.lanf.api.order.model.dto.AllowOutboundDTO;
import com.lanf.api.order.model.dto.DeliveryDTO;
import com.lanf.api.order.model.query.OrderDetailQuery;
import com.lanf.api.order.model.vo.ExpressPageVO;
import com.lanf.api.order.model.vo.OrderDetailForAdminVO;
import com.lanf.constant.model.query.PageQuery;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.constant.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/order")
public class OrderAdminController {

    @Autowired
    private OrderApiService orderApiService;

    @PostMapping("/allowOutbound")
    public Result<Void> allowOutbound(@Validated @RequestBody AllowOutboundDTO dto) {
        log.info("允许发货:{}dto", dto);
        return orderApiService.allowOutbound(dto);
    }

    @PostMapping("/delivery")
    public Result<Void> delivery(@Validated @RequestBody DeliveryDTO dto) {
        log.info("进行发货:{}dto", dto);
        return orderApiService.delivery(dto);
    }

    @GetMapping("/orderDetailForAdminQuery")
    public Result<OrderDetailForAdminVO> orderDetailForAdminQuery(@Validated OrderDetailQuery query) {
        log.info("admin查询订单详细:{}", query);
        return orderApiService.orderDetailForAdminQuery(query);
    }
    @PostMapping("/addExpress")
    public Result<Void> addExpress(@Validated @RequestBody AddExpressDTO dto) {
        log.info("添加快递公司:{}", dto);
        orderApiService.addExpress(dto);
        return Result.ok();
    }

    @GetMapping("/expressPageQuery")
    public Result<PageResult<ExpressPageVO>> expressPageQuery(@Validated PageQuery query){

        log.info("分页查询快递公司");
        return orderApiService.expressPageQuery(query);
    }

}
