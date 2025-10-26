package com.lanf.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.mybatis.base.PageResult;
import com.lanf.user.model.dto.LoginOutDTO;
import com.lanf.user.model.dto.UserLoginDTO;
import com.lanf.user.model.dto.UserRegisterDTO;
import com.lanf.user.model.entity.MemberDO;
import com.lanf.user.model.query.UserPageQuery;
import com.lanf.user.model.vo.UserLoginVO;
import com.lanf.user.model.vo.UserPageVO;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 江帅帅 Jss_forever
 * @since 2024-06-10
 */
public interface IMemberService extends IService<MemberDO> {

    void  userRegister(UserRegisterDTO dto);

    UserLoginVO userLogin(UserLoginDTO dto);

    void  loginOut(LoginOutDTO dto);

    PageResult<UserPageVO> userPage(UserPageQuery query);
}
