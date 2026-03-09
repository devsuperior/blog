# dev-xp-ai

API Spring Boot para cadastro de desenvolvedores interessados em IA.

## Executar a aplicacao

```bash
./mvnw spring-boot:run
```

No Windows (PowerShell):

```powershell
.\mvnw.cmd spring-boot:run
```

Base URL local: `http://localhost:8080`

Header obrigatorio em todos os endpoints: `correlationId` (UUID)

## Exemplos de curl

### 1. Criar desenvolvedor

```bash
curl -X POST "http://localhost:8080/developers" \
  -H "Content-Type: application/json" \
  -H "correlationId: 2bb97789-cf91-4f3b-9ff5-9b4b5f4e7786" \
  -d '{
    "fullName": "Maria Fernanda Oliveira",
    "email": "maria.dev@email.com",
    "nickname": "maria.dev",
    "uf": "SP",
    "yearsOfExperience": 6,
    "primaryLanguage": "Java",
    "interestedInAi": true,
    "skills": ["Spring Boot", "JUnit"]
  }'
```

### 2. Listar todos os desenvolvedores

```bash
curl "http://localhost:8080/developers" \
  -H "correlationId: 2bb97789-cf91-4f3b-9ff5-9b4b5f4e7786"
```

### 3. Listar com filtros

```bash
curl "http://localhost:8080/developers?uf=SP&language=Java" \
  -H "correlationId: 2bb97789-cf91-4f3b-9ff5-9b4b5f4e7786"
```

### 4. Buscar por id

```bash
curl "http://localhost:8080/developers/1" \
  -H "correlationId: 2bb97789-cf91-4f3b-9ff5-9b4b5f4e7786"
```

### 5. Atualizar anos de experiencia

```bash
curl -X PUT "http://localhost:8080/developers/1/experience" \
  -H "Content-Type: application/json" \
  -H "correlationId: 2bb97789-cf91-4f3b-9ff5-9b4b5f4e7786" \
  -d '{
    "yearsOfExperience": 8
  }'
```
