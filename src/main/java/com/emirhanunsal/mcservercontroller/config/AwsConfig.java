package com.emirhanunsal.mcservercontroller.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.lambda.LambdaClient;

@Configuration
public class AwsConfig {
    @Bean
    LambdaClient lambdaClient(AwsProperties properties) {
        return LambdaClient.builder().region(Region.of(properties.region()))
                .credentialsProvider(DefaultCredentialsProvider.builder().build()).build();
    }
}
