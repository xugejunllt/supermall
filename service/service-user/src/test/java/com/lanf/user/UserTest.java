package com.lanf.user.test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lanf.constant.result.Result;
import com.lanf.user.controller.app.UserController;
import com.lanf.user.model.vo.PublicKeyVO;
import com.lanf.web.security.encrypt.AesEncryptUtils;
import com.lanf.web.security.keygen.SignKeyManager;
import com.lanf.web.security.sign.SignUtils;
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

}
