#!/bin/bash
echo "Criando fila billing-queue no LocalStack..."
awslocal sqs create-queue --queue-name billing-queue
echo "Fila criada com sucesso!"
awslocal sqs list-queues
