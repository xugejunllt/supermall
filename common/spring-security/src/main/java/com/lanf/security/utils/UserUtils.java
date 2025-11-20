package com.lanf.security.utils;

import com.alibaba.fastjson.JSON;
import com.lanf.common.utils.BeanUtil;
import com.lanf.common.utils.JwtUtils;
import com.lanf.constant.constant.Constants;
import com.lanf.system.model.bo.SysUserBO;
import com.lanf.web.utils.WebUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * @author tanlingfei
 * @version 1.0
 * @description TODO
 * @date 2023/4/27 15:53
 */
public class UserUtils {

    public static String getAdminUserId() {
        HttpServletRequest request = WebUtil.getRequest();
        if (request == null) {

            return null;
        }
        String token = request.getHeader("token");
        if (StringUtils.isEmpty(token)) {
            token = request.getParameter("token");
        }
        String userId = JwtUtils.getUserIds(token);
        return userId;
    }




    public static Long getUserId() {
        HttpServletRequest request = WebUtil.getRequest();
        String token = request.getHeader(Constants.USER_TOKEN);
        String userId = JwtUtils.getUserIds(token);
        return Long.parseLong(userId);
    }


    public static SysUserBO getUserInfo() {
        String userId = getAdminUserId();
        if (userId == null) {
            return null;
        }
        RedisTemplate redisTemplate = (RedisTemplate) BeanUtil.getBean("redisTemplate");
        String result = (String) redisTemplate.opsForValue().get(userId);
        if (result != null) {
            return JSON.parseObject(result, SysUserBO.class);
        }
        return null;
    }

    public static String getTenantCode() {

        HttpServletRequest request = WebUtil.getRequest();
        if (request == null) {

            return null;
        }
        String token = request.getHeader("token");

       return JwtUtils.getTenantCode(token);
    }

    public static Long getShopId() {

        SysUserBO userInfo = null;
        try {
            userInfo = getUserInfo();
        } catch (Exception e) {
            return null;
        }

        if (userInfo == null) {
            return null;
        }
        return null;
    }

    public static Long getBusinessId() {

        SysUserBO userInfo = null;
        try {
            userInfo = getUserInfo();
        } catch (Exception e) {
            return null;
        }

        if (userInfo == null) {
            return null;
        }
        return null;

    }
}