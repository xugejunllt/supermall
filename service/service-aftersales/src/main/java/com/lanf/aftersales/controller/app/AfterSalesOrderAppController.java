package com.lanf.aftersales.controller.app;


import com.lanf.aftersales.model.dto.AfterSalesOrderAddDTO;
import com.lanf.aftersales.model.dto.UserDeliveryDTO;
import com.lanf.aftersales.model.query.AfterSalesOrderPageQuery;
import com.lanf.aftersales.model.vo.AfterSalesOrderPageVO;
import com.lanf.aftersales.service.IAfterSalesOrderService;
import com.lanf.aftersales.service.layout.InterfaceLayoutService;
import com.lanf.constant.web.PageResult;
import com.lanf.constant.result.Result;
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
    private InterfaceLayoutService interfaceLayoutService;
    @Autowired
    private IAfterSalesOrderService afterSalesOrderService;


    @PostMapping("/afterSalesOrderAdd")
    public Result afterSalesOrderAdd(@Validated @RequestBody AfterSalesOrderAddDTO dto) {

        log.info("创建售后单:dto{}", dto);
        interfaceLayoutService.afterSalesOrderAdd(dto);

        return Result.ok();
    }

    @GetMapping("/afterSalesOrderPageQuery")
    public Result<PageResult<AfterSalesOrderPageVO>> afterSalesOrderPageQuery(@Validated AfterSalesOrderPageQuery query) {

        log.info("分页查询售后单:query{}", query);
        query.setUserId(UserUtils.getUserId());
        return Result.ok(afterSalesOrderService.afterSalesOrderPageQuery(query));
    }

    @GetMapping("/afterSalesOrderDetail")
    public Result<AfterSalesOrderPageVO> afterSalesOrderDetail(@Validated Long id) {

        log.info("查询售后单明细:id:{}", id);

        return Result.ok(afterSalesOrderService.afterSalesOrderDetail(id));
    }
    @PostMapping("/userDelivery")
    public Result<Void> userDelivery(@RequestBody UserDeliveryDTO dto) {

        log.info("用户发货:dto{}", dto);
        afterSalesOrderService.userDelivery(dto);
        return Result.ok();
    }


}

