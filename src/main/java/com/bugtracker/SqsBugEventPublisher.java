package com.bugtracker;

import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

public class SqsBugEventPublisher implements BugEventPublisher {

    private final SqsClient sqs;
    private final String queueName;
    private volatile String queueUrl;

    public SqsBugEventPublisher(SqsClient sqs, String queueName) {
        this.sqs = sqs;
        this.queueName = queueName;
    }

    @Override
    public void publishBugCreated(String bugId) {
        sqs.sendMessage(SendMessageRequest.builder()
                .queueUrl(queueUrl())
                .messageBody(bugId)
                .build());
    }

    private String queueUrl() {
        if (queueUrl == null) {
            queueUrl = sqs.getQueueUrl(
                    GetQueueUrlRequest.builder().queueName(queueName).build()).queueUrl();
        }
        return queueUrl;
    }
}
