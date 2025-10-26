package com.lanf.common.utils;

import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class BeanCopyUtils implements Serializable {

    public static void copy(Object source, Object target) {

        BeanUtils.copyProperties(source, target);
    }

    //单个实体类拷贝
    public static <V> V copyBean(Object source, Class<V> clazz) {
        //创建目标对象
        V result = null;
        try {
            result = clazz.newInstance();
            //实现属性拷贝
            BeanUtils.copyProperties(source, result);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public static <O, V> List<V> copyBeanList(List<O> list, Class<V> clazz) {


        List<V> result = list.stream()
                .map(o -> copyBean(o, clazz))
                .collect(Collectors.toList());
        return result;
    }


}
