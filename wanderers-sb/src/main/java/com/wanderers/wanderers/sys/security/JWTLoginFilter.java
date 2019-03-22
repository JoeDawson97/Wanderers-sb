package com.wanderers.wanderers.sys.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wanderers.wanderers.sys.utils.JWTUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

public class JWTLoginFilter extends UsernamePasswordAuthenticationFilter{
    private AuthenticationManager authenticationManager;

    public JWTLoginFilter(AuthenticationManager authenticationManager){
        this.authenticationManager = authenticationManager;
    }

    /**
     * 从请求头中获取username和password并组装成Authentication对象
     * @param request   httpServlet request
     * @param response    httpServlet response
     * @return Authentication对象
     * @throws AuthenticationException 抛出异常表示提交的数据格式有误，无法进行权限验证
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException{
        //前后端分离的架构，所以不再进行表单提交，而是json数据的直接提交：content-Typr=application/json {username:?,password:?}
        //我们需要将json字符串转换成User对象(UserDetail对象)

        try{
            //需要通过jackson将json数据转换成User对象{username：‘张三’，password：‘1234’}
            User user = new ObjectMapper().readValue(request.getInputStream(),User.class);

            //直接返回Authentication对象，实现类为authentication(new UsernamePasswordAuthenticationToken
            return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    user.getUsername(),
                    user.getPassword(),
                    new ArrayList<>()));
        }catch (IOException e){
            e.printStackTrace();
            throw new RuntimeException(e);  //抛出异常表示提交的数据格式有误，无法进行权限验证
        }
    }


    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain chain,Authentication authResult) throws IOException,ServletException{
        //1 生成jwt token
        String token = JWTUtils.generateJWT(authResult.getName(), authResult.getAuthorities());

        //放入响应头中
        response.addHeader(HttpHeaders.AUTHORIZATION,token);
    }
}
