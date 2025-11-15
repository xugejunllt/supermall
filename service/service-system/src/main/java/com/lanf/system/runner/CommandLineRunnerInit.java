package com.lanf.system.runner;

import com.lanf.bizcache.model.bo.PlatformRateConfigBO;
import com.lanf.bizcache.service.BizCacheService;
import com.lanf.common.utils.BeanCopyUtils;
import com.lanf.system.model.entiry.CompanyDO;
import com.lanf.system.model.entiry.PlatformRateConfigDO;
import com.lanf.system.service.IPlatformRateConfigService;
import com.lanf.system.service.company.ICompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CommandLineRunnerInit implements CommandLineRunner {

    @Autowired
    private IPlatformRateConfigService platformRateConfigService;
    @Autowired
    private ICompanyService companyService;

    @Override
    public void run(String... args) throws Exception {

        /**
         * 加载缓存平台费率配置
         */
        List<PlatformRateConfigDO> list = platformRateConfigService.lambdaQuery().list();
        List<PlatformRateConfigBO> platformRateConfigDOList = BeanCopyUtils.copyBeanList(list, PlatformRateConfigBO.class);

        BizCacheService.addCache(platformRateConfigDOList);
        /**
         * 加载缓存平台商家信息
         */
        Long id = companyService.lambdaQuery().eq(CompanyDO::getUserType, 0).one().getId();
        BizCacheService.addCache(id);

    }
}