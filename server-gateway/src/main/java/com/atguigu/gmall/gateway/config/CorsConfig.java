package com.atguigu.gmall.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * @author mqx
 * @date 2021-2-1 14:29:49
 */
@Configuration // beans.xml spring 的核心配置文件
public class CorsConfig {

    /*
    spring的核心配置文件中添加一个bean标签
    <bean id="corsWebFilter" class ="org.springframework.web.cors.reactive.CorsWebFilter">
    </bean>
     */
    @Bean
    public CorsWebFilter corsWebFilter(){

        // new CorsConfiguration
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        //  设置请求方法  GET, POST, PUT...
        corsConfiguration.addAllowedMethod("*");
        //  设置允许请求域名
        corsConfiguration.addAllowedOrigin("*");
        //  允许携带请求头
        corsConfiguration.addAllowedHeader("*");
        //  设置允许携带cookie
        corsConfiguration.setAllowCredentials(true);

        // 它是一个接口，不能new，则要创建CorsConfigurationSource接口的实例，需要使用实现类！
        UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();
        // 需要标明哪些path 要过滤
        urlBasedCorsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration);
        //  返回CorsWebFilter 对象！
        return new CorsWebFilter(urlBasedCorsConfigurationSource);
    }
}
