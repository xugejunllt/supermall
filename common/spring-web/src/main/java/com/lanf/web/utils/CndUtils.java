package com.lanf.web.utils;

import com.lanf.constant.exception.BizException;
import com.lanf.web.config.CdnConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindException;

import java.net.URI;
import java.net.URISyntaxException;

@Component
public class CndUtils {

    @Autowired
    private  CdnConfig config;


    private  String getDomain(){

        Integer active = config.getActive();
        if (active == 0){

            return config.getAliyun();
        }
        if (active == 1){

            return config.getAliyun();
        }
        return config.getLocal();
    }


    public  String replace(String url){

        String domain = getDomain();
        String pathAfterDomain = getPathAfterDomain(url);

        return domain+pathAfterDomain;
    }
    /**
     * 获取URL中域名后的部分（路径和查询参数）
     */
    public  String getPathAfterDomain(String url) {


        try {
            URI uri = new URI(url);
            StringBuilder result = new StringBuilder();

            // 添加路径
            if (uri.getPath() != null) {
                result.append(uri.getPath());
            }

            return result.toString();

        } catch (URISyntaxException e) {

            throw new BizException("图片地址替换失败");
        }
    }
    public static void main(String[] args) {
        String[] testUrls = {
                "http://localhost:10010/file/uploadFile",
                "https://example.com/api/v1/users",
                "http://localhost:8080/",
                "http://localhost:9000/path?param=value"
        };

        for (String url : testUrls) {
           // System.out.println(url + " -> " + getPathAfterDomain(url));
        }
    }

}
