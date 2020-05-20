package com.changgou;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @Auther: hftang
 * @Date: 2020/2/22 17:10
 * @Description:
 */
@SpringBootApplication
@EnableEurekaClient
public class GatewayWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayWebApplication.class, args);
    }


    @Bean(name = "ipKeyResolver")
    public KeyResolver useKeyResolver() {
        return new KeyResolver() {
            @Override
            public Mono<String> resolve(ServerWebExchange exchange) {
                String hostAddress = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
                System.out.println("ip:" + hostAddress);

                return Mono.just(hostAddress);
            }
        };
    }
}
