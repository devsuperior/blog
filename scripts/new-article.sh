#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd -P)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd -P)"
TEMPLATE_FILE="${REPO_ROOT}/templates/article/README.template.md"
ARTICLES_DIR="${REPO_ROOT}/articles"
ROOT_README_FILE="${REPO_ROOT}/README.md"

INDEX_HEADER='| article-id | titulo | stack | projetos |'
INDEX_SEPARATOR='| --- | --- | --- | --- |'

escape_markdown_cell() {
  local value="$1"
  value="${value//|/\\|}"
  printf '%s' "$value"
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

if [[ ! -f "${TEMPLATE_FILE}" ]]; then
  echo "Erro: template nao encontrado em ${TEMPLATE_FILE}" >&2
  exit 1
fi

if [[ ! -f "${ROOT_README_FILE}" ]]; then
  echo "Erro: README da raiz nao encontrado em ${ROOT_README_FILE}" >&2
  exit 1
fi

echo "Criacao de novo artigo (modo interativo)"

while true; do
  read -r -p "Article id (slug-do-artigo): " ARTICLE_ID

  if [[ -z "${ARTICLE_ID}" ]]; then
    echo "Erro: article id e obrigatorio."
    continue
  fi

  if [[ ! "${ARTICLE_ID}" =~ ^[a-z0-9-]+$ ]]; then
    echo "Erro: article id deve conter apenas letras minusculas, numeros e hifen."
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
    echo "Erro: o indice em README.md ja possui o artigo ${ARTICLE_ID}."
    continue
  fi

  break
done

while true; do
  read -r -p "Titulo do artigo: " TITLE
  if [[ -n "${TITLE}" ]]; then
    break
  fi
  echo "Erro: titulo e obrigatorio."
done

read -r -p "Stack principal [preencher]: " STACK_INPUT
if [[ -n "${STACK_INPUT}" ]]; then
  STACK="${STACK_INPUT}"
fi

echo "Informe os projetos (deixe vazio para finalizar):"
while true; do
  read -r -p "Projeto: " project

  if [[ -z "${project}" ]]; then
    break
  fi

  if [[ ! "${project}" =~ ^[a-z0-9-]+$ ]]; then
    echo "Erro: projeto deve conter apenas letras minusculas, numeros e hifen."
    continue
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

  PROJECT_NAMES+=("${project}")
done

if [[ ${#PROJECT_NAMES[@]} -eq 0 ]]; then
  PROJECT_NAMES=("app")
  echo "Nenhum projeto informado. Usando projeto padrao: app"
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
