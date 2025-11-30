package com.lanf.finance.controller.admin;

import com.lanf.finance.model.entity.ContrastBillDO;
import com.lanf.finance.model.query.ContrastBillPageQuery;
import com.lanf.finance.model.vo.ContrastBillTrackVO;
import com.lanf.finance.service.IContrastBillService;
import com.lanf.mybatis.base.PageResult;
import com.lanf.constant.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin/contrastBill")
public class ContrastBillController {

    @Autowired
    private IContrastBillService contrastBillService;

    @GetMapping("/contrastBillPage")
    public Result<PageResult<ContrastBillDO>> contrastBillPage(ContrastBillPageQuery query) {

        log.info("分页查询对账单:{}", query);

        return Result.ok(contrastBillService.contrastBillPage(query));
    }
    @GetMapping("/contrastBillDetail")
    public Result<List<ContrastBillTrackVO>> contrastBillDetail(Long  id) {

        log.info("查询对账单明细:{}", id);

        return Result.ok(contrastBillService.contrastBillDetail(id));
    }
}
