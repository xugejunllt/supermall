package com.lanf.security.config;

import com.lanf.cache.service.RedissonCacheService;
import com.lanf.security.custom.IBCryptPasswordEncoder;
import com.lanf.security.filter.AdminAuthFilter;
import com.lanf.security.filter.AdminLoginFilter;
import com.lanf.web.auth.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Configuration
@EnableWebSecurity //@EnableWebSecurity是开启SpringSecurity的默认行为
@EnableGlobalMethodSecurity(prePostEnabled = true)//开启注解功能，默认禁用注解
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    private RedisTemplate redisTemplate;

    @Qualifier("userDetailsServiceImpl")
    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private IBCryptPasswordEncoder customMd5PasswordEncoder;

    @Autowired
    private Environment environment;
    @Autowired
    private FilterPathConfig filterPathConfig;

    @Autowired
    private AuthService authService;

    @Autowired
    private RedissonCacheService redissonCacheService;
    @Value("${token.user.accessTokenExpMinutes:100000000}")
    private Long accessTokenExpMinutes;

    @Value("${token.user.refreshTokenExpMinutes:1000000000}")
    private Long refreshTokenExpMinutes;

    @Bean
    @Override
    protected AuthenticationManager authenticationManager() throws Exception {
        return super.authenticationManager();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        // 这是配置的关键，决定哪些接口开启防护，哪些接口绕过防护
        http
                //关闭csrf
                .csrf().disable()
                // 开启跨域以便前端调用接口
                .authorizeRequests()
                // 指定某些接口不需要通过验证即可访问。登陆接口肯定是不需要认证的
                .antMatchers("/system/admin/system/index/login",
                        "/system/admin/system/index/getI18n").permitAll()
                // 这里意思是其它所有接口需要认证才能访问
                .anyRequest().authenticated()
                .and()
                /**
                 * TokenAuthenticationFilter放到UsernamePasswordAuthenticationFilter的前面，
                 * 这样做就是为了除了登录的时候去查询数据库外，
                 * 其他时候都用token进行认证。
                 * 就说 如果不配置  默认请求都先走 UsernamePasswordAuthenticationFilter
                 * 实际上 除登入接口外 都先走AdminAuthFilter
                 */
                .addFilterBefore(new AdminAuthFilter(authService), UsernamePasswordAuthenticationFilter.class)
                .addFilter(new AdminLoginFilter(authenticationManager(),
                        redissonCacheService,accessTokenExpMinutes,refreshTokenExpMinutes));


        //禁用session
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }


    /**
     * Cors 的配置信息 配置+路径
     */
    CorsConfigurationSource corsConfiguration() {
        // Cors配置类
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowCredentials(false); // 是否返回时生成凭证
        corsConfiguration.setAllowedHeaders(Collections.singletonList("*")); // 允许请求携带哪些请求头信息
        corsConfiguration.setAllowedMethods(Collections.singletonList("*")); // 允许哪些类型的请求方法
        corsConfiguration.setAllowedOrigins(Collections.singletonList("*")); // 允许哪些域可以进行方法
        corsConfiguration.setMaxAge(3600L); // 设置预检的最大的时长
        corsConfiguration.setExposedHeaders(Collections.emptyList()); // 设置返回暴露的响应头信息
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        /**
         *  指定UserDetailService和加密器
         *  security框架进行登入认证时，
         *  默认使用 username 查询 账户信息，但这里是多租户登入，
         *  所以需要自定义查询逻辑。
         *  即指定 实现了UserDetailsService 接口的对象 这个对象重写了
         *   UserDetails loadUserByUsername(String username) 方法
         *
         */
        auth.userDetailsService(userDetailsService).passwordEncoder(customMd5PasswordEncoder);
    }

    /**
     * 配置哪些请求不拦截
     * 排除swagger相关请求
     */
    @Override
    public void configure(WebSecurity web)  {
        List<String> list = new ArrayList<>();
        list.add("/favicon.ico");
        list.add("/swagger-resources/**");
        list.add("/webjars/**");
        list.add("/v2/**");
        list.add("/swagger-ui.html/**");
        list.add("/doc.html");
        list.add("/temp/sysUser.xlsx");
        list.add("/img/**");
        list.add("/admin/system/index/getI18n");
        String arr[] = list.toArray(new String[0]);
        web.ignoring().antMatchers(arr);
    }
}
