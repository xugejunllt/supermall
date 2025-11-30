package com.lanf.user.controller.admin;


import com.lanf.common.utils.JsonUtils;
import com.lanf.user.model.dto.CreateUserLevelConfigDTO;
import com.lanf.user.service.benefit.IUserLevelConfigService;
import com.lanf.constant.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 会员等级配置表 前端控制器
 * </p>
 *
 * @author jarven
 * @since 2025-11-20
 */
@Slf4j
@RestController
@RequestMapping("/admin/userLevelConfig")
public class UserLevelConfigController {

    @Autowired
    private IUserLevelConfigService userLevelConfigService;

    @PostMapping("/createUserLevelConfig")
    public Result<Void> createUserLevelConfig(@Validated @RequestBody CreateUserLevelConfigDTO dto) {

        log.info("[{}]开始,入参:[{}]", "添加用户等级配置", JsonUtils.toJsonString(dto));

        userLevelConfigService.createUserLevelConfig(dto);

        log.info("[{}]结束", "添加用户等级配置");

        return Result.ok();
    }
}

