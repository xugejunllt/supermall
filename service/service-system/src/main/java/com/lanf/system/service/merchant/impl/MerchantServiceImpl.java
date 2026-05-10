package com.lanf.system.service.merchant.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.common.utils.CodeGenerateUtils;
import com.lanf.mybatis.utils.IdUtils;
import com.lanf.constant.exception.BizException;
import com.lanf.cache.aop.DistributedLock;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.constant.web.PageResult;
import com.lanf.security.custom.IBCryptPasswordEncoder;
import com.lanf.system.mapper.MerchantMapper;
import com.lanf.system.model.dto.MerchantRegisterDTO;
import com.lanf.system.model.entiry.MerchantDO;
import com.lanf.system.model.entiry.SysUserDO;
import com.lanf.system.model.enums.CompanyStatusEnum;
import com.lanf.system.model.query.CompanyPageQuery;
import com.lanf.system.service.SysUserService;
import com.lanf.system.service.merchant.IMerchantService;
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
    private SysUserService sysUserService;
    @Autowired
    private IBCryptPasswordEncoder cryptPasswordEncoder;
    @Autowired
    private MerchantMapper companyMapper;

    @Autowired
    private IBCryptPasswordEncoder customMd5PasswordEncoder;
    @Override
    @DistributedLock(key = "#companyRegister.phoneNumber")
    public void registerMerchant(MerchantRegisterDTO companyRegister) {

        validateRegisterMerchant( companyRegister);
        //构建公司信息
        MerchantDO merchantDO = buildMerchantDO(companyRegister);

        this.save(merchantDO);

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


    }

    @Override
    @Transactional
    @DistributedLock(key = "#id")
    public void auditApprove(Long id) {

        validateAuditApprove( id);
        //创建默认admin用户
        SysUserDO sysUserDO = buildSysUserDO(id);
        sysUserService.save(sysUserDO);
        //更新商家审核状态
        this.lambdaUpdate().eq(BaseEntity::getId, id)
                .set(MerchantDO::getStatus,1)
                .update();
        /**
         * 发送短信给注册商家 告知默认密码 账号 租户编码 及后台地址
         */

    }

    private SysUserDO buildSysUserDO( Long id){

        MerchantDO company = this.getById(id);

        String password = "123456";
        SysUserDO sysUser = new SysUserDO();
        sysUser.setUsername("admin");
        sysUser.setName(company.getUserName());
        sysUser.setPassword(password);
        sysUser.setMobile(company.getPhoneNumber());
        sysUser.setHeadUrl("http://yaxincheng.oss-cn-shenzhen.aliyuncs.com/images/1722712091070.png?Expires=2040664091&OSSAccessKeyId=LTAI5tDUawmj1r1teFxZBWYo&Signature=0fsf0Nu1FVqEn22WnQRSIt9%2Biwk%3D");
        sysUser.setStatus(1);
        sysUser.setTenantId(company.getId());
        //这里与spring security同一个加密器加密 因为它登入时比较的是加密的密码
        sysUser.setPassword(customMd5PasswordEncoder.encode(password));

        return  sysUser;
    }


    private void  validateAuditApprove(Long id){

        MerchantDO company = this.getById(id);
        if (company == null){
            log.warn("商家不存在");
            throw new BizException("商家不存在");
        }
        if (company.getStatus() != 0){
            log.warn("商家已审核");
            throw new BizException("商家已审核");
        }
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
    public PageResult<MerchantDO> merchantPage(CompanyPageQuery query) {

       IPage<MerchantDO> page = new Page<>(query.getPage(), query.getPageSize());
        IPage<MerchantDO> companyPage = this.lambdaQuery().
                like(!StringUtils.isEmpty(query.getCompanyName()), MerchantDO::getCompany, query.getCompanyName()).
                like(!StringUtils.isEmpty(query.getPhoneNumber()), MerchantDO::getPhoneNumber, query.getPhoneNumber()).page(page);


        return PageResult.toPageResult(companyPage);
    }

}
