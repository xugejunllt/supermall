package com.lanf.web.sentinel;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Sentinel 熔断降级使用示例
 * <p>
 * 演示如何通过 {@link SentinelResource} 注解对方法进行熔断保护。
 * 配合 {@link SentinelDegradeConfig} 和 {@link SentinelDegradeProperties} 使用。
 *
 * @author qoder
 */
@Slf4j
@Service
public class SentinelDegradeExample {

    /**
     * 示例：对下游服务调用进行熔断保护
     * <p>
     * {@code @SentinelResource} 注解说明：
     * <ul>
     *     <li>value: 资源名称，必须与 {@code sentinel.degrade.rules[*].resource} 配置一致</li>
     *     <li>blockHandler: 熔断触发后的处理方法（必须是当前类的 public 方法）</li>
     *     <li>fallback: 业务异常时的处理方法（可选）</li>
     * </ul>
     *
     * <p>使用方式：在任意 Spring Bean 的方法上添加 {@link SentinelResource} 注解即可。</p>
     */
    @SentinelResource(
            value = "goods-service",
            blockHandler = "goodsServiceBlockHandler"
    )
    public String callGoodsService(Long goodsId) {
        // 模拟调用下游商品服务
        // 实际场景中可能是 Feign 调用或 HTTP 调用
        log.info("调用商品服务，goodsId={}", goodsId);
        return "商品详情: " + goodsId;
    }

    /**
     * 熔断触发后的处理方法
     * <p>
     * 方法签名要求：返回值类型与原方法一致，参数列表为 (原参数, BlockException)
     */
    public String goodsServiceBlockHandler(Long goodsId, BlockException e) {
        log.warn("[Sentinel] 商品服务熔断降级，goodsId={}", goodsId);
        // 返回降级数据，如缓存数据、默认值等
        return "商品服务暂不可用，请稍后再试";
    }

    /**
     * 示例：对库存扣减操作进行熔断保护
     * <p>
     * 使用异常比例策略（grade=1），当异常比例超过 50% 时触发熔断
     */
    @SentinelResource(
            value = "deduct-stock",
            blockHandler = "deductStockBlockHandler"
    )
    public boolean deductStock(Long skuId, Integer count) {
        // 模拟库存扣减逻辑
        log.info("扣减库存，skuId={}, count={}", skuId, count);
        // 模拟业务异常
        if (count < 0) {
            throw new IllegalArgumentException("扣减数量不能小于0");
        }
        return true;
    }

    /**
     * 库存扣减熔断处理方法
     */
    public boolean deductStockBlockHandler(Long skuId, Integer count, BlockException e) {
        log.warn("[Sentinel] 库存扣减熔断降级，skuId={}, count={}", skuId, count);
        // 返回 false 表示扣减失败，由上层业务处理
        return false;
    }

    /**
     * 示例：对查询操作进行熔断保护（使用慢调用比例策略）
     * <p>
     * 配置慢调用比例策略（grade=0），当响应时间超过阈值的比例超过设定值时触发熔断
     */
    @SentinelResource(
            value = "order-query",
            blockHandler = "orderQueryBlockHandler"
    )
    public String queryOrder(String orderNo) {
        // 模拟查询订单
        log.info("查询订单，orderNo={}", orderNo);
        return "订单详情: " + orderNo;
    }

    /**
     * 订单查询熔断处理方法
     */
    public String orderQueryBlockHandler(String orderNo, BlockException e) {
        log.warn("[Sentinel] 订单查询熔断降级，orderNo={}", orderNo);
        return "订单查询服务暂不可用";
    }
}
