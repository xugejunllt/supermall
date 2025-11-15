package com.lanf.common.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class IStringUtils {

    public static String splitJoint(List<String> dataList, String so) {

        StringBuffer v2 = new StringBuffer();

        for (int i = 0; i < dataList.size(); i++) {

            v2.append(dataList.get(i).toString());

            if (i != dataList.size() - 1) {

                v2.append(so);
            }

        }
        return v2.toString();
    }

    public static List<String> toList(String value, String so) {


        return new ArrayList<>(Arrays.asList(value.split(so)));

    }

    /**
     *
     *
     *
     */
    public static String generateKey(List<Long> value,String content) {

        List<String> collected = value.stream().map(a -> a.toString()).collect(Collectors.toList());
        String splitJoint = splitJoint(collected, ",")+content;


        return MD5.encrypt(splitJoint);

    }
    public static boolean isEmpty(CharSequence cs) {

        return org.apache.commons.lang3.StringUtils.isEmpty(cs);
    }
}
