package com.lanf.common.utils;

import com.lanf.constant.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
@Slf4j
public class IStringUtils {

    /**
     * ThreadLocal 缓存 MessageDigest 实例，避免高并发下频繁 getInstance 的开销
     */
    private static final ThreadLocal<MessageDigest> SHA256_HOLDER = ThreadLocal.withInitial(() -> {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (Exception e) {
            log.error("获取 MessageDigest 实例失败", e);
            throw new BizException("获取 MessageDigest 实例失败");
        }
    });
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

    public static boolean isEmpty(List list) {

        return CollectionUtils.isEmpty(list);
    }
    public static boolean isEmpty(Set list) {

        return CollectionUtils.isEmpty(list);
    }
    /**
     * 将字符串内容进行 hash，转成唯一的长度 20 位数字字符串
     */
    public static String hashToUniqueString(String content) {
        if (content == null) {
            throw new BizException("content cannot be null");
        }
        try {
            MessageDigest md = SHA256_HOLDER.get();
            md.reset();
            byte[] digest = md.digest(content.getBytes(StandardCharsets.UTF_8));
            BigInteger hashVal = new BigInteger(1, digest);
            BigInteger mod = new BigInteger("100000000000000000000"); // 10^20
            String result = hashVal.mod(mod).toString();
            // 不足 20 位前面补零
            if (result.length() >= 20) {
                return result.substring(0, 20);
            }
            StringBuilder sb = new StringBuilder();
            for (int i = result.length(); i < 20; i++) {
                sb.append('0');
            }
            sb.append(result);
            return sb.toString();
        } catch (Exception e) {

            log.error("hash content failed", e);
            throw new BizException("hash content failed");
        }
    }
}
