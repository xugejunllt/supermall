package com.lanf.order.controller.admin;


import com.lanf.constant.model.query.PageQuery;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.constant.result.Result;
import com.lanf.order.model.dto.BusinessAgreeDTO;
import com.lanf.order.model.dto.BusinessReceiverDTO;
import com.lanf.order.model.dto.CompleteRefundDTO;
import com.lanf.order.model.vo.AfterSalesOrderForUserDetailVO;
import com.lanf.order.model.vo.AfterSalesOrderForUserPageVO;
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
@RequestMapping("/admin/afterSalesOrder")
public class AfterSalesOrderForAdminController {

    @Autowired
    private IAfterSalesOrderService afterSalesOrderService;


    @GetMapping("/afterSalesOrderForUserPageQuery")
    public Result<PageResult<AfterSalesOrderForUserPageVO>> afterSalesOrderForUserPageQuery(@Validated PageQuery query) {

        log.info("分页查询售后单:query{}", query);

        return Result.ok(afterSalesOrderService.afterSalesOrderForUserPageQuery(query));
    }

    @GetMapping("/afterSalesOrderForUserDetailQuery")
    public Result<AfterSalesOrderForUserDetailVO> afterSalesOrderForUserDetailQuery(Long id) {

        log.info("售后单详细:query{}", id);

        return Result.ok(afterSalesOrderService.afterSalesOrderForUserDetailQuery(id));
    }

    @PostMapping("/businessAgree")
    public Result<Void> businessAgree(@RequestBody @Validated BusinessAgreeDTO dto) {

        log.info("商家同意:dto{}", dto);
        afterSalesOrderService.businessAgree(dto);
        return Result.ok();
    }

    @PostMapping("/businessReceiver")
    public Result<Void> businessReceiver(@RequestBody @Validated BusinessReceiverDTO dto) {

        log.info("商家签收商品:dto{}", dto);

        afterSalesOrderService.businessReceiver(dto);

        return Result.ok();
    }

    @PostMapping("/completeRefund")
    public Result<Void> completeRefund(@RequestBody @Validated CompleteRefundDTO dto) {

        log.info("发起退款:dto{}", dto);

        afterSalesOrderService.completeRefund(dto);

        return Result.ok();
    }

}

