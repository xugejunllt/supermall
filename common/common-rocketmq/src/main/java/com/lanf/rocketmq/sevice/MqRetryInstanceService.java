package com.lanf.rocketmq.sevice;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * MQ重试实例编号服务
 * <p>封装分布式实例编号分配和获取逻辑，供定时任务复用</p>
 */
@Slf4j
@Component
public class MqRetryInstanceService {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private RedissonClient redissonClient;

    @Value("${spring.application.name:unknown}")
    private String serviceName;

    /**
     * Redis Hash key前缀：实例编号分配
     */
    private static final String REDIS_MQ_RETRY_ALLOCATE_PREFIX = "mq:retry:allocate:";

    /**
     * 分布式锁key前缀
     */
    private static final String LOCK_KEY_PREFIX = "mq:retry:allocate:lock:";

    /**
     * Redis Hash过期时间（分钟），需大于定时任务间隔
     */
    private static final int REDIS_HASH_EXPIRE_MINUTES = 2;

    /**
     * 获取服务名称
     *
     * @return 服务名
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * 获取分布式锁key
     *
     * @return 锁key
     */
    public String getLockKey() {
        return LOCK_KEY_PREFIX + serviceName;
    }

    /**
     * 获取Redis分配key
     *
     * @return Redis key
     */
    public String getAllocateRedisKey() {
        return REDIS_MQ_RETRY_ALLOCATE_PREFIX + serviceName;
    }

    /**
     * 获取当前实例标识（IP + 端口）
     *
     * @return 实例标识，格式：ip:port
     */
    public String getCurrentInstanceId() {
        try {
            String ip = InetAddress.getLocalHost().getHostAddress();
            int port = getServerPort();
            return ip + ":" + port;
        } catch (Exception e) {
            log.error("获取当前实例标识失败", e);
            return "unknown:" + getServerPort();
        }
    }

    /**
     * 动态获取当前服务的端口
     *
     * @return 端口号
     */
    public int getServerPort() {
        try {
            if (applicationContext instanceof org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext) {
                return ((org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext) applicationContext)
                        .getWebServer().getPort();
            }
        } catch (Exception e) {
            log.error("动态获取端口失败", e);
        }
        // 兜底：尝试从系统属性获取
        String port = System.getProperty("server.port");
        if (port != null) {
            try {
                return Integer.parseInt(port);
            } catch (NumberFormatException ignored) {
            }
        }
        return 8080;
    }

    /**
     * 获取当前实例的编号
     *
     * @return 编号，未分配返回null
     */
    public Integer getServiceNumber() {
        try {
            String instanceId = getCurrentInstanceId();
            String redisKey = getAllocateRedisKey();
            RMap<String, String> allocateMap = redissonClient.getMap(redisKey);
            String numberStr = allocateMap.get(instanceId);
            if (numberStr != null) {
                return Integer.parseInt(numberStr);
            }
            return null;
        } catch (Exception e) {
            log.error("获取当前实例编号失败", e);
            return null;
        }
    }

    /**
     * 获取实例总数
     *
     * @return 实例总数
     */
    public Integer getInstanceCount() {
        try {
            String redisKey = getAllocateRedisKey();
            RMap<String, String> allocateMap = redissonClient.getMap(redisKey);
            return allocateMap.size();
        } catch (Exception e) {
            log.error("获取实例总数失败", e);
            return null;
        }
    }

    /**
     * 分配编号并写入Redis
     *
     * @param instances 实例列表
     */
    public void allocateNumbers(List<InstanceInfo> instances) {
        if (instances == null || instances.isEmpty()) {
            return;
        }
        String redisKey = getAllocateRedisKey();
        RMap<String, String> allocateMap = redissonClient.getMap(redisKey);
        allocateMap.clear();

        for (int i = 0; i < instances.size(); i++) {
            InstanceInfo instance = instances.get(i);
            String instanceId = instance.getHost() + ":" + instance.getPort();
            allocateMap.put(instanceId, String.valueOf(i));
            log.info("分配编号，instanceId:{}, number:{}/{}", instanceId, i, instances.size());
        }

        // 设置过期时间
        allocateMap.expire(REDIS_HASH_EXPIRE_MINUTES, TimeUnit.MINUTES);
    }

    /**
     * 从Nacos获取当前服务的所有实例
     *
     * @return 实例列表
     */
    public List<InstanceInfo> getInstancesFromNacos() {
        List<InstanceInfo> result = new ArrayList<>();
        try {
            // 通过反射获取DiscoveryClient，避免编译时依赖
            Class<?> discoveryClientClass = Class.forName("org.springframework.cloud.client.discovery.DiscoveryClient");
            Object discoveryClient = applicationContext.getBean(discoveryClientClass);
            Method getInstancesMethod = discoveryClientClass.getMethod("getInstances", String.class);
            List<?> instances = (List<?>) getInstancesMethod.invoke(discoveryClient, serviceName);

            if (instances != null && !instances.isEmpty()) {
                for (Object instance : instances) {
                    // 反射获取host和port
                    Class<?> instanceClass = instance.getClass();
                    Method getHostMethod = instanceClass.getMethod("getHost");
                    Method getPortMethod = instanceClass.getMethod("getPort");
                    String host = (String) getHostMethod.invoke(instance);
                    int port = (int) getPortMethod.invoke(instance);
                    result.add(new InstanceInfo(host, port));
                }
            }
        } catch (ClassNotFoundException e) {
            log.warn("DiscoveryClient未找到，可能是非Spring Cloud环境，使用默认单实例");
            // 非Spring Cloud环境，使用当前实例
            result.add(new InstanceInfo("localhost", 8080));
        } catch (Exception e) {
            log.warn("从Nacos获取服务实例失败，使用默认单实例", e);
            result.add(new InstanceInfo("localhost", 8080));
        }
        return result;
    }

    /**
     * 按IP+端口排序实例列表
     *
     * @param instances 实例列表
     */
    public void sortInstances(List<InstanceInfo> instances) {
        Collections.sort(instances, Comparator.comparing(InstanceInfo::getHost)
                .thenComparing(InstanceInfo::getPort));
    }

    /**
     * 实例信息
     */
    public static class InstanceInfo {
        private final String host;
        private final int port;

        public InstanceInfo(String host, int port) {
            this.host = host;
            this.port = port;
        }

        public String getHost() {
            return host;
        }

        public int getPort() {
            return port;
        }
    }
}
