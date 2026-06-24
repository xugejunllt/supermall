package com.lanf.dynamicsrrefresh.service.conftroller;

import com.lanf.dynamicsrrefresh.core.model.enums.BizCodeEnum;
import com.lanf.dynamicsrrefresh.core.store.StoreService;
import com.lanf.dynamicsrrefresh.service.model.dto.PublishConfigDTO;
import com.lanf.dynamicsrrefresh.service.model.vo.ConfigVO;
import com.lanf.dynamicsrrefresh.service.result.PageResult;
import com.lanf.dynamicsrrefresh.service.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/pushConfig")
public class PushConfigController {


    @Autowired
    private StoreService storeService;



    @PostMapping("/publishConfig")
    public Result publishConfig(@RequestBody PublishConfigDTO dto) {

        log.info("发布配置:dto:{}", dto);
        BizCodeEnum bizCodeEnum = BizCodeEnum.getByCode(dto.getBizCode());

        return Result.ok(storeService.publishConfig(bizCodeEnum.getDateId(), bizCodeEnum.getGroup(), dto.getContent()));
    }

    @GetMapping("/getConfig")
    public Result<PageResult<ConfigVO>> getConfig(@RequestParam("bizCode") Integer bizCode) {

        log.info("获取配置:bizCode:{}", bizCode);
        BizCodeEnum bizCodeEnum = BizCodeEnum.getByCode(bizCode);
        List<String> contentList = storeService.contentList(bizCodeEnum.getDateId(), bizCodeEnum.getGroup());
        List<ConfigVO> configVO = contentList.stream().map(ConfigVO::new).collect(Collectors.toList());
        PageResult<ConfigVO> pageResult = new PageResult<>();
        pageResult.setTotal(0);
        pageResult.setRecords(configVO);

        return Result.ok(pageResult);
    }

    @PostMapping("/deleteContent")
    public Result deleteContent(@RequestBody PublishConfigDTO dto) {

        log.info("删除配置:dto:{}", dto);

        BizCodeEnum bizCodeEnum = BizCodeEnum.getByCode(dto.getBizCode());

        return Result.ok(storeService.deleteContent(bizCodeEnum.getDateId(), bizCodeEnum.getGroup(), dto.getContent()));
    }
//    @GetMapping("/test")
//    public Result<PageResult<ConfigVO>> test(Integer bizCode) {
//
//        log.info("获取配置:bizCode:{}", bizCode);
//        Result<PageResult<ConfigVO>> config = systemLogService.getConfig(bizCode);
//
//        return Result.ok(config.getData());
//    }
}
