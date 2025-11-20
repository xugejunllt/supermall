package com.lanf.web.interceptor;


import com.lanf.constant.constant.Constants;
import com.lanf.web.utils.WebUtil;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * @author tanlingfei
 * @version 1.0
 * @description TODO
 * @date 2023/5/1 10:46
 */
@Configuration
public class FeignConfig implements RequestInterceptor {


    @Override
    public void apply(RequestTemplate requestTemplate) {


        HttpServletRequest request = WebUtil.getRequest();
        if (request != null) {
            //添加admin token
            String token = request.getHeader(Constants.USER_TOKEN);
            if (!StringUtils.isEmpty(token)) {
                requestTemplate.header(Constants.USER_TOKEN, token);
            }
        }

    }
}
