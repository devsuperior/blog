# Serie Mensageria, Episodio 2: Fan-out com AWS SNS + SQS

Quatro microsservicos Spring Boot orquestrando fan-out de eventos via **AWS SNS + SQS**, rodando localmente com **LocalStack**. Quando um pagamento e processado, o `ms-billing` publica um evento de dominio em um topic SNS e dois consumidores independentes (`ms-notification` e `ms-fulfillment`) reagem em paralelo, cada um na sua propria fila.

## Projetos

| Projeto | Porta | Descricao |
|---|---|---|
| **ms-payment-ingestor** | `8081` | Recebe webhooks de pagamento e enfileira na `billing-queue` via `SqsTemplate` |
| **ms-billing** | `8082` | Consome `billing-queue`, processa (cambio + imposto), persiste no H2 e publica `PaymentProcessedEvent` no topic `payment-events` |
| **ms-notification** | `8083` | Consome `notification-queue` (assinante do topic) e empurra notificacoes para clientes via Server-Sent Events |
| **ms-fulfillment** | `8084` | Consome `fulfillment-queue` (assinante do topic) e libera produto/servico (log estruturado) |

## Topologia de mensageria

- 1 topic SNS: `payment-events`
- 3 filas SQS: `billing-queue`, `notification-queue`, `fulfillment-queue`
- 2 subscriptions SNS para SQS, ambas com `RawMessageDelivery=true` para que o `@SqsListener` desserialize o payload diretamente para o record `PaymentProcessedEvent` sem envelope SNS.

## Pre-requisitos

- Java 25
- Docker
- Token do LocalStack (crie uma conta gratuita em https://app.localstack.cloud/auth-tokens)

## Subindo o ambiente

```bash
# 1. Copie .env.example para .env e cole seu token
cp .env.example .env
# edite .env e troque o placeholder pelo token real

# 2. Exporte as variaveis de ambiente
export LOCALSTACK_AUTH_TOKEN=ls-seu-token-aqui
export AWS_ACCESS_KEY_ID=test
export AWS_SECRET_ACCESS_KEY=test

# 3. Suba o LocalStack (cria topic, filas e subscriptions automaticamente)
cd localstack && docker-compose up -d && cd ..

# 4. Confirme topology
aws --endpoint-url=http://localhost:4566 sns list-topics --region us-east-1
aws --endpoint-url=http://localhost:4566 sqs list-queues --region us-east-1
aws --endpoint-url=http://localhost:4566 sns list-subscriptions --region us-east-1
```

## Subindo os microsservicos

Cada um em um terminal separado, com o profile `local` ativo:

```bash
cd ms-payment-ingestor && SPRING_PROFILES_ACTIVE=local ./mvnw spring-boot:run
cd ms-billing           && SPRING_PROFILES_ACTIVE=local ./mvnw spring-boot:run
cd ms-notification      && SPRING_PROFILES_ACTIVE=local ./mvnw spring-boot:run
cd ms-fulfillment       && SPRING_PROFILES_ACTIVE=local ./mvnw spring-boot:run
```

## Teste fim a fim

### 1. Conectar no stream SSE do `ms-notification`

Em um terminal separado, antes de disparar o webhook:

```bash
curl -N http://localhost:8083/api/notifications/stream/pay_e2e_001
```

O stream agora e especifico para o `paymentId` informado: o `curl -N` so recebe eventos do pagamento `pay_e2e_001` e ignora os demais. A conexao fica aberta esperando eventos.

### 2. Disparar o webhook no `ms-payment-ingestor`

```bash
curl -X POST http://localhost:8081/api/payments/webhook \
  -H "Content-Type: application/json" \
  -d '{
    "paymentId": "pay_e2e_001",
    "amount": 299.90,
    "currency": "BRL",
    "status": "succeeded",
    "createdAt": "2026-04-29T10:30:00Z"
  }'
```

Resposta imediata: `{"status":"accepted"}`.

### 3. Verificar a propagacao

- **Terminal do `curl -N`** mostra um evento SSE:
  ```
  event:payment-processed
  data:{"paymentId":"pay_e2e_001","processedAmountUsd":60.13,"processedTaxUsd":2.19,"processedAt":"..."}
  ```
- **Terminal do `ms-fulfillment`** loga `Liberando produto/servico para pagamento pay_e2e_001, valor liquido aproximadamente 57 USD` (o valor exato varia conforme cotacao BRL/USD do dia).
- **H2 console** em http://localhost:8082/h2-console (JDBC URL `jdbc:h2:mem:billingdb`, user `sa`, sem senha):
  ```sql
  SELECT * FROM processed_payments;
  ```

### 4. Idempotencia

Repita o `curl` da etapa 2 com o mesmo `paymentId`. O `ms-billing` loga `Pagamento pay_e2e_001 ja foi processado, ignorando duplicata` e nao publica novo evento. Nada novo no terminal do `curl -N` nem no `ms-fulfillment`.

## Parando tudo

```bash
cd localstack && docker-compose down && cd ..
```
