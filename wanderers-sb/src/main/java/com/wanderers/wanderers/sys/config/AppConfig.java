package com.wanderers.wanderers.sys.config;

import com.wanderers.wanderers.sys.security.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity  //启动springsecurity的web支持
public class AppConfig extends WebSecurityConfigurerAdapter implements WebMvcConfigurer {

    @Autowired
    private UserService userService;

    @Autowired
    private Environment env;

    @Override
    public void addCorsMappings(CorsRegistry registry){
        registry.addMapping("/api/**") //哪些请求需要跨域
                .allowedOrigins("*")
                .allowedHeaders("token")
                .exposedHeaders("token")
                .allowCredentials(true)
                .allowedMethods("DELETE","POST","GET","PUT")
                .maxAge(3600);
    }
}
