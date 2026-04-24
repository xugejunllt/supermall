package com.lanf.aftersales.controller.admin;


import com.lanf.aftersales.model.dto.*;
import com.lanf.aftersales.model.query.AfterSalesOrderPageQuery;
import com.lanf.aftersales.model.vo.AfterSalesOrderPageVO;
import com.lanf.aftersales.service.IAfterSalesOrderService;
import com.lanf.aftersales.service.layout.InterfaceLayoutService;
import com.lanf.mybatis.base.PageResult;
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
@RequestMapping("/admin/afterSalesOrder")
public class AfterSalesOrderAdminController {

    @Autowired
    private InterfaceLayoutService interfaceLayoutService;
    @Autowired
    private IAfterSalesOrderService afterSalesOrderService;


    @GetMapping("/afterSalesOrderPageQuery")
    public Result<PageResult<AfterSalesOrderPageVO>> afterSalesOrderPageQuery(@Validated AfterSalesOrderPageQuery query) {

        log.info("分页查询售后单:query{}", query);

        return Result.ok(afterSalesOrderService.afterSalesOrderPageQuery(query));
    }

    @GetMapping("/afterSalesOrderDetail")
    public Result<AfterSalesOrderPageVO> afterSalesOrderDetail(@Validated Long id) {

        log.info("查询售后单明细:id:{}", id);

        return Result.ok(afterSalesOrderService.afterSalesOrderDetail(id));
    }


    @PostMapping("/businessAgree")
    public Result<Void> businessAgree(@RequestBody BusinessAgreeDTO dto) {

        log.info("商家同意退货退款申请:dto:{}", dto);
        afterSalesOrderService.businessAgree(dto);
        return Result.ok();
    }


    /**
     *
     * 签收退货商品
     *
     */

    @PostMapping("/businessReceiver")
    public Result<Void> businessReceiver(@RequestBody BusinessReceiverDTO dto) {

        log.info("商家收货:dto:{}", dto);
        interfaceLayoutService.businessReceiver(dto);
        return Result.ok();
    }

}

