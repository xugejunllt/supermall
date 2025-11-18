package com.lanf.user.service.benefit.manager;

import com.lanf.common.utils.BeanUtil;
import com.lanf.common.utils.IStringUtils;
import com.lanf.user.model.entity.BenefitDO;
import com.lanf.user.model.enums.BenefitCodeEnum;
import com.lanf.user.service.benefit.IBenefitService;
import com.lanf.web.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 管理权益发放服务
 *
 * 工厂模式 管理对象 如创建 加载入缓存等
 */
@Slf4j
@Component
public class BenefitGrantServiceFactory implements CommandLineRunner {

    private Map<String, BenefitGrantService> benefitGrantServiceMap = new ConcurrentHashMap<>(10);


    public void addBenefitGrantService(String benefitGrantCode) {

        if (IStringUtils.isEmpty(benefitGrantCode)) {
            throw new BizException("权益code为空");
        }

        benefitGrantServiceMap.put(benefitGrantCode, getByBenefitGrantCode(benefitGrantCode));

    }

    public void removeBenefitGrantService(String benefitGrantCode) {

        if (IStringUtils.isEmpty(benefitGrantCode)) {
            throw new BizException("权益code为空");
        }
        benefitGrantServiceMap.remove(benefitGrantCode);

    }

    public Set<BenefitGrantService> listBenefitGrantService(Set<String> codeSet) {

        if (IStringUtils.isEmpty(codeSet)) {
            throw new BizException("权益code set为空");
        }
        Set<BenefitGrantService> benefitGrantServices = new HashSet<>();
        for (String code : codeSet) {
            BenefitGrantService benefitGrantService = getBenefitGrantService(code);
            if (benefitGrantService == null){
                //权益没有开放 不加入
                continue;
            }
            benefitGrantServices.add(benefitGrantService);
        }

        return benefitGrantServices;
    }

    public BenefitGrantService getBenefitGrantService(String benefitGrantCode) {

        if (IStringUtils.isEmpty(benefitGrantCode)) {
            throw new BizException("权益code为空");
        }


        return benefitGrantServiceMap.get(benefitGrantCode);

    }

    private BenefitGrantService getByBenefitGrantCode(String benefitGrantCode) {

        if (IStringUtils.isEmpty(benefitGrantCode)) {
            throw new BizException("权益code为空");
        }
        BenefitGrantService benefitGrantService = null;
        if (BenefitCodeEnum.GRANT_COUPON.getCode().equals(benefitGrantCode)) {

            benefitGrantService = new CouponBenefitGrantServiceImpl();
        }
        if (BenefitCodeEnum.GRANT_WALLET_BALANCE.getCode().equals(benefitGrantCode)) {
            benefitGrantService = new CouponBenefitGrantServiceImpl();
        }

        if (benefitGrantService == null) {
            throw new BizException("权益code服务不存在");
        }

        return  benefitGrantService;
    }


    @Override
    public void run(String... args)  {

        log.info("应用启动重构,加载BenefitGrantService开始");
        //避免循环依赖 通过上下文工具获取
        IBenefitService benefitService = BeanUtil.getBean(IBenefitService.class);
        //查找开放的权益列表
        List<String> benefitDOList = benefitService.listUseBenefitCode();

        if ( IStringUtils.isEmpty(benefitDOList)){
            log.info("没有可以使用的权益");
            return;
        }
        benefitDOList.forEach(this::addBenefitGrantService);
        log.info("应用启动重构,加载BenefitGrantService完成");

    }
}
