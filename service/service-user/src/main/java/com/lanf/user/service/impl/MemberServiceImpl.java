package com.lanf.user.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lanf.common.utils.ImageUtils;
import com.lanf.common.utils.JwtUtils;
import com.lanf.mybatis.base.BaseEntity;
import com.lanf.mybatis.base.PageResult;
import com.lanf.redis.constant.CacheConstants;
import com.lanf.redis.service.RedisCache;
import com.lanf.security.custom.IBCryptPasswordEncoder;
import com.lanf.user.mapper.MemberMapper;
import com.lanf.user.model.dto.LoginOutDTO;
import com.lanf.user.model.dto.UserLoginDTO;
import com.lanf.user.model.dto.UserRegisterDTO;
import com.lanf.user.model.entity.MemberDO;
import com.lanf.user.model.query.UserPageQuery;
import com.lanf.user.model.vo.UserLoginVO;
import com.lanf.user.model.vo.UserPageVO;
import com.lanf.user.service.IMemberService;
import com.lanf.web.exception.BizException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-10
 */
@Service
public class MemberServiceImpl extends ServiceImpl<MemberMapper, MemberDO> implements IMemberService {

    @Autowired
    private IBCryptPasswordEncoder ibCryptPasswordEncoder;

    @Autowired
    private RedisCache redisCache;

    @Override
    public void userRegister(UserRegisterDTO dto) {

        String phoneNumber = dto.getPhoneNumber();
        MemberDO one = this.lambdaQuery().eq(MemberDO::getPhoneNumber, phoneNumber).one();
        if (one != null) {
            throw new BizException("该手机号已注册");
        }

        MemberDO memberDO = new MemberDO();
        memberDO.setUserName("孤单的一个人666");
        memberDO.setUserPassword(ibCryptPasswordEncoder.encode(dto.getPassWord()));
        memberDO.setPhoneNumber(phoneNumber);
        memberDO.setNickName("孤单的一个人");
        memberDO.setUserStatus(1);
        memberDO.setRegisterSource(dto.getRegisterSource());
        memberDO.setHeadImageUrl(ImageUtils.getDefaultImage());
        this.save(memberDO);



    }

    @Override
    public UserLoginVO userLogin(UserLoginDTO dto) {

        MemberDO one = this.lambdaQuery().eq(MemberDO::getPhoneNumber, dto.getPhoneNumber()).one();
        if (one == null) {
            throw new BizException("用户不存在");
        }

        String userToken = JwtUtils.createUserToken(one.getId());
        String userRefreshToken = JwtUtils.createUserRefreshToken(one.getId());
        String key = CacheConstants.USER_TOKEN + one.getId();
        redisCache.setCacheObject(key, userToken, 8, TimeUnit.DAYS);
        UserLoginVO userLoginVO = new UserLoginVO();
        userLoginVO.setToken(userToken);
        userLoginVO.setUserId(one.getId());
        userLoginVO.setUserName(one.getUserName());
        userLoginVO.setHeadImageUrl(one.getHeadImageUrl());
        userLoginVO.setRefreshToken(userRefreshToken);
        return userLoginVO;
    }

    @Override
    public void loginOut(LoginOutDTO dto) {
        String key = CacheConstants.USER_TOKEN + dto.getUserId();
        redisCache.deleteObject(key);
    }

    @Override
    public PageResult<UserPageVO> userPage(UserPageQuery query) {

        IPage<MemberDO> page = new Page<>(query.getPage(), query.getPageSize());
        IPage<MemberDO> result = this.lambdaQuery().
                orderByDesc(BaseEntity::getId)

                .page(page);
        return PageResult.toPageResult(result,UserPageVO.class);
    }

}
