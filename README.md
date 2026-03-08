# devsuperior-blog

Monorepo oficial das POCs dos artigos tecnicos do blog.
Cada artigo tecnico com codigo pratico deve ter sua pasta propria dentro de `articles/`.

## Convencoes

- `article-id`: `slug-do-artigo` (ex: `spring-batch-em-acao`)
- `project-name`: nome curto e descritivo (ex: `car-dealer`)
- Cada artigo deve ter um `README.md` com:
  - titulo do artigo
  - stack
  - lista de projetos e como executar

## Como adicionar um novo artigo

Use o script (Git Bash / Linux):

```bash
bash scripts/new-article.sh \
  --id introducao-ao-kafka \
  --title "Introducao ao Kafka" \
  --project kafka-api \
  --project kafka-consumer \
  --stack "Java, Spring Boot, Kafka"
```

O script:

- cria a estrutura padrao do artigo
- adiciona `.gitkeep` nas pastas padrao
- atualiza automaticamente o indice de artigos neste README

## Indice de artigos

| article-id | titulo | stack | projetos |
| --- | --- | --- | --- |
| [`spring-batch-em-acao`](articles/spring-batch-em-acao/) | Spring Batch em acao: processamento de grandes lotes de dados | Java, Spring Boot, Spring Batch | `car-dealer` |
