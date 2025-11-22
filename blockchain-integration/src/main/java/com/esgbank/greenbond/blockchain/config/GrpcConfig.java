package com.esgbank.greenbond.blockchain.config;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class GrpcConfig {

    @Value("${app.grpc.server.port:9090}")
    private int grpcServerPort;

    private final BondTokenizationServiceImpl bondTokenizationService;

    @Bean
    public Server grpcServer() throws IOException {
        log.info("Starting gRPC server on port: {}", grpcServerPort);
        Server server = ServerBuilder.forPort(grpcServerPort)
                .addService(bondTokenizationService)
                .build()
                .start();

        log.info("gRPC server started successfully on port: {}", grpcServerPort);

        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down gRPC server");
            server.shutdown();
            log.info("gRPC server shut down successfully");
        }));

        return server;
    }
}