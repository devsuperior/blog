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
bash scripts/new-article.sh
```

O script:

- solicita de forma interativa: `titulo`, `stack`, `quantidade de projetos`, `projetos` e `objetivo` de cada projeto
- gera automaticamente o `article-id` (slug) a partir do titulo informado
- cria a estrutura padrao do artigo
- pergunta no final se deseja criar as pastas opcionais `assets` e `docs`
- adiciona `.gitkeep` nas pastas que forem geradas
- atualiza automaticamente o indice de artigos neste README

## Indice de artigos

| article-id | titulo | stack | projetos |
| --- | --- | --- | --- |
| [`spring-batch-em-acao`](articles/spring-batch-em-acao/) | Spring Batch em acao: processamento de grandes lotes de dados | Java, Spring Boot, Spring Batch | `car-dealer` |
