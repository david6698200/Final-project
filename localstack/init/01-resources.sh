#!/bin/bash
set -e

echo "Creating DynamoDB table: bugs"
awslocal dynamodb create-table \
  --table-name bugs \
  --attribute-definitions AttributeName=id,AttributeType=S \
  --key-schema AttributeName=id,KeyType=HASH \
  --billing-mode PAY_PER_REQUEST

echo "Creating SQS queue: bug-events"
awslocal sqs create-queue --queue-name bug-events

echo "LocalStack resources ready"
