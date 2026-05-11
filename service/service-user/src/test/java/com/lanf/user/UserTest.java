package com.lanf.user;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lanf.constant.result.Result;
import com.lanf.user.controller.app.UserController;
import com.lanf.user.model.vo.PublicKeyVO;
import com.lanf.web.exception.IExpiredJwtException;
import com.lanf.web.exception.TokenParseException;
import com.lanf.web.model.bo.JwtTokenInfo;
import com.lanf.web.security.encrypt.AesEncryptUtils;
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
public class UserTest {

    @Autowired
    private SignKeyManager signKeyManager;

    @Autowired
    private SignUtils signUtils;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    @Autowired
    private UserController userController;




    /**
     * 测试Token中signingKey的存在性
     */
    @Test
    public void testTokenContainsSigningKey() throws TokenParseException, IExpiredJwtException {

            log.info("========== 开始执行Token包含signingKey测试 ==========");

            //1.生成Token
            log.info("步骤1：生成Token");
            String token = JwtUtils.createTokenForUserWithDays(999L, "test-device", 3);
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

                // ==================== 第一阶段：准备密钥 ====================

                Result<PublicKeyVO> signKey = userController.getSignKey();

                PublicKeyVO data = signKey.getData();

                String signRandomKey = data.getRandomKey();
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

                //6.使用AES密钥加密签名字符串（模拟前端签名行为）
                log.info("步骤6：使用AES密钥生成签名");
                byte[] aesKeyBytes = signKeyManager.getSignKeyBytes(signRandomKey);

                String sign = AesEncryptUtils.encryptByAes(aesKeyBytes, signString);
                log.info("生成的签名: {}", sign);

                // ==================== 第三阶段：验证签名（模拟 SignFilter）====================

                //7.将sign添加到参数Map中（模拟 SignFilter 第114行）
                log.info("步骤7：将sign添加到参数Map");
                params.put("sign", sign);
                log.info("添加sign后的参数数量: {}", params.size());

                //8.从Redis获取AES密钥（模拟 SignFilter 第112行）
                log.info("步骤8：从Redis获取AES密钥");
                byte[] signKeyBytesFromRedis = signKeyManager.getSignKeyBytes(signRandomKey);
                log.info("从Redis获取的AES密钥长度: {} 字节", signKeyBytesFromRedis.length);

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



            String signRandomKey = "5cb7fed51f484473a6736d93c8130367";
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

            //6.使用AES密钥加密签名字符串（模拟前端签名行为）
            log.info("步骤6：使用AES密钥生成签名");
            byte[] aesKeyBytes = signKeyManager.getSignKeyBytes(signRandomKey);

            String sign = AesEncryptUtils.encryptByAes(aesKeyBytes, signString);
            log.info("生成的签名: {}", sign);

            // ==================== 第三阶段：验证签名（模拟 SignFilter）====================

            //7.将sign添加到参数Map中（模拟 SignFilter 第114行）
            log.info("步骤7：将sign添加到参数Map");
            params.put("sign", sign);
            log.info("添加sign后的参数数量: {}", params.size());

            //8.从Redis获取AES密钥（模拟 SignFilter 第112行）
            log.info("步骤8：从Redis获取AES密钥");
            byte[] signKeyBytesFromRedis = signKeyManager.getSignKeyBytes(signRandomKey);
            log.info("从Redis获取的AES密钥长度: {} 字节", signKeyBytesFromRedis.length);

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
            String token = "eyJhbGciOiJIUzUxMiIsInppcCI6IkdaSVAifQ.H4sIAAAAAAAAAKtWKi5NUrJScgwN8dANDXYNUtJRSq0oULIyNDe3NDQ0MTQz0lEqLU4t8kwBiukopaSWZSangjhKhkZGicUpQPXFmel5mXnp3qmVQNGw1GzLKkszIw8XM8sgX_8QXyft0sx0W1ulWgCpaJv2awAAAA.RL-clZThZ6PGk8IHF-kxc6RMCNo0fq1k_t-ECNV_LOJw7lXxKnHDtzhlApjFf2FDz7QVXyrMQ_zJvQpv2_q-7w";
            log.info("Token生成成功，长度: {}", token.length());

            //3.解析Token获取signingKey
            log.info("步骤3：解析Token获取signingKey");
            JwtTokenInfo tokenInfo = JwtUtils.parseUserToken(token);
            String signingKey = tokenInfo.getSigningKey();
            
            if (signingKey == null || signingKey.isEmpty()) {
                log.error("❌ Token中未包含signingKey");
                throw new RuntimeException("Token中未包含signingKey");
            }
            
            log.info("✅ 成功获取signingKey: {}", signingKey);
            log.info("✅ Token解析结果 - userId: {}, deviceId: {}", 
                    tokenInfo.getUserId(), tokenInfo.getDeviceId());

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

            //7.使用signingKey加密签名字符串
            log.info("步骤7：使用signingKey生成签名");
            byte[] aesKeyBytes = java.util.Base64.getDecoder().decode(signingKey);
            log.info("AES密钥长度: {} 字节", aesKeyBytes.length);
            
            String sign = AesEncryptUtils.encryptByAes(aesKeyBytes, signString);
            log.info("生成的签名: {}", sign);

            // ==================== 第三阶段：验证签名 ====================
            
            //8.将sign添加到参数Map中
            log.info("步骤8：将sign添加到参数Map");
            params.put("sign", sign);
            log.info("添加sign后的参数数量: {}", params.size());

            //9.验证签名
            log.info("步骤9：调用SignUtils验证签名");
            boolean isValid = signUtils.verifySign(aesKeyBytes, params);
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


}
