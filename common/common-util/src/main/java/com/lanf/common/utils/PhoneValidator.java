package com.lanf.common.utils;

import lombok.Data;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.HashSet;
import java.util.Set;

/**
 * 手机号码格式校验工具类
 * 支持中国大陆手机号码格式校验
 */
public class PhoneValidator {
    
    // 中国大陆手机号正则表达式
    private static final String CHINA_MOBILE_REGEX = "^1[3-9]\\d{9}$";
    
    // 各运营商号段 (2024年最新)
    private static final Set<String> OPERATOR_PREFIXES = new HashSet<>();
    
    static {
        // 中国移动
        OPERATOR_PREFIXES.add("134"); OPERATOR_PREFIXES.add("135"); 
        OPERATOR_PREFIXES.add("136"); OPERATOR_PREFIXES.add("137");
        OPERATOR_PREFIXES.add("138"); OPERATOR_PREFIXES.add("139");
        OPERATOR_PREFIXES.add("147"); OPERATOR_PREFIXES.add("148");
        OPERATOR_PREFIXES.add("150"); OPERATOR_PREFIXES.add("151");
        OPERATOR_PREFIXES.add("152"); OPERATOR_PREFIXES.add("157");
        OPERATOR_PREFIXES.add("158"); OPERATOR_PREFIXES.add("159");
        OPERATOR_PREFIXES.add("165"); OPERATOR_PREFIXES.add("172");
        OPERATOR_PREFIXES.add("178"); OPERATOR_PREFIXES.add("182");
        OPERATOR_PREFIXES.add("183"); OPERATOR_PREFIXES.add("184");
        OPERATOR_PREFIXES.add("187"); OPERATOR_PREFIXES.add("188");
        OPERATOR_PREFIXES.add("195"); OPERATOR_PREFIXES.add("197");
        OPERATOR_PREFIXES.add("198");
        
        // 中国联通
        OPERATOR_PREFIXES.add("130"); OPERATOR_PREFIXES.add("131");
        OPERATOR_PREFIXES.add("132"); OPERATOR_PREFIXES.add("140");
        OPERATOR_PREFIXES.add("145"); OPERATOR_PREFIXES.add("146");
        OPERATOR_PREFIXES.add("155"); OPERATOR_PREFIXES.add("156");
        OPERATOR_PREFIXES.add("166"); OPERATOR_PREFIXES.add("167");
        OPERATOR_PREFIXES.add("171"); OPERATOR_PREFIXES.add("175");
        OPERATOR_PREFIXES.add("176"); OPERATOR_PREFIXES.add("185");
        OPERATOR_PREFIXES.add("186"); OPERATOR_PREFIXES.add("196");
        
        // 中国电信
        OPERATOR_PREFIXES.add("133"); OPERATOR_PREFIXES.add("134");
        OPERATOR_PREFIXES.add("141"); OPERATOR_PREFIXES.add("149");
        OPERATOR_PREFIXES.add("153"); OPERATOR_PREFIXES.add("162");
        OPERATOR_PREFIXES.add("173"); OPERATOR_PREFIXES.add("174");
        OPERATOR_PREFIXES.add("177"); OPERATOR_PREFIXES.add("180");
        OPERATOR_PREFIXES.add("181"); OPERATOR_PREFIXES.add("189");
        OPERATOR_PREFIXES.add("190"); OPERATOR_PREFIXES.add("191");
        OPERATOR_PREFIXES.add("193"); OPERATOR_PREFIXES.add("199");
        
        // 虚拟运营商
        OPERATOR_PREFIXES.add("170"); OPERATOR_PREFIXES.add("171");
    }
    
    // 编译正则表达式
    private static final Pattern MOBILE_PATTERN = Pattern.compile(CHINA_MOBILE_REGEX);
    
    /**
     * 基础手机号格式校验
     * @param phone 手机号码
     * @return 是否为有效的手机号格式
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        
        String cleanedPhone = cleanPhoneNumber(phone);
        return MOBILE_PATTERN.matcher(cleanedPhone).matches();
    }
    
    /**
     * 严格手机号校验（包含运营商号段校验）
     * @param phone 手机号码
     * @return 是否为有效的手机号
     */
    public static boolean isStrictValidPhone(String phone) {
        if (!isValidPhone(phone)) {
            return false;
        }
        
        String cleanedPhone = cleanPhoneNumber(phone);
        String prefix = cleanedPhone.substring(0, 3);
        
        return OPERATOR_PREFIXES.contains(prefix);
    }
    
    /**
     * 获取手机号运营商类型
     * @param phone 手机号码
     * @return 运营商类型
     */
    public static MobileOperator getOperator(String phone) {
        if (!isValidPhone(phone)) {
            return MobileOperator.UNKNOWN;
        }
        
        String cleanedPhone = cleanPhoneNumber(phone);
        String prefix = cleanedPhone.substring(0, 3);
        
        // 中国移动
        if (prefix.matches("134[0-8]|135|136|137|138|139|147|148|150|151|152|157|158|159|165|172|178|182|183|184|187|188|195|197|198")) {
            return MobileOperator.CHINA_MOBILE;
        }
        // 中国联通
        else if (prefix.matches("130|131|132|140|145|146|155|156|166|167|171|175|176|185|186|196")) {
            return MobileOperator.CHINA_UNICOM;
        }
        // 中国电信
        else if (prefix.matches("133|1349|141|149|153|162|173|174|177|180|181|189|190|191|193|199")) {
            return MobileOperator.CHINA_TELECOM;
        }
        // 虚拟运营商
        else if (prefix.matches("170|171")) {
            return MobileOperator.VIRTUAL_OPERATOR;
        }
        
        return MobileOperator.UNKNOWN;
    }
    
    /**
     * 清理手机号码（移除空格、横杠等特殊字符）
     * @param phone 原始手机号
     * @return 清理后的手机号
     */
    public static String cleanPhoneNumber(String phone) {
        if (phone == null) {
            return "";
        }
        // 移除所有非数字字符
        return phone.replaceAll("[^0-9]", "");
    }
    

    
    /**
     * 验证手机号并返回详细结果
     * @param phone 手机号码
     * @return 验证结果对象
     */
    public static ValidationResult validatePhone(String phone) {
        ValidationResult result = new ValidationResult();
        result.setOriginalPhone(phone);

        if (phone == null || phone.trim().isEmpty()) {
            result.setValid(false);
            result.setMessage("手机号不能为空");
            return result;
        }
        
        String cleanedPhone = cleanPhoneNumber(phone);
        result.setCleanedPhone(cleanedPhone);

        if (cleanedPhone.length() != 11) {
            result.setValid(false);
            result.setMessage("手机号长度必须为11位");
            return result;
        }
        
        if (!MOBILE_PATTERN.matcher(cleanedPhone).matches()) {
            result.setValid(false);
            result.setMessage("手机号格式不正确");
            return result;
        }
        
        String prefix = cleanedPhone.substring(0, 3);
        if (!OPERATOR_PREFIXES.contains(prefix)) {
            result.setValid(false);
            result.setMessage("手机号号段不存在");
            return result;
        }
        
        result.setValid(true);
        result.setMessage("手机号格式正确");
        result.setOperator(getOperator(cleanedPhone));

        return result;
    }
    
    /**
     * 手机运营商枚举
     */
    public enum MobileOperator {
        CHINA_MOBILE("中国移动"),
        CHINA_UNICOM("中国联通"),
        CHINA_TELECOM("中国电信"),
        VIRTUAL_OPERATOR("虚拟运营商"),
        UNKNOWN("未知运营商");
        
        private final String description;
        
        MobileOperator(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 验证结果类
     */
    @Data
    public static class ValidationResult {
        private String originalPhone;
        private String cleanedPhone;
        private boolean isValid;
        private String message;
        private MobileOperator operator;


    }
}