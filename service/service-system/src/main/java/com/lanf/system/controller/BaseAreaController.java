package com.lanf.system.controller;


import com.lanf.system.model.vo.BaseAreaVO;
import com.lanf.system.service.IBaseAreaService;
import com.lanf.constant.result.Result;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-05-30
 */
@Slf4j
@RestController
@RequestMapping("/baseArea")
public class BaseAreaController {

    @Autowired
    private IBaseAreaService baseAreaService;

    @ApiOperation(value = "查询地域树")
    @GetMapping("/treeAreaList")
    public Result<List<BaseAreaVO>> treeAreaList( ) {
        log.info("查询地域树");
        return Result.ok(baseAreaService.areaTree());
    }

}

