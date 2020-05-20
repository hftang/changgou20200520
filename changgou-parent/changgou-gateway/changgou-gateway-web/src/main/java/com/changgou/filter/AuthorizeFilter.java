package com.changgou.filter;

import com.changgou.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @Auther: hftang
 * @Date: 2020/2/24 15:35
 * @Description: 全局的拦截器
 */

@Component
public class AuthorizeFilter implements GlobalFilter, Ordered {

    //令牌头名字
    private static final String AUTHORIZE_TOKEN = "Authorization";

    private static final String USER_LOGIN_URL = "http://localhost:9001/oauth/login";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        //获取请求路径oknkooyrrrh
        String path = request.getURI().getPath();

        if (path.startsWith("/api/user/login")  || URLFilter.hasAuthorize(path)) {
            //放行
            Mono<Void> filter = chain.filter(exchange);
            return filter;
        }

        //1 从
        String tokent = request.getHeaders().getFirst(AUTHORIZE_TOKEN);
        if (StringUtils.isEmpty(tokent)) {
            //请求头中没有，从请求参数中获取
            tokent = request.getQueryParams().getFirst(AUTHORIZE_TOKEN);
        }


        //从cookie中获取token
        HttpCookie first = request.getCookies().getFirst(AUTHORIZE_TOKEN);
        if (first != null) {
            tokent = first.getValue();
        }

        //如果是空的话 我直接让他重定向到登录页面

//        if (StringUtils.isEmpty(tokent)) {
//            response.setStatusCode(HttpStatus.METHOD_NOT_ALLOWED);
//            return response.setComplete();
//        }

        if (StringUtils.isEmpty(tokent)) {

            return needAuthorization(USER_LOGIN_URL+"?FROM="+request.getURI(), exchange);
        }


        //如果有token 则解析一下

        try {
//            Claims claims = JwtUtil.parseJWT(tokent);

            request.mutate().header(AUTHORIZE_TOKEN, "Bearer " + tokent);


        } catch (Exception e) {
            e.printStackTrace();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }

        //放行

        return chain.filter(exchange);
    }

    /***
     * 重定向
     * @param userLoginUrl
     * @param exchange
     * @return
     */
    private Mono<Void> needAuthorization(String userLoginUrl, ServerWebExchange exchange) {

        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.SEE_OTHER);
        response.getHeaders().set("Location", userLoginUrl);
        return exchange.getResponse().setComplete();
    }

    /**
     * 过滤器的执行顺序
     *
     * @return
     */
    @Override
    public int getOrder() {
        return 0;
    }
}
