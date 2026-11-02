package com.bugtracker;

import java.net.URI;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.sqs.SqsClient;

public final class AwsConfig {

    public static final String ENDPOINT =
            System.getenv().getOrDefault("AWS_ENDPOINT", "http://localhost:4566");
    public static final String BUGS_TABLE =
            System.getenv().getOrDefault("BUGS_TABLE", "bugs");
    public static final String QUEUE_NAME =
            System.getenv().getOrDefault("QUEUE_NAME", "bug-events");

    private static final StaticCredentialsProvider CREDENTIALS =
            StaticCredentialsProvider.create(AwsBasicCredentials.create("test", "test"));

    private AwsConfig() {}

    public static DynamoDbClient dynamoDb() {
        return DynamoDbClient.builder()
                .endpointOverride(URI.create(ENDPOINT))
                .region(Region.US_EAST_1)
                .credentialsProvider(CREDENTIALS)
                .httpClient(UrlConnectionHttpClient.create())
                .build();
    }

    public static SqsClient sqs() {
        return SqsClient.builder()
                .endpointOverride(URI.create(ENDPOINT))
                .region(Region.US_EAST_1)
                .credentialsProvider(CREDENTIALS)
                .httpClient(UrlConnectionHttpClient.create())
                .build();
    }
}
