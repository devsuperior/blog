# 📨 Série Mensageria — Episódio 1: Point-to-Point com AWS SQS

Dois microsserviços Spring Boot comunicando via **AWS SQS**, rodando localmente com **LocalStack**.

## 📦 Projetos

| Projeto | Porta | Descrição |
|---------|-------|-----------|
| **ms-payment-ingestor** | `8081` | Recebe webhooks de pagamento e enfileira na `billing-queue` via `SqsTemplate` |
| **ms-billing** | `8082` | Consome da `billing-queue` via `@SqsListener`, converte moeda, calcula imposto e persiste no H2 |

## 🏗️ Pré-requisitos

- Java 25
- Docker
- Token do LocalStack ([crie uma conta gratuita](https://app.localstack.cloud))

## 🚀 Subindo tudo junto

```bash
# 1. Exporte as variáveis
export LOCALSTACK_AUTH_TOKEN=ls_seu_token_aqui
export AWS_ACCESS_KEY_ID=test
export AWS_SECRET_ACCESS_KEY=test

# 2. Suba o LocalStack
docker-compose up -d

# 3. Confirme que a fila foi criada
aws --endpoint-url=http://localhost:4566 sqs list-queues --region us-east-1

# 4. Suba os microsserviços (cada um em um terminal)
cd ms-payment-ingestor && ./mvnw spring-boot:run
cd ms-billing && ./mvnw spring-boot:run
```

## 🔧 Subindo separado

### Somente o Ingestor (sem consumidor)

```bash
docker-compose up -d
cd ms-payment-ingestor && ./mvnw spring-boot:run
```

As mensagens serão enfileiradas e ficarão aguardando na fila. Suba o billing depois para processá-las.

### Somente o Billing (consumir fila pendente)

```bash
cd ms-billing && ./mvnw spring-boot:run
```

Ele vai consumir automaticamente tudo que estiver pendente na `billing-queue`.

## 🧪 Enviando um pagamento de teste

```bash
curl -X POST http://localhost:8081/api/payments/webhook \
  -H "Content-Type: application/json" \
  -d '{
    "paymentId": "pay_test_001",
    "amount": 299.90,
    "currency": "BRL",
    "status": "succeeded",
    "createdAt": "2026-04-12T10:30:00Z"
  }'
```

✅ Resposta esperada: `{"status":"accepted"}`

## 🔍 Inspecionando a fila

```bash
# Quantas mensagens estão na fila?
aws --endpoint-url=http://localhost:4566 sqs get-queue-attributes \
  --queue-url http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/billing-queue \
  --attribute-names ApproximateNumberOfMessages \
  --region us-east-1
```

## 🗄️ Verificando o banco (ms-billing)

Acesse o console H2 em [http://localhost:8082/h2-console](http://localhost:8082/h2-console):

| Campo | Valor |
|-------|-------|
| JDBC URL | `jdbc:h2:mem:billingdb` |
| User | `sa` |
| Password | *(vazio)* |

```sql
SELECT * FROM processed_payments;
```

## 🛑 Parando tudo

```bash
docker-compose down
```
