package com.wanderers.wanderers.sys.security;

import com.wanderers.wanderers.sys.utils.JWTUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JWTAuthenticationFilter extends BasicAuthenticationFilter {
    public JWTAuthenticationFilter(AuthenticationManager authenticationManager){ super(authenticationManager);}

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException,ServletException{
        //获得header值
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        //判断是否为空
        if(StringUtils.isBlank(header) || !header.startsWith(JWTUtils.BEARER)){
            chain.doFilter(request,response);
        }

        //如果有token，并且合法，就讲token转换成Authentication对象
        Authentication authentication = JWTUtils.extractJWT(request);

        //将authentication对象交给springsecurity的环境，供其进行验证
        SecurityContextHolder.getContext().setAuthentication(authentication);
        //再一次生成token,并且将token发送给前台
        response.addHeader(HttpHeaders.AUTHORIZATION,JWTUtils.generateJWT(authentication.getName(),authentication.getAuthorities()));

        chain.doFilter(request,response);
    }
}
