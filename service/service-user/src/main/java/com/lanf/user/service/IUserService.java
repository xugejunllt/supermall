package com.lanf.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lanf.user.model.dto.LoginUserDTO;
import com.lanf.user.model.dto.RegisterUserDTO;
import com.lanf.user.model.entity.UserDO;
import com.lanf.user.model.vo.LoginUserVO;

/**
 * <p>
 *  User 接口
 * </p>
 *
 * @author jarven
 * @since 2025-10-27
 */
public interface IUserService extends IService<UserDO> {


    void registerUser(RegisterUserDTO dto);

    void registerSendCode(String phoneNumber);

    void  loginSendCode(String phoneNumber);

    LoginUserVO login(LoginUserDTO dt);

}
