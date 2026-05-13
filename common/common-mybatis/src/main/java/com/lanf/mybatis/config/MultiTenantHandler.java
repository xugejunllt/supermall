package com.lanf.mybatis.config;

import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.lanf.constant.utils.UserContext;
import com.lanf.constant.utils.TenantContextHolder;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;

/**
 * 多租户处理器实现TenantLineHandler接口
 *
 * @author hege
 * @Date 2023-08-25
 */
@Slf4j
public class MultiTenantHandler implements TenantLineHandler {

    private final TenantProperties properties;

    public MultiTenantHandler(TenantProperties properties) {
        this.properties = properties;
    }

    /**
     * 获取租户ID值表达式，只支持单个ID值 (实际应该从用户信息中获取)
     *
     * @return 租户ID值表达式
     */
    @Override
    public Expression getTenantId() {


        return new LongValue(UserContext.getTenantId());

    }

    /**
     * 获取租户字段名,默认字段名叫: tenant_id
     *
     * @return 租户字段名
     */
    @Override
    public String getTenantIdColumn() {
        return properties.getColumn();
    }

    /**
     * 根据表名判断是否忽略拼接多租户条件
     * <p>
     * 默认都要进行解析并拼接多租户条件
     *
     * system模块 才会引入 这个拦截器
     * 所以默认 都需要拦截
     * @param tableName 表名
     * @return 是否忽略, true:表示忽略，false:需要解析并拼接多租户条件
     */
    @Override
    public boolean ignoreTable(String tableName) {

        if (properties.getFilterTables().contains(tableName)) {
            return true;
        }
        /**
         * 如果上下文标记不存在 默认返回true
         */
        return TenantContextHolder.isSkipTenant();


    }


}