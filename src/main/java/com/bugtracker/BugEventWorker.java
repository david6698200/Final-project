package com.bugtracker;

import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

public class BugEventWorker implements Runnable {

    private final SqsClient sqs;
    private final String queueName;
    private final BugService service;

    public BugEventWorker(SqsClient sqs, String queueName, BugService service) {
        this.sqs = sqs;
        this.queueName = queueName;
        this.service = service;
    }

    @Override
    public void run() {
        String queueUrl = waitForQueue();
        if (queueUrl == null) {
            return;
        }
        System.out.println("Worker listening on queue: " + queueName);

        while (!Thread.currentThread().isInterrupted()) {
            try {
                var response = sqs.receiveMessage(ReceiveMessageRequest.builder()
                        .queueUrl(queueUrl)
                        .maxNumberOfMessages(10)
                        .waitTimeSeconds(5)
                        .build());

                for (Message message : response.messages()) {
                    handle(message.body());
                    sqs.deleteMessage(DeleteMessageRequest.builder()
                            .queueUrl(queueUrl)
                            .receiptHandle(message.receiptHandle())
                            .build());
                }
            } catch (Exception e) {
                System.err.println("Worker error: " + e.getMessage());
                if (!pause(2000)) {
                    return;
                }
            }
        }
    }

    private void handle(String bugId) {
        try {
            service.markNotified(bugId);
            System.out.println("Worker marked bug as notified: " + bugId);
        } catch (NotFoundException e) {
            System.err.println("Worker: bug not found, dropping message: " + bugId);
        }
    }

    private String waitForQueue() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                return sqs.getQueueUrl(
                        GetQueueUrlRequest.builder().queueName(queueName).build()).queueUrl();
            } catch (Exception e) {
                System.err.println("Worker waiting for queue '" + queueName + "': " + e.getMessage());
                if (!pause(2000)) {
                    return null;
                }
            }
        }
        return null;
    }

    private boolean pause(long millis) {
        try {
            Thread.sleep(millis);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}
