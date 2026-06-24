package com.lanf.dynamicsrrefresh.core.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StrUtils {

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

}
