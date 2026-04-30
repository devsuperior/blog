#!/bin/bash
set -e

echo "==> Criando billing-queue"
awslocal sqs create-queue --queue-name billing-queue

echo "==> Criando notification-queue"
awslocal sqs create-queue --queue-name notification-queue

echo "==> Criando fulfillment-queue"
awslocal sqs create-queue --queue-name fulfillment-queue

echo "==> Criando topic payment-events"
TOPIC_ARN=$(awslocal sns create-topic --name payment-events --query 'TopicArn' --output text)
echo "    TopicArn=$TOPIC_ARN"

NOTIFICATION_QUEUE_ARN=$(awslocal sqs get-queue-attributes \
  --queue-url http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/notification-queue \
  --attribute-names QueueArn \
  --query 'Attributes.QueueArn' --output text)

FULFILLMENT_QUEUE_ARN=$(awslocal sqs get-queue-attributes \
  --queue-url http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/fulfillment-queue \
  --attribute-names QueueArn \
  --query 'Attributes.QueueArn' --output text)

echo "==> Assinando notification-queue no topic (RawMessageDelivery=true)"
awslocal sns subscribe \
  --topic-arn "$TOPIC_ARN" \
  --protocol sqs \
  --notification-endpoint "$NOTIFICATION_QUEUE_ARN" \
  --attributes '{"RawMessageDelivery":"true"}'

echo "==> Assinando fulfillment-queue no topic (RawMessageDelivery=true)"
awslocal sns subscribe \
  --topic-arn "$TOPIC_ARN" \
  --protocol sqs \
  --notification-endpoint "$FULFILLMENT_QUEUE_ARN" \
  --attributes '{"RawMessageDelivery":"true"}'

echo "==> Topologia pronta:"
awslocal sqs list-queues
awslocal sns list-topics
awslocal sns list-subscriptions
