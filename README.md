# devsuperior-blog

Monorepo oficial das POCs dos artigos tecnicos do blog.
Cada artigo tecnico com codigo pratico deve ter sua pasta propria dentro de `articles/`.

## Convencoes

- `article-id`: `YYYY-MM-slug-do-artigo` (ex: `2026-02-spring-batch-em-acao`)
- `project-name`: nome curto e descritivo (ex: `car-dealer`)
- Cada artigo deve ter um `README.md` com:
  - titulo do artigo
  - link publicado
  - stack
  - lista de projetos e como executar

Um artigo pode estar `em preparacao` ou `publicado`.

## Como adicionar um novo artigo

Use o script (Git Bash / Linux):

```bash
bash scripts/new-article.sh \
  --id 2026-03-introducao-ao-kafka \
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
| [`2026-02-spring-batch-em-acao`](articles/2026-02-spring-batch-em-acao/) | Spring Batch em acao: processamento de grandes lotes de dados | Java, Spring Boot, Spring Batch | `car-dealer` |
