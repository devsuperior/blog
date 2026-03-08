#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd -P)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd -P)"
ARTICLES_DIR="${REPO_ROOT}/articles"
ROOT_README_FILE="${REPO_ROOT}/README.md"

INDEX_HEADER='| article-id | titulo | stack | projetos |'
INDEX_SEPARATOR='| --- | --- | --- | --- |'

escape_markdown_cell() {
  local value="$1"
  value="${value//|/\\|}"
  printf '%s' "$value"
}

slugify() {
  local value="$1"
  local slug

  slug="$(printf '%s' "${value}" \
    | tr '[:upper:]' '[:lower:]' \
    | sed -E 's/[^a-z0-9]+/-/g; s/-+/-/g; s/^-+//; s/-+$//')"

  printf '%s' "${slug}"
}

if [[ $# -gt 0 ]]; then
  echo "Erro: este script agora e interativo e nao aceita parametros via CLI." >&2
  echo "Uso: bash scripts/new-article.sh" >&2
  exit 1
fi

ARTICLE_ID=""
TITLE=""
STACK="preencher"
PROJECT_NAMES=()
PROJECT_OBJECTIVES=()

if [[ ! -f "${ROOT_README_FILE}" ]]; then
  echo "Erro: README da raiz nao encontrado em ${ROOT_README_FILE}" >&2
  exit 1
fi

echo "Criacao de novo artigo (modo interativo)"

while true; do
  read -r -p "Titulo do artigo: " TITLE
  if [[ -z "${TITLE}" ]]; then
    echo "Erro: titulo e obrigatorio."
    continue
  fi

  ARTICLE_ID="$(slugify "${TITLE}")"
  if [[ -z "${ARTICLE_ID}" ]]; then
    echo "Erro: nao foi possivel gerar slug a partir do titulo informado."
    continue
  fi

  ARTICLE_DIR="${ARTICLES_DIR}/${ARTICLE_ID}"
  README_FILE="${ARTICLE_DIR}/README.md"

  if [[ -d "${ARTICLE_DIR}" ]]; then
    echo "Erro: artigo ja existe: ${ARTICLE_DIR}"
    continue
  fi

  if grep -Fq "| \`${ARTICLE_ID}\` |" "${ROOT_README_FILE}" || \
     grep -Fq "(articles/${ARTICLE_ID}/)" "${ROOT_README_FILE}"; then
    echo "Erro: o slug gerado (${ARTICLE_ID}) ja existe no indice."
    echo "Dica: informe outro titulo para gerar um slug diferente."
    continue
  fi

  break
done

echo "Slug gerado automaticamente: ${ARTICLE_ID}"

read -r -p "Stack principal [preencher]: " STACK_INPUT
if [[ -n "${STACK_INPUT}" ]]; then
  STACK="${STACK_INPUT}"
fi

echo "Informe os projetos (deixe vazio para finalizar):"
while true; do
  read -r -p "Projeto: " project_raw

  if [[ -z "${project_raw}" ]]; then
    break
  fi

  project="$(slugify "${project_raw}")"
  if [[ -z "${project}" ]]; then
    echo "Erro: nome de projeto invalido."
    continue
  fi

  if [[ "${project}" != "${project_raw}" ]]; then
    echo "Nome de projeto normalizado para: ${project}"
  fi

  already_added=0
  for existing in "${PROJECT_NAMES[@]}"; do
    if [[ "${existing}" == "${project}" ]]; then
      already_added=1
      break
    fi
  done

  if [[ ${already_added} -eq 1 ]]; then
    echo "Aviso: projeto repetido ignorado: ${project}"
    continue
  fi

  while true; do
    read -r -p "Objetivo do projeto ${project}: " objective
    if [[ -n "${objective//[[:space:]]/}" ]]; then
      break
    fi
    echo "Erro: objetivo do projeto e obrigatorio."
  done

  PROJECT_NAMES+=("${project}")
  PROJECT_OBJECTIVES+=("${objective}")
done

if [[ ${#PROJECT_NAMES[@]} -eq 0 ]]; then
  PROJECT_NAMES=("app")
  echo "Nenhum projeto informado. Usando projeto padrao: app"

  while true; do
    read -r -p "Objetivo do projeto app: " default_objective
    if [[ -n "${default_objective//[[:space:]]/}" ]]; then
      break
    fi
    echo "Erro: objetivo do projeto e obrigatorio."
  done

  PROJECT_OBJECTIVES=("${default_objective}")
fi

mkdir -p "${ARTICLE_DIR}/projects" "${ARTICLE_DIR}/assets" "${ARTICLE_DIR}/docs"
touch "${ARTICLE_DIR}/projects/.gitkeep"
touch "${ARTICLE_DIR}/assets/.gitkeep"
touch "${ARTICLE_DIR}/docs/.gitkeep"

for project in "${PROJECT_NAMES[@]}"; do
  mkdir -p "${ARTICLE_DIR}/projects/${project}"
  touch "${ARTICLE_DIR}/projects/${project}/.gitkeep"
done

projects_section_file="$(mktemp)"
for i in "${!PROJECT_NAMES[@]}"; do
  project="${PROJECT_NAMES[$i]}"
  objective="${PROJECT_OBJECTIVES[$i]}"

  cat <<EOF >> "${projects_section_file}"
### ${project}

- Caminho: \`projects/${project}\`
- Objetivo: ${objective}

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

cat > "${README_FILE}" <<EOF
# ${ARTICLE_ID}

## Metadados

- Titulo: ${TITLE}
- Stack: ${STACK}

## Projetos

EOF
cat "${projects_section_file}" >> "${README_FILE}"
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
