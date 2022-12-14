package com.alex.vis.orderservice;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.sleuth.instrument.async.TraceableExecutorService;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients
@RequiredArgsConstructor
public class OrderServiceApplication {
     //Getting from spring context
    private final BeanFactory beanFactory;

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }

    //Этот бин нам нужен потому что в этом случае не работает наш  token relay и jwt не передается
    //по цепочке из OrderService в Inventory service, потому что мы вызываем его через Feign а
    //Добавляются секьюрити  депенднеси в помник
//    @Bean
//    public RequestInterceptor requestTokenBearerInterceptor() {
//        return new RequestInterceptor() {
//            @Override
//            public void apply(RequestTemplate requestTemplate) {
//                HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
//                        .getRequestAttributes()).getRequest();
//
//                System.out.println("In the request interceptor");
//
//                System.out.println(SecurityContextHolder.getContext());
//                System.out.println(SecurityContextHolder.getContext().getAuthentication());
//
//                JwtAuthenticationToken token = (JwtAuthenticationToken) SecurityContextHolder
//                        .getContext().getAuthentication();
//
//                System.out.println("Token header: " + request.getHeader(HttpHeaders.AUTHORIZATION));
//                requestTemplate.header("Authorization", request.getHeader(HttpHeaders.AUTHORIZATION));
////                requestTemplate.header("Authorization", "Bearer" + token.getToken().getTokenValue());
//            }
//        };
//    }

    //TODO Find a way to send token with feign request
    @Bean
    public RequestInterceptor requestTokenBearerInterceptor() {
        return requestTemplate -> {
            JwtAuthenticationToken token = (JwtAuthenticationToken) SecurityContextHolder.getContext()
                    .getAuthentication();

            System.out.println(token.getToken().getTokenValue());

            requestTemplate.header("Authorization", "Bearer " + token.getToken().getTokenValue());
        };
    }

    //Not change name/ We will use it inside orderController
    //It`s needed for pass trace id to another thread
    @Bean
    public ExecutorService traceableExecutorService() {
        ExecutorService executorService = Executors.newCachedThreadPool();
        return new TraceableExecutorService(beanFactory, executorService);
    }
}
