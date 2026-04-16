# desacoplando-sistemas-com-mensageria-no-aws-sqs

## Metadados

- Titulo: Desacoplando sistemas com mensageria no AWS SQS
- Stack: Java, Spring Boot, AWS, SQS

## Projetos

### localstack

- Caminho: `projects/localstack`
- Objetivo: Infra local para simular AWS SQS

#### Execucao local

```bash
cd articles/desacoplando-sistemas-com-mensageria-no-aws-sqs/projects/localstack
# comando de execucao
```

#### Testes

```bash
cd articles/desacoplando-sistemas-com-mensageria-no-aws-sqs/projects/localstack
# comando de teste
```

### ms-payment-ingestor

- Caminho: `projects/ms-payment-ingestor`
- Objetivo: Serviço responsável por receber as requisições do gateway de pagamentos

#### Execucao local

```bash
cd articles/desacoplando-sistemas-com-mensageria-no-aws-sqs/projects/ms-payment-ingestor
# comando de execucao
```

#### Testes

```bash
cd articles/desacoplando-sistemas-com-mensageria-no-aws-sqs/projects/ms-payment-ingestor
# comando de teste
```

### ms-billing

- Caminho: `projects/ms-billing`
- Objetivo: Serviço responsável pelo faturamento do pedido no sistema

#### Execucao local

```bash
cd articles/desacoplando-sistemas-com-mensageria-no-aws-sqs/projects/ms-billing
# comando de execucao
```

#### Testes

```bash
cd articles/desacoplando-sistemas-com-mensageria-no-aws-sqs/projects/ms-billing
# comando de teste
```

