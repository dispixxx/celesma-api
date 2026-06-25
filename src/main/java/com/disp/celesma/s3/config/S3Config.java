package com.disp.celesma.s3.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Configuration
public class S3Config {

    @Value("${storage.access-key}")
    private String storageAccessKey;

    @Value("${storage.secret-key}")
    private String storageSecretKey;

    @Value("${storage.region}")
    private String storageRegion;

    @Value("${storage.endpoint}")
    private String storageEndpoint;

    @Bean
    public S3Client s3Client() {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(
                storageAccessKey,
                storageSecretKey
        );

        return S3Client.builder()
                .httpClient(ApacheHttpClient.create())
                .region(Region.of(storageRegion)) // Регион Yandex Cloud
                .endpointOverride(URI.create(storageEndpoint)) // Эндпоинт S3
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }
}