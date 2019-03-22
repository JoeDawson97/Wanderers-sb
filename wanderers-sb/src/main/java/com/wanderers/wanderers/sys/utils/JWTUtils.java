package com.wanderers.wanderers.sys.utils;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 用于JWT token的生成和加密
 */
public class JWTUtils {
    public final static String BEARER="Bearer ";
    private static String secret;
    private static Long expire;

    public static void setSecret(String secret){JWTUtils.secret = secret;}
    public static void setExpire(Long expire){JWTUtils.expire = expire;}

    /**
     * 生成jwt token
     * @param subject 用户名
     * @param authorities 代表用户权限组合
     * @return null
     */
    public static String generateJWT(String subject, Collection<? extends GrantedAuthority> authorities){

        String authStr = authorities.stream().map(GrantedAuthority.class::cast)  //map将原有的Object集合转换成GrantedAuthority集合
                .map(GrantedAuthority::getAuthority)  //通过GrantedAuthority的getAuthority方法将集合转换成String集合
                .collect(Collectors.joining(","));  //把每一个小的authStr用“，”隔开，拼成一个大的authStr

        //创建JWT的构造对象JWTClaimsSet
        JWTClaimsSet.Builder builder= new JWTClaimsSet.Builder();
        builder.subject(subject)  //将用户名设为subject
                .issuer("wanderers")  //将项目名称设为发布者
                .expirationTime(new Date((new Date().getTime() + expire)))  //设置token过期时间
                .claim("roles",authStr); //声明自定义的载荷；保存角色信息转化为JSON对象{roles:"admin.user//"}

        //通过构造器生成JWT对象
        JWTClaimsSet claimsSet = builder.build();

        //设置签名
        //创建签名对象
        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256),claimsSet);

        //调用sign方法进行签名
        try {
            signedJWT.sign(new MACSigner(secret));
        }catch (JOSEException e){
            e.printStackTrace();
        }

        //以字符串形式返回token
        return signedJWT.serialize();
    }

    /**
     * 从request中获取token并且进行解析，获得用户名，以及权限，并且将这些信息封装成Spring-security能够识别的Authentication对象
     * @param request http request
     * @return null
     */
    public static Authentication extractJWT(HttpServletRequest request){
        //从request中获得请求头为Authentication的token字符串：" bearer qwertyuiopasdfghjklzxcvbnmqwerty"
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        String token = null;
        if (StringUtils.isNotBlank(authHeader)){
            //如果不为空，对token字符串进行去bearer
            token = authHeader.substring(BEARER.length());
            //解密
            try{
                //将字符串token反序列化为jwt对象
                SignedJWT signedJWT = SignedJWT.parse(token);

                //获得JWT的过期时间，判断是否过期
                boolean isAfter = signedJWT.getJWTClaimsSet().getExpirationTime().after(new Date());

                //检查你的签名是否合法
                boolean isVarify = signedJWT.verify(new MACVerifier(secret));

                //判断jwt是否过期并且是否合法，再解密
                if (isAfter && isVarify){
                    //提取其中的关于user的信息 username和authority
                    String subject;
                    String authStr;
                    List<GrantedAuthority> authorities;

                    //获取subject
                    subject = signedJWT.getJWTClaimsSet().getSubject();

                    //获得authority的字符串形式
                    authStr = (String) signedJWT.getJWTClaimsSet().getClaim("roles");
                    // authStr = "role_admin,role_dbm,role_client

                    //将authStr转换成GrantedAuthority集合
                    authorities = Stream.of(authStr.split(",")) //通过of方法将字符串数组转化为流
                            .filter(StringUtils::isNotBlank)  //排除空字符串
                            .map(SimpleGrantedAuthority::new)  //转换成GrantedAuthority集合
                            .collect(Collectors.toList());
                }

            }catch (ParseException | JOSEException e){
                e.printStackTrace();
            }
        }
        return null;
    }
}
