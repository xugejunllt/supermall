package com.lanf.user;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lanf.cache.service.RedissonCacheService;
import com.lanf.user.controller.app.UserController;
import com.lanf.web.exception.IExpiredJwtException;
import com.lanf.web.exception.TokenParseException;
import com.lanf.web.model.bo.JwtTokenInfo;
import com.lanf.web.security.keygen.RsaEncryptKeyManager;
import com.lanf.web.security.keygen.SignKeyManager;
import com.lanf.web.security.sign.SignUtils;
import com.lanf.web.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户服务单元测试
 * 测试签名验证功能
 */
@Slf4j
@SpringBootTest
public class SignTest {

    @Autowired
    private SignKeyManager signKeyManager;

    @Autowired
    private SignUtils signUtils;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    @Autowired
    private UserController userController;

    @Autowired
    private RsaEncryptKeyManager rsaEncryptKeyManager;

    @Autowired
    private RedissonCacheService redissonCacheService;

    /**
     * 测试Token中signingKey的存在性
     */
    @Test
    public void testTokenContainsSigningKey() throws TokenParseException, IExpiredJwtException {

            log.info("========== 开始执行Token包含signingKey测试 ==========");

            //1.生成Token
            log.info("步骤1：生成Token");
            String token = JwtUtils.createTokenForUserWithMinutes(999L, "test-device", 3);
            log.info("Token长度: {}", token.length());
            JwtTokenInfo jwtTokenInfo = JwtUtils.parseUserToken(token);
             log.info("signingKey: {}", jwtTokenInfo.getExpTime());

    }

        /**
         * 完整的签名生成与验证测试（模拟 SignFilter 流程）
         * 1. 自定义业务参数
         * 2. 生成签名
         * 3. 验证签名
         */
        @Test
        public void testCompleteSignProcess() {
            try {
                log.info("========== 开始执行完整的签名生成与验证测试 ==========");

                // ==================== 第一阶段：生成签名密钥（模拟 /getSignKey）====================
                log.info("步骤1：生成签名密钥");
                SignKeyManager.SignKeyInfo signKeyInfo = signKeyManager.generateSignKey();
                String signRandomKey = signKeyInfo.getSignRandomKey();
                log.info("签名密钥生成成功, randomKey={}", signRandomKey);

                // ==================== 第二阶段：生成签名（模拟前端）====================

                //3.自定义业务参数（模拟前端请求体）
                log.info("步骤3：自定义业务参数");
                Map<String, Object> businessParams = new HashMap<>();
                businessParams.put("phoneNumber", "13800138000");
                businessParams.put("userName", "张三");
                businessParams.put("age", 25);

                String requestBodyJson = OBJECT_MAPPER.writeValueAsString(businessParams);
                log.info("请求体JSON: {}", requestBodyJson);

                //4.解析请求体为Map（模拟 SignFilter 第109行）
                log.info("步骤4：解析请求体为Map");
                Map<String, Object> params = OBJECT_MAPPER.readValue(requestBodyJson, new TypeReference<Map<String, Object>>() {
                });
                log.info("解析后的参数: {}", params);

                //5.生成签名字符串（按key排序，排除sign字段）
                log.info("步骤5：生成签名字符串");
                String signString = signUtils.generateSignString(params);
                log.info("待签名字符串: {}", signString);

                //6.使用HMAC-SHA256生成签名（模拟前端签名行为）
                log.info("步骤6：使用HMAC-SHA256生成签名");
                byte[] signKeyBytes = signKeyManager.getSignKeyBytes(signRandomKey);

                String sign = signUtils.generateSign(signKeyBytes, signString);
                log.info("生成的签名: {}", sign);

                // ==================== 第三阶段：验证签名（模拟 SignFilter）====================

                //7.将sign添加到参数Map中（模拟 SignFilter 第114行）
                log.info("步骤7：将sign添加到参数Map");
                params.put("sign", sign);
                log.info("添加sign后的参数数量: {}", params.size());

                //8.从Redis获取签名密钥（模拟 SignFilter 第112行）
                log.info("步骤8：从Redis获取签名密钥");
                byte[] signKeyBytesFromRedis = signKeyManager.getSignKeyBytes(signRandomKey);
                log.info("从Redis获取的签名密钥长度: {} 字节", signKeyBytesFromRedis.length);

                //9.验证签名（模拟 SignFilter 第116行）
                log.info("步骤9：调用SignUtils验证签名");
                boolean isValid = signUtils.verifySign(signKeyBytesFromRedis, params);
                log.info("签名验证结果: {}", isValid ? "✅ 通过" : "❌ 失败");

                //10.断言验证结果
                if (!isValid) {
                    log.error("签名验证失败！");
                    throw new RuntimeException("签名验证失败");
                }

                log.info("========== 完整的签名生成与验证测试完成 ==========");

            } catch (Exception e) {
                log.error("测试执行失败", e);
                throw new RuntimeException("测试执行失败", e);
            }
        }

    /**
     * 完整
     * 1.接口获取签名密钥
     * 2.生成签名
     * 3.拿到签名值请求
     */
    @Test
    public void testCompleteSignProcess2() {
        try {
            log.info("========== 开始执行完整的签名生成与验证测试 ==========");



            String signRandomKey = "3406ab6c0a964903a00b8b73ca2ed1b5";
            // ==================== 第二阶段：生成签名（模拟前端）====================

            //3.自定义业务参数（模拟前端请求体）
            log.info("步骤3：自定义业务参数");
            Map<String, Object> businessParams = new HashMap<>();
            businessParams.put("phoneNumber", "13800138000");
            businessParams.put("userName", "张三");
            businessParams.put("age", 25);

            String requestBodyJson = OBJECT_MAPPER.writeValueAsString(businessParams);
            log.info("请求体JSON: {}", requestBodyJson);

            //4.解析请求体为Map（模拟 SignFilter 第109行）
            log.info("步骤4：解析请求体为Map");
            Map<String, Object> params = OBJECT_MAPPER.readValue(requestBodyJson, new TypeReference<Map<String, Object>>() {
            });
            log.info("解析后的参数: {}", params);

            //5.生成签名字符串（按key排序，排除sign字段）
            log.info("步骤5：生成签名字符串");
            String signString = signUtils.generateSignString(params);
            log.info("待签名字符串: {}", signString);

            //6.使用HMAC-SHA256生成签名（模拟前端签名行为）
            log.info("步骤6：使用HMAC-SHA256生成签名");
            byte[] signKeyBytes = signKeyManager.getSignKeyBytes(signRandomKey);

            String sign = signUtils.generateSign(signKeyBytes, signString);
            log.info("生成的签名: {}", sign);

            // ==================== 第三阶段：验证签名（模拟 SignFilter）====================

            //7.将sign添加到参数Map中（模拟 SignFilter 第114行）
            log.info("步骤7：将sign添加到参数Map");
            params.put("sign", sign);
            log.info("添加sign后的参数数量: {}", params.size());

            //8.从Redis获取签名密钥（模拟 SignFilter 第112行）
            log.info("步骤8：从Redis获取签名密钥");
            byte[] signKeyBytesFromRedis = signKeyManager.getSignKeyBytes(signRandomKey);
            log.info("从Redis获取的签名密钥长度: {} 字节", signKeyBytesFromRedis.length);

            //9.验证签名（模拟 SignFilter 第116行）
            log.info("步骤9：调用SignUtils验证签名");
            boolean isValid = signUtils.verifySign(signKeyBytesFromRedis, params);
            log.info("签名验证结果: {}", isValid ? "✅ 通过" : "❌ 失败");

            //10.断言验证结果
            if (!isValid) {
                log.error("签名验证失败！");
                throw new RuntimeException("签名验证失败");
            }

            log.info("========== 完整的签名生成与验证测试完成 ==========");

        } catch (Exception e) {
            log.error("测试执行失败", e);
            throw new RuntimeException("测试执行失败", e);
        }
    }

    /**
     * 完整的签名生成与验证测试（使用Token中的signingKey）
     * 1. 生成JWT Token
     * 2. 解析Token获取signingKey
     * 3. 使用signingKey生成签名
     * 4. 验证签名
     */
    @Test
    public void testSignWithTokenSigningKey() {
        try {
            log.info("========== 开始执行基于Token signingKey的签名测试 ==========");

            // ==================== 第一阶段：生成并解析Token ====================
            
            //1.准备用户信息
            log.info("步骤1：准备用户信息");
            Long userId = 1L;
            String deviceId = "122asd";
            long expDays = 7;
            log.info("userId: {}, deviceId: {}, expDays: {}", userId, deviceId, expDays);

            //2.生成JWT Token
            log.info("步骤2：生成JWT Token");
            String token = "eyJhbGciOiJIUzUxMiIsInppcCI6IkdaSVAifQ.H4sIAAAAAAAAAKtWKi5NUrJScgwN8dANDXYNUtJRSq0oULIyNze3NDK0MDUw0VEqLU4t8kwBqjI0NTAzsTAwNbOwtDCyNDMzMjcCqk9JLctMToUoMDJKLE4BihVnpudl5qV7p1YCRfM8PDLNnFOCvCLKPCpcIoCmukWZBNraKtUCANaO2-t_AAAA.QSmW-KZeF3FNMJ4iEC0ixXEdO08qOVpzFxGJrUAI6slj82SP-Dkq_RUjumpuOiNlP3Jcz6VZS9VxTYKHX0HzxQ";
            log.info("Token生成成功，长度: {}", token.length());

            //3.解析Token获取signingKey
            log.info("步骤3：解析Token获取signingKey");
            JwtTokenInfo tokenInfo = JwtUtils.parseUserToken(token);


            // ==================== 第二阶段：准备业务数据并生成签名 ====================
            
            //4.自定义业务参数（模拟前端请求体）
            log.info("步骤4：准备业务参数");
            Map<String, Object> businessParams = new HashMap<>();
            businessParams.put("phoneNumber", "13800138000");
            businessParams.put("userName", "张三");
            businessParams.put("age", 25);
            
            String requestBodyJson = OBJECT_MAPPER.writeValueAsString(businessParams);
            log.info("请求体JSON: {}", requestBodyJson);

            //5.解析请求体为Map
            log.info("步骤5：解析请求体为Map");
            Map<String, Object> params = OBJECT_MAPPER.readValue(requestBodyJson, new TypeReference<Map<String, Object>>() {});
            log.info("解析后的参数数量: {}", params.size());

            //6.生成签名字符串（按key排序，排除sign字段）
            log.info("步骤6：生成签名字符串");
            String signString = signUtils.generateSignString(params);
            log.info("待签名字符串: {}", signString);

            //7.使用signingKey生成签名
            log.info("步骤7：使用signingKey生成签名");
            byte[] signKeyBytes = java.util.Base64.getDecoder().decode(tokenInfo.getSigningKey());
            log.info("签名密钥长度: {} 字节", signKeyBytes.length);
            
            String sign = signUtils.generateSign(signKeyBytes, signString);
            log.info("生成的签名: {}", sign);

            // ==================== 第三阶段：验证签名 ====================
            
            //8.将sign添加到参数Map中
            log.info("步骤8：将sign添加到参数Map");
            params.put("sign", sign);
            log.info("添加sign后的参数数量: {}", params.size());

            //9.验证签名
            log.info("步骤9：调用SignUtils验证签名");
            boolean isValid = signUtils.verifySign(signKeyBytes, params);
            log.info("签名验证结果: {}", isValid ? "✅ 通过" : "❌ 失败");

            //10.断言验证结果
            if (!isValid) {
                log.error("❌ 签名验证失败！");
                throw new RuntimeException("签名验证失败");
            }

            log.info("✅ userId验证通过: {}", tokenInfo.getUserId().equals(userId));
            log.info("✅ deviceId验证通过: {}", tokenInfo.getDeviceId().equals(deviceId));
            log.info("✅ 签名验证通过");

            log.info("========== 基于Token signingKey的签名测试完成 ==========");

        } catch (Exception e) {
            log.error("测试执行失败", e);
            throw new RuntimeException("测试执行失败", e);
        }
    }

    /**
     * 测试RSA密钥对生成、加密和解密完整流程
     */
    @Test
    public void testRsaEncryptDecrypt() {
        try {
            log.info("========== 开始执行RSA加密解密测试 ==========");


            String randomKey = "3b4cd6f6a6d5478186fb602f573f7b6c";

            log.info("randomKey: {}", randomKey);

            //2.准备测试数据
            log.info("步骤2：准备测试数据");
            String originalData = "18320911824";
            log.info("原始数据: {}", originalData);

            //3.使用公钥加密数据
            log.info("步骤3：使用公钥加密数据");
            String encryptedData = rsaEncryptKeyManager.encryptByRandomKey(randomKey, originalData);
            log.info("加密成功");
            log.info("加密后的数据: [{}]",encryptedData);

            //4.使用私钥解密数据并验证
            log.info("步骤4：使用私钥解密数据并验证");
            String decryptedData = rsaEncryptKeyManager.decryptAndVerify(randomKey, encryptedData);
            log.info("解密成功");
            log.info("解密数据: {}", decryptedData);

            //5.验证解密后的数据与原始数据是否一致
            log.info("步骤5：验证数据一致性");
            if (!originalData.equals(decryptedData)) {
                log.error("❌ 数据不一致！");
                log.error("原始数据: {}", originalData);
                log.error("解密数据: {}", decryptedData);
                throw new RuntimeException("数据一致性验证失败");
            }
            
            log.info("✅ 数据一致性验证通过");
            log.info("✅ 原始数据: {}", originalData);
            log.info("✅ 解密数据: {}", decryptedData);

            log.info("========== RSA加密解密测试完成 ==========");

        } catch (Exception e) {
            log.error("测试执行失败", e);
            throw new RuntimeException("测试执行失败", e);
        }
    }

    /**
     * 不需要登录的签名测试（使用 randomKey）
     * 模拟 /getSignKey 获取签名密钥后进行 HMAC 签名
     */
    @Test
    public void testSignWithoutLogin() {
        try {
            log.info("========== 开始执行【不需要登录】的签名测试 ==========");

            // 1. 生成签名密钥（模拟调用 /getSignKey）
            log.info("步骤1：生成签名密钥");
            SignKeyManager.SignKeyInfo signKeyInfo = signKeyManager.generateSignKey();
            String signRandomKey = signKeyInfo.getSignRandomKey();
            log.info("签名密钥生成成功, randomKey={}", signRandomKey);

            // 2. 构造业务参数
            log.info("步骤2：构造业务参数");
            Map<String, Object> businessParams = new HashMap<>();
            businessParams.put("phoneNumber", "13800138000");
            businessParams.put("userName", "张三");
            businessParams.put("age", 25);
            log.info("业务参数: {}", businessParams);

            // 3. 生成签名字符串
            log.info("步骤3：生成签名字符串");
            String signString = signUtils.generateSignString(businessParams);
            log.info("待签名字符串: {}", signString);

            // 4. 使用 HMAC-SHA256 生成签名
            log.info("步骤4：使用HMAC-SHA256生成签名");
            byte[] signKeyBytes = signKeyManager.getSignKeyBytes(signRandomKey);
            String sign = signUtils.generateSign(signKeyBytes, signString);
            log.info("生成的签名: {}", sign);

            // 5. 将签名添加到参数中并验证
            log.info("步骤5：验证签名");
            businessParams.put("sign", sign);
            boolean isValid = signUtils.verifySign(signKeyBytes, businessParams);
            log.info("签名验证结果: {}", isValid ? "✅ 通过" : "❌ 失败");

            if (!isValid) {
                throw new RuntimeException("签名验证失败");
            }

            log.info("========== 【不需要登录】的签名测试完成 ==========");

        } catch (Exception e) {
            log.error("【不需要登录】签名测试失败", e);
            throw new RuntimeException("【不需要登录】签名测试失败", e);
        }
    }

    /**
     * 需要登录的签名测试（使用 Token 中的 signingKey）
     * 模拟登录后从 Token 获取 signingKey 进行 HMAC 签名
     */
    @Test
    public void testSignWithLogin() {
        try {
            log.info("========== 开始执行【需要登录】的签名测试 ==========");

            // 1. 生成 JWT Token（模拟登录成功）
            log.info("步骤1：生成JWT Token（模拟登录）");
            Long userId = 1L;
            String deviceId = "test-device-001";
            long expMinutes = 30;
            String token = JwtUtils.createTokenForUserWithMinutes(userId, deviceId, expMinutes);
            log.info("Token生成成功, 长度: {}", token.length());

            // 2. 解析 Token 获取 signingKey
            log.info("步骤2：解析Token获取signingKey");
            JwtTokenInfo tokenInfo = JwtUtils.parseUserToken(token);
            String signingKey = tokenInfo.getSigningKey();
            log.info("signingKey: {}", signingKey);

            // 3. 构造业务参数
            log.info("步骤3：构造业务参数");
            Map<String, Object> businessParams = new HashMap<>();
            businessParams.put("phoneNumber", "13800138000");
            businessParams.put("userName", "张三");
            businessParams.put("age", 25);
            log.info("业务参数: {}", businessParams);

            // 4. 生成签名字符串
            log.info("步骤4：生成签名字符串");
            String signString = signUtils.generateSignString(businessParams);
            log.info("待签名字符串: {}", signString);

            // 5. 使用 signingKey 进行 HMAC-SHA256 签名
            log.info("步骤5：使用signingKey进行HMAC-SHA256签名");
            byte[] signKeyBytes = java.util.Base64.getDecoder().decode(signingKey);
            String sign = signUtils.generateSign(signKeyBytes, signString);
            log.info("生成的签名: {}", sign);

            // 6. 将签名添加到参数中并验证
            log.info("步骤6：验证签名");
            businessParams.put("sign", sign);
            boolean isValid = signUtils.verifySign(signKeyBytes, businessParams);
            log.info("签名验证结果: {}", isValid ? "✅ 通过" : "❌ 失败");

            if (!isValid) {
                throw new RuntimeException("签名验证失败");
            }

            log.info("========== 【需要登录】的签名测试完成 ==========");

        } catch (Exception e) {
            log.error("【需要登录】签名测试失败", e);
            throw new RuntimeException("【需要登录】签名测试失败", e);
        }
    }

}
