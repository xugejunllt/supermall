package com.lanf.security.custom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * <p>
 * 密码处理
 * </p>
 *
 */
@Component
public class IBCryptPasswordEncoder implements PasswordEncoder {

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    public String encode(CharSequence rawPassword) {

        return bCryptPasswordEncoder.encode(rawPassword);
    }

    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        boolean result = bCryptPasswordEncoder.matches(rawPassword, encodedPassword);

        return result;

    }
}
