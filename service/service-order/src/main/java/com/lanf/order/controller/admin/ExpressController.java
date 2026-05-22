package com.lanf.order.controller.admin;


import com.lanf.constant.model.query.PageQuery;
import com.lanf.constant.model.vo.PageResult;
import com.lanf.constant.result.Result;
import com.lanf.order.model.dto.AddExpressDTO;
import com.lanf.order.model.vo.ExpressPageVO;
import com.lanf.order.service.shipping.IExpressService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-17
 */
@Slf4j
@RestController
@RequestMapping("/express")
public class ExpressController {

    @Autowired
    private IExpressService expressService;

    @PostMapping("/addExpress")
    public Result<Void> addExpress(@Validated @RequestBody AddExpressDTO dto) {

        log.info("添加快递公司");
        expressService.addExpress(dto);
        return Result.ok();
    }

    @GetMapping("/expressPageQuery")
    public Result<PageResult<ExpressPageVO>> expressPageQuery(@Validated PageQuery query) {

        log.info("分页查询快递公司:query{}", query);
        return Result.ok(expressService.expressPageQuery(query));
    }

}

