package com.bank.gatewayserver;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;

@SpringBootApplication
public class GatewayServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatewayServerApplication.class, args);
    }

    @Bean
    public RouteLocator bankRouteConfig(RouteLocatorBuilder routeLocatorBuilder) {
        return routeLocatorBuilder.routes()
                .route(
                        p-> p
                            .path("/bank/accounts/**")
                            .filters(f-> f.rewritePath("/bank/accounts/(?<segment>.*)","/${segment}")
                                    .addResponseHeader("X-Response-Time", LocalDateTime.now().toString())
                                    .circuitBreaker(config -> config.setName("accountsCircuitBreaker")
                                    .setFallbackUri("forward:/contactSupport"))
                            )
                            .uri("lb://ACCOUNTS"))
                .route(
                        p-> p
                            .path("/bank/loans/**")
                            .filters(f-> f.rewritePath("/bank/loans/(?<segment>.*)","/${segment}")
                                .addResponseHeader("X-Response-Time", LocalDateTime.now().toString())
                                .retry(retryConfig -> retryConfig
                                        .setRetries(3)
                                        .setMethods(HttpMethod.GET).setBackoff(Duration.ofMillis(100), Duration.ofMillis(1000), 2, true)))
                            .uri("lb://LOANS"))
                .route(
                        p-> p
                            .path("/bank/cards/**")
                            .filters(f-> f.rewritePath("/bank/cards/(?<segment>.*)","/${segment}")
                                    .addResponseHeader("X-Response-Time", LocalDateTime.now().toString())
                                    .requestRateLimiter(config -> config.setRateLimiter(redisRateLimiter()).setKeyResolver(keyResolver()))
                            )
                            .uri("lb://CARDS")).build();
    }

    // Retry
    @Bean
    public Customizer<ReactiveResilience4JCircuitBreakerFactory> defaultCustomizer() {
        return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
                .circuitBreakerConfig(CircuitBreakerConfig.ofDefaults())
                .timeLimiterConfig(TimeLimiterConfig.custom().timeoutDuration(Duration.ofSeconds(4)).build()).build());
    }

    @Bean
    public RedisRateLimiter redisRateLimiter() {
        // replenishRate : 초당 재충전 되는 토큰 수
        // burstCapacity : 버킷이 가질 수 있는 최대 토큰 수
        // requestedTokens : 요청이 소비하는 토큰 수
        return new RedisRateLimiter(1,1,1);
    }

    @Bean
    public KeyResolver keyResolver() {
        return exchange -> Mono.justOrEmpty(exchange.getRequest().getQueryParams().getFirst("user")) // 쿼리 파라미터 중 user 파라미터의 첫 번째 값을 가져옴
                .defaultIfEmpty("anonymous"); // user 값이 없으면 anonymous
    }
}
