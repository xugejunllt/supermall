package com.lanf.user.service.benefit.manager;

import com.lanf.common.utils.BeanUtil;
import com.lanf.common.utils.IStringUtils;
import com.lanf.common.utils.StackTraceUtil;
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
 * <p>
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

    /**
     * 获取可以权益列表
     */
    public Set<BenefitGrantService> listBenefitGrantService(Set<String> codeSet) {


        if ( !loadSuccess){
            /**
             * 权益使用在异步任务中 如果启动加载失败 上游调用者也能够通过异常来发现
             *
             */
            log.warn("启动加载权益失败");
            throw new BizException("启动加载权益失败");
        }

        if (IStringUtils.isEmpty(codeSet)) {
            throw new BizException("权益code set为空");
        }

        Set<BenefitGrantService> benefitGrantServices = new HashSet<>();
        for (String code : codeSet) {
            BenefitGrantService benefitGrantService = getBenefitGrantService(code);
            if (benefitGrantService == null) {
                //权益没有开放 不加入
                continue;
            }
            benefitGrantServices.add(benefitGrantService);
        }

        return benefitGrantServices;
    }

    private BenefitGrantService getBenefitGrantService(String benefitGrantCode) {

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

            benefitGrantService = BeanUtil.getBean(CouponBenefitGrantServiceImpl.class);
        }
        if (BenefitCodeEnum.GRANT_WALLET_BALANCE.getCode().equals(benefitGrantCode)) {

            benefitGrantService = BeanUtil.getBean(CouponBenefitGrantServiceImpl.class);
        }

        if (benefitGrantService == null) {
            throw new BizException("权益code服务不存在");
        }
        return benefitGrantService;
    }

    private boolean loadSuccess = true ;

    @Override
    public void run(String... args) {


        log.info("应用启动成功,加载BenefitGrantService开始");
        //避免循环依赖 通过上下文工具获取
        IBenefitService benefitService = null;
        try {
            benefitService = BeanUtil.getBean(IBenefitService.class);
        } catch (Exception e) {
            //异常发生的原因无法预知 但提前做异常处理 比如手动DB插入脏数据又或者代码回滚了 新增的类不存在
            //但DB中的code是存在的
            log.error("获取benefitService失败");
            loadSuccess = false;
        }

        //查找开放的权益列表
        List<String> benefitDOList = null;
        try {
            benefitDOList = benefitService.listUseBenefitCode();
        } catch (Exception e) {
            //加载失败 告警 服务进行重启 或者提供API 手动调用执行一次加载
            //如果不进行处理 那么所有权益使用都废弃 在业务上是不允许的
            log.error("DB加载权益列表异常{}", StackTraceUtil.getStackTrace(e));
            loadSuccess = false;
            return;
        }
        if (IStringUtils.isEmpty(benefitDOList)) {
            log.info("没有可以使用的权益");
            return;
        }
        try {
            benefitDOList.forEach(this::addBenefitGrantService);
        } catch (Exception e) {
            log.error("加载权益失败{}",StackTraceUtil.getStackTrace(e));
            loadSuccess = false;
            return;
        }
        log.info("应用启动重构,加载BenefitGrantService完成");

    }
}
