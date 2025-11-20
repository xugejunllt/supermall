package com.lanf.system.service.merchant.company;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.common.utils.IdUtils;
import com.lanf.common.utils.CodeGenerateUtils;
import com.lanf.common.utils.ThreadLocalUtils;
import com.lanf.mybatis.base.PageResult;
import com.lanf.security.custom.IBCryptPasswordEncoder;
import com.lanf.system.mapper.MerchantMapper;
import com.lanf.system.model.dto.MerchantRegisterDTO;
import com.lanf.system.model.entiry.MerchantDO;
import com.lanf.system.model.entiry.ShopDO;
import com.lanf.system.model.entiry.SysUserDO;
import com.lanf.system.model.enums.CompanyStatusEnum;
import com.lanf.system.model.query.CompanyPageQuery;
import com.lanf.system.service.SysUserService;
import com.lanf.system.service.merchant.IMerchantService;
import com.lanf.system.service.manager.SystemManagerService;
import com.lanf.web.exception.BizException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-05-28
 */
@Service
public class MerchantServiceImpl extends ServiceImpl<MerchantMapper, MerchantDO> implements IMerchantService {

    @Autowired
    private ShopServiceImpl shopService;
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private IBCryptPasswordEncoder cryptPasswordEncoder;
    @Autowired
    private MerchantMapper companyMapper;
    @Autowired
    private SystemManagerService systemManagerService;

    @Override
    @Transactional
    public void registerMerchant(MerchantRegisterDTO companyRegister) {

        validateRegisterMerchant( companyRegister);
        //构建公司信息
        MerchantDO merchantDO = buildMerchantDO(companyRegister);
        //构建店铺信息
        ShopDO shopDO = buildShopDO(companyRegister);
        this.save(merchantDO);
        shopDO.setMerchantId(merchantDO.getId());
        shopService.save(shopDO);

    }

    private ShopDO buildShopDO(MerchantRegisterDTO companyRegister){

        ShopDO shopSave = new ShopDO();
        shopSave.setName(companyRegister.getShopName());
        //注册时 生成默认头像
        shopSave.setHeadUrl(companyRegister.getHeadUrl());
        shopSave.setId(IdUtils.generateId());
        return  shopSave;
    }


    private MerchantDO buildMerchantDO(MerchantRegisterDTO companyRegister){
        String tenantCode = CodeGenerateUtils.generaCode();
        MerchantDO companySave = new MerchantDO();
        Long companyId = IdUtils.generateId();
        companySave.setId(companyId);
        companySave.setCompany(companyRegister.getCompany());
        companySave.setUserName(companyRegister.getUserName());
        companySave.setPhoneNumber(companyRegister.getPhoneNumber());
        companySave.setStatus(CompanyStatusEnum.IN.getCode());
        companySave.setTenantCode(tenantCode);
        return  companySave;
    }
    private void  validateRegisterMerchant(MerchantRegisterDTO companyRegister){

        checkCode(companyRegister.getCode(), companyRegister.getPhoneNumber());
        //
        MerchantDO company = this.lambdaQuery().eq(MerchantDO::getCompany, companyRegister.getCompany()).one();
        if (company != null) {
            throw new BizException("公司名称已存在");
        }
        company = this.lambdaQuery().eq(MerchantDO::getPhoneNumber, companyRegister.getPhoneNumber()).one();
        if (company != null) {
            throw new BizException("手机号已注册");
        }
        ShopDO shop = shopService.lambdaQuery().eq(ShopDO::getName, companyRegister.getShopName()).one();
        if (shop != null) {
            throw new BizException("店铺名称已存在");
        }

    }

    @Override
    @Transactional
    public void auditing(Long id, Integer status) {

        if (!CompanyStatusEnum.include(status)) {
            throw new BizException("审核状态不存在");
        }
        ThreadLocalUtils.addIgnoreTableName(true);
        MerchantDO company = companyMapper.selectById(id);


        /**
         * 更新审核状态
         */
        //初始admin账号时需要的密码
        //companySave.setAdminPassword(cryptPasswordEncoder.encode(companyRegister.getAdminPassword()));
        MerchantDO companyUpdate = new MerchantDO();
        companyUpdate.setId(company.getId());
        companyUpdate.setStatus(status);
        this.updateById(companyUpdate);
        /**
         * 添加admin用户
         */
        SysUserDO sysUser = new SysUserDO();
        sysUser.setUsername("admin");
        sysUser.setName(company.getUserName());
        sysUser.setPassword(company.getAdminPassword());
        sysUser.setMobile(company.getPhoneNumber());
        sysUser.setHeadUrl("http://yaxincheng.oss-cn-shenzhen.aliyuncs.com/images/1722712091070.png?Expires=2040664091&OSSAccessKeyId=LTAI5tDUawmj1r1teFxZBWYo&Signature=0fsf0Nu1FVqEn22WnQRSIt9%2Biwk%3D");
        sysUser.setStatus(1);
        sysUser.setTenantCode(company.getTenantCode());
        ThreadLocalUtils.addTenantCode(company.getTenantCode());
        sysUserService.save(sysUser);

    }


    /**
     * 校验手机验证码
     *
     * @param code
     */
    private void checkCode(String code, String phoneNumber) {

        if (!code.equals("6666")) {
            throw new BizException("短信验证码错误");
        }
    }

    @Override
    public PageResult<MerchantDO> companyPage(CompanyPageQuery query) {

        ThreadLocalUtils.addIgnoreTableName(true);
       IPage<MerchantDO> page = new Page<>(query.getPage(), query.getPageSize());
        IPage<MerchantDO> companyPage = this.lambdaQuery().
                like(!StringUtils.isEmpty(query.getCompanyName()), MerchantDO::getCompany, query.getCompanyName()).
                like(!StringUtils.isEmpty(query.getPhoneNumber()), MerchantDO::getPhoneNumber, query.getPhoneNumber()).page(page);


        return PageResult.toPageResult(companyPage);
    }

}
