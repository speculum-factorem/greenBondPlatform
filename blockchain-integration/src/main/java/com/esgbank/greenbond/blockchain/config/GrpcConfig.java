package com.esgbank.greenbond.blockchain.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

/**
 * gRPC configuration
 * Note: gRPC services are auto-configured by grpc-spring-boot-starter
 * Services annotated with @GrpcService are automatically registered
 */
@Slf4j
@Configuration
public class GrpcConfig {
    // gRPC services are auto-configured by grpc-spring-boot-starter
    // No manual configuration needed when using @GrpcService annotation
}