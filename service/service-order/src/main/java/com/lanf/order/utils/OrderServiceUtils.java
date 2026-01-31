package com.lanf.order.utils;


import com.lanf.common.utils.IdUtils;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class OrderServiceUtils {


    public static  String generateOrderNumber(){


        return IdUtils.generateId()+"";
    }
}
