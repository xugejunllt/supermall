package com.lanf.dynamicsrrefresh.gatewayfilter.util;

import org.springframework.http.HttpHeaders;

public class IpUtils {

    public static  String getIp(HttpHeaders headers) {

//        String ipAddress = Objects.requireNonNull(headers.get("X-Forwarded-For")).get(0) ;
////        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
////            ipAddress = headers.get("Proxy-Client-IP").get(0);
////        }
////        if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
////            ipAddress = headers.get("WL-Proxy-Client-IP").get(0);
////        }
//        if (ipAddress == null){
//            return  "";
//        }


        return "";
    }

}
