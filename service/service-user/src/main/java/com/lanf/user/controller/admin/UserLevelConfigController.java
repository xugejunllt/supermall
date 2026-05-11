package com.lanf.user.controller.admin;


import com.lanf.constant.result.Result;
import com.lanf.user.model.dto.AddUserLevelConfigDTO;
import com.lanf.user.service.benefit.IUserLevelConfigService;
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

    @PostMapping("/addUserLevelConfig")
    public Result<Void> addUserLevelConfig(@Validated @RequestBody AddUserLevelConfigDTO dto) {

        log.info("添加用户等级配置,参数:{}", dto);
        userLevelConfigService.addUserLevelConfig(dto);
        return Result.ok();
    }
}

