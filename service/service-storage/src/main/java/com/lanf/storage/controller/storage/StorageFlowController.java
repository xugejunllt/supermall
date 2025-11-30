package com.lanf.storage.controller.storage;


import com.lanf.mybatis.base.PageResult;
import com.lanf.storage.model.entity.StorageFlowDO;
import com.lanf.storage.model.query.StorageFlowPageQuery;
import com.lanf.storage.service.storage.IStorageFlowService;
import com.lanf.constant.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 入库明细 前端控制器
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-07
 */
@Slf4j
@RestController
@RequestMapping("/storageFlow")
public class StorageFlowController {

    @Autowired
    private IStorageFlowService storageDetailsService;
    @GetMapping("/storageFlowPage")
    public Result<PageResult<StorageFlowDO>> storageFlowPage(StorageFlowPageQuery query) {
        log.info("分页查入库明细列表:query{}", query);
        return Result.ok(storageDetailsService.storageFlowPage(query));
    }
}

