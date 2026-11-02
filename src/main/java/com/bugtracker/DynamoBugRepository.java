package com.bugtracker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;

public class DynamoBugRepository implements BugRepository {

    private final DynamoDbClient client;
    private final String table;

    public DynamoBugRepository(DynamoDbClient client, String table) {
        this.client = client;
        this.table = table;
    }

    @Override
    public void save(Bug bug) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("id", AttributeValue.fromS(bug.getId()));
        item.put("title", AttributeValue.fromS(bug.getTitle()));
        item.put("description", AttributeValue.fromS(bug.getDescription()));
        item.put("severity", AttributeValue.fromS(bug.getSeverity().name()));
        item.put("status", AttributeValue.fromS(bug.getStatus().name()));
        item.put("notified", AttributeValue.fromBool(bug.isNotified()));
        item.put("createdAt", AttributeValue.fromS(bug.getCreatedAt()));

        client.putItem(PutItemRequest.builder().tableName(table).item(item).build());
    }

    @Override
    public Optional<Bug> findById(String id) {
        Map<String, AttributeValue> item = client.getItem(GetItemRequest.builder()
                .tableName(table)
                .key(Map.of("id", AttributeValue.fromS(id)))
                .build()).item();

        if (item == null || item.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(fromItem(item));
    }

    @Override
    public List<Bug> findAll() {
        List<Bug> result = new ArrayList<>();
        client.scanPaginator(ScanRequest.builder().tableName(table).build())
                .items()
                .forEach(item -> result.add(fromItem(item)));
        return result;
    }

    private Bug fromItem(Map<String, AttributeValue> item) {
        return new Bug(
                item.get("id").s(),
                item.get("title").s(),
                item.get("description").s(),
                Bug.Severity.valueOf(item.get("severity").s()),
                Bug.Status.valueOf(item.get("status").s()),
                item.get("notified").bool(),
                item.get("createdAt").s());
    }
}
