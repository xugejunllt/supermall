package com.lanf.order.controller.app;


import com.lanf.constant.model.query.PageQuery;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.constant.result.Result;
import com.lanf.order.model.dto.AddAfterSalesOrderDTO;
import com.lanf.order.model.dto.UserDeliveryDTO;
import com.lanf.api.order.model.vo.AfterSalesOrderForUserDetailVO;
import com.lanf.api.order.model.vo.AfterSalesOrderForUserPageVO;
import com.lanf.order.service.aftersales.IAfterSalesOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 售后单 前端控制器
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-19
 */
@Slf4j
@RestController
@RequestMapping("/app/afterSalesOrder")
public class AfterSalesOrderAppController {

    @Autowired
    private IAfterSalesOrderService afterSalesOrderService;


    @PostMapping("/addAfterSalesOrder")
    public Result<Void> addAfterSalesOrder(@Validated @RequestBody AddAfterSalesOrderDTO dto) {

        log.info("创建售后单:dto{}", dto);
        afterSalesOrderService.addAfterSalesOrder(dto);

        return Result.ok();
    }

    @GetMapping("/afterSalesOrderForUserPageQuery")
    public Result<PageResult<AfterSalesOrderForUserPageVO>> afterSalesOrderForUserPageQuery(@Validated PageQuery query) {

        log.info("分页查询用户售后单:query{}", query);

        return Result.ok(afterSalesOrderService.afterSalesOrderForUserPageQuery(query));
    }

    @GetMapping("/afterSalesOrderForUserDetailQuery")
    public Result<AfterSalesOrderForUserDetailVO> afterSalesOrderForUserDetailQuery(Long id) {

        log.info("查询用户售后单详细:query{}", id);

        return Result.ok(afterSalesOrderService.afterSalesOrderForUserDetailQuery(id));
    }



    @PostMapping("/userDelivery")
    public Result<Void> userDelivery(@RequestBody @Validated UserDeliveryDTO dto) {

        log.info("用户发货:dto{}", dto);
        afterSalesOrderService.userDelivery(dto);
        return Result.ok();
    }


}

