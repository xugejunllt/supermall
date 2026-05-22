package com.lanf.order.controller.app;


import com.lanf.constant.result.Result;
import com.lanf.order.model.dto.AddAfterSalesOrderDTO;
import com.lanf.order.service.aftersales.IAfterSalesOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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




}

