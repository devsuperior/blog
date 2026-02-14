#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd -P)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd -P)"
TEMPLATE_FILE="${REPO_ROOT}/templates/article/README.template.md"
ARTICLES_DIR="${REPO_ROOT}/articles"
ROOT_README_FILE="${REPO_ROOT}/README.md"

INDEX_HEADER='| article-id | titulo | stack | projetos |'
INDEX_SEPARATOR='| --- | --- | --- | --- |'

usage() {
  cat <<'EOF'
Usage:
  bash scripts/new-article.sh --id YYYY-MM-slug --title "Titulo" [options]

Required:
  --id         Article id no formato YYYY-MM-slug
  --title      Titulo do artigo

Optional:
  --project    Nome de projeto (repita a flag para mais de um; default: app)
  --stack      Stack principal (default: preencher)
  --status     em preparacao | publicado (default: em preparacao)
  -h, --help   Exibe ajuda

Example:
  bash scripts/new-article.sh \
    --id 2026-03-introducao-ao-kafka \
    --title "Introducao ao Kafka" \
    --project kafka-api \
    --project kafka-consumer \
    --stack "Java, Spring Boot, Kafka"
EOF
}

escape_markdown_cell() {
  local value="$1"
  value="${value//|/\\|}"
  printf '%s' "$value"
}

ARTICLE_ID=""
TITLE=""
STACK="preencher"
STATUS="em preparacao"
PROJECT_NAMES=()

while [[ $# -gt 0 ]]; do
  case "$1" in
    --id)
      ARTICLE_ID="${2:-}"
      shift 2
      ;;
    --title)
      TITLE="${2:-}"
      shift 2
      ;;
    --project)
      PROJECT_NAMES+=("${2:-}")
      shift 2
      ;;
    --stack)
      STACK="${2:-}"
      shift 2
      ;;
    --status)
      STATUS="${2:-}"
      shift 2
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      echo "Parametro invalido: $1" >&2
      usage
      exit 1
      ;;
  esac
done

if [[ -z "${ARTICLE_ID}" || -z "${TITLE}" ]]; then
  echo "Erro: --id e --title sao obrigatorios." >&2
  usage
  exit 1
fi

if [[ ! "${ARTICLE_ID}" =~ ^[0-9]{4}-(0[1-9]|1[0-2])-[a-z0-9-]+$ ]]; then
  echo "Erro: --id deve seguir o formato YYYY-MM-slug (minusculo)." >&2
  exit 1
fi

if [[ ! "${STATUS}" =~ ^(em\ preparacao|publicado)$ ]]; then
  echo "Erro: --status deve ser 'em preparacao' ou 'publicado'." >&2
  exit 1
fi

if [[ ${#PROJECT_NAMES[@]} -eq 0 ]]; then
  PROJECT_NAMES=("app")
fi

if [[ ! -f "${TEMPLATE_FILE}" ]]; then
  echo "Erro: template nao encontrado em ${TEMPLATE_FILE}" >&2
  exit 1
fi

ARTICLE_DIR="${ARTICLES_DIR}/${ARTICLE_ID}"
README_FILE="${ARTICLE_DIR}/README.md"
DATE_REF="${ARTICLE_ID:0:7}"

if [[ -d "${ARTICLE_DIR}" ]]; then
  echo "Erro: artigo ja existe: ${ARTICLE_DIR}" >&2
  exit 1
fi

if [[ ! -f "${ROOT_README_FILE}" ]]; then
  echo "Erro: README da raiz nao encontrado em ${ROOT_README_FILE}" >&2
  exit 1
fi

if grep -Fq "| \`${ARTICLE_ID}\` |" "${ROOT_README_FILE}" || \
   grep -Fq "(articles/${ARTICLE_ID}/)" "${ROOT_README_FILE}"; then
  echo "Erro: o indice em README.md ja possui o artigo ${ARTICLE_ID}." >&2
  exit 1
fi

unique_projects=()
for project in "${PROJECT_NAMES[@]}"; do
  if [[ -z "${project}" ]]; then
    echo "Erro: --project nao pode ser vazio." >&2
    exit 1
  fi

  if [[ ! "${project}" =~ ^[a-z0-9-]+$ ]]; then
    echo "Erro: --project deve conter apenas letras minusculas, numeros e hifen." >&2
    exit 1
  fi

  already_added=0
  for existing in "${unique_projects[@]}"; do
    if [[ "${existing}" == "${project}" ]]; then
      already_added=1
      break
    fi
  done

  if [[ ${already_added} -eq 0 ]]; then
    unique_projects+=("${project}")
  fi
done

PROJECT_NAMES=("${unique_projects[@]}")

mkdir -p "${ARTICLE_DIR}/projects" "${ARTICLE_DIR}/assets" "${ARTICLE_DIR}/docs"
touch "${ARTICLE_DIR}/projects/.gitkeep"
touch "${ARTICLE_DIR}/assets/.gitkeep"
touch "${ARTICLE_DIR}/docs/.gitkeep"

for project in "${PROJECT_NAMES[@]}"; do
  mkdir -p "${ARTICLE_DIR}/projects/${project}"
  touch "${ARTICLE_DIR}/projects/${project}/.gitkeep"
done

projects_section_file="$(mktemp)"
for project in "${PROJECT_NAMES[@]}"; do
  cat <<EOF >> "${projects_section_file}"
### ${project}

- Caminho: \`projects/${project}\`
- Objetivo: <resumo-da-poc>

#### Execucao local

\`\`\`bash
cd articles/${ARTICLE_ID}/projects/${project}
# comando de execucao
\`\`\`

#### Testes

\`\`\`bash
cd articles/${ARTICLE_ID}/projects/${project}
# comando de teste
\`\`\`

EOF
done

tmp_readme_file="$(mktemp)"
while IFS= read -r line || [[ -n "${line}" ]]; do
  if [[ "${line}" == "<projects-section>" ]]; then
    cat "${projects_section_file}" >> "${tmp_readme_file}"
    continue
  fi

  line="${line//<article-id>/${ARTICLE_ID}}"
  line="${line//<titulo-do-artigo>/${TITLE}}"
  line="${line//<status>/${STATUS}}"
  line="${line//<YYYY-MM>/${DATE_REF}}"
  line="${line//<tecnologias-principais>/${STACK}}"
  printf '%s\n' "${line}" >> "${tmp_readme_file}"
done < "${TEMPLATE_FILE}"

mv "${tmp_readme_file}" "${README_FILE}"
rm -f "${projects_section_file}"

projects_table_cell=""
for project in "${PROJECT_NAMES[@]}"; do
  if [[ -n "${projects_table_cell}" ]]; then
    projects_table_cell+=", "
  fi
  projects_table_cell+="\`${project}\`"
done

article_id_cell="[\`${ARTICLE_ID}\`](articles/${ARTICLE_ID}/)"
title_cell="$(escape_markdown_cell "${TITLE}")"
stack_cell="$(escape_markdown_cell "${STACK}")"
row="| ${article_id_cell} | ${title_cell} | ${stack_cell} | ${projects_table_cell} |"

tmp_root_readme="$(mktemp)"
awk -v header="${INDEX_HEADER}" -v separator="${INDEX_SEPARATOR}" -v new_row="${row}" '
BEGIN {
  found=0
  in_table=0
  inserted=0
}
{
  if ($0 == header) {
    found=1
    in_table=1
    print $0
    next
  }

  if (in_table == 1) {
    if ($0 ~ /^\|/) {
      print $0
      next
    }

    if (inserted == 0) {
      print new_row
      inserted=1
    }
    in_table=0
  }

  print $0
}
END {
  if (found == 0) {
    print ""
    print "## Indice de artigos"
    print ""
    print header
    print separator
    print new_row
  } else if (in_table == 1 && inserted == 0) {
    print new_row
  }
}
' "${ROOT_README_FILE}" > "${tmp_root_readme}"
mv "${tmp_root_readme}" "${ROOT_README_FILE}"

cat <<EOF
Artigo criado com sucesso:
- ${ARTICLE_DIR}
- ${README_FILE}

Projetos criados:
$(for project in "${PROJECT_NAMES[@]}"; do printf -- "- %s\n" "${ARTICLE_DIR}/projects/${project}"; done)

Proximo passo:
1. Colocar codigo em articles/${ARTICLE_ID}/projects/<project-name>
2. Completar objetivos/comandos em ${README_FILE}
EOF
