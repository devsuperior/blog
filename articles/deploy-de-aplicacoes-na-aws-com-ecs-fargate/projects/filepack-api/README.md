# FilePack API

API Spring Boot para empacotamento e criptografia de múltiplos arquivos em um único arquivo ZIP protegido por senha.

## 📋 Descrição

FilePack API é uma aplicação que recebe múltiplos arquivos via upload e gera um arquivo ZIP criptografado, protegido com a senha fornecida pelo usuário. Ideal para cenários onde é necessário:

- Agrupar múltiplos arquivos em um único pacote
- Proteger arquivos sensíveis com criptografia
- Facilitar o download de múltiplos documentos simultaneamente
- Garantir segurança no transporte de dados

## 🚀 Como Executar a Aplicação

### Pré-requisitos
- Java 25 ou superior
- Maven 3.6+
- GitBash

### Compilar e Executar

```bash
# Compilar o projeto
./mvnw clean package

# Executar a aplicação
./mvnw spring-boot:run
```

A aplicação estará disponível em: `$SERVICE_URL`

## 🔧 Endpoint da API

**URL:** `POST /api/filepack`

**Parâmetros:**
- `files` (multipart/form-data): Arquivos para empacotar (múltiplos)
- `password` (string): Senha para criptografar o ZIP

**Resposta:**
- Arquivo ZIP criptografado para download
- Content-Type: `application/zip`

## 📝 Exemplos de Uso com cURL

> **Nota:** Os arquivos de exemplo estão localizados na pasta `data/` na raiz do projeto.

### Configurar URL do Serviço

Antes de executar os testes, defina a variável `SERVICE_URL` de acordo com seu ambiente:

**Executando localmente:**
```bash
export SERVICE_URL="http://localhost:8080"
```

**Executando na AWS (substitua pelo endereço do seu ALB):**
```bash
export SERVICE_URL="http://seu-alb-123456.us-east-1.elb.amazonaws.com"
```

---

### Exemplo 0: Validação Básica (1 arquivo)

**Quando usar:** Validação inicial, verificar se a API está funcionando, testes de conectividade

**Tamanho do upload:** ~170 KB

```bash
curl -X POST $SERVICE_URL/api/filepack \
  -F "files=@data/infrastructure_config.xml" \
  -F "password=teste123" \
  --output validation_pack.zip
```

## 🔓 Como Descompactar o ZIP Resultante

## No Windows com GitBash e 7zip

```bash
"C:\Program Files\7-Zip\7z.exe" x validation_pack.zip -pteste123 -oextracted/
```

### No Linux/Mac

```bash
# Usando unzip
unzip -P teste123 validation_pack.zip

# Ou extrair para um diretório específico
unzip -P teste123 validation_pack.zip -d extracted/
```

**Nota:** A senha usada para descompactar deve ser a mesma fornecida no parâmetro `password` durante a criação do ZIP.

## 🔒 Segurança

- O ZIP gerado utiliza criptografia AES
- As senhas não são armazenadas no servidor
- Cada requisição gera um novo arquivo ZIP
- Recomenda-se usar senhas fortes (mínimo 8 caracteres, com letras, números e símbolos)

## 📄 Licença

Este projeto foi desenvolvido para fins educacionais como parte do blog DevSuperior.
