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

ask_yes_no() {
  local prompt="$1"
  local default_answer="$2"
  local answer=""

  while true; do
    if [[ "${default_answer}" == "s" ]]; then
      read -r -p "${prompt} [S/n]: " answer
      answer="${answer:-s}"
    else
      read -r -p "${prompt} [s/N]: " answer
      answer="${answer:-n}"
    fi

    answer="$(printf '%s' "${answer}" | tr '[:upper:]' '[:lower:]')"
    case "${answer}" in
      s|sim|y|yes)
        return 0
        ;;
      n|nao|no)
        return 1
        ;;
      *)
        echo "Erro: responda com s ou n."
        ;;
    esac
  done
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
PROJECT_COUNT=0
CREATE_ASSETS=0
CREATE_DOCS=0

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

while true; do
  read -r -p "Quantidade de projetos do artigo: " project_count_raw

  if [[ ! "${project_count_raw}" =~ ^[0-9]+$ ]]; then
    echo "Erro: informe um numero inteiro."
    continue
  fi

  if [[ "${project_count_raw}" -lt 1 ]]; then
    echo "Erro: a quantidade de projetos deve ser pelo menos 1."
    continue
  fi

  PROJECT_COUNT="${project_count_raw}"
  break
done

for ((i=1; i<=PROJECT_COUNT; i++)); do
  while true; do
    read -r -p "Projeto ${i}/${PROJECT_COUNT}: " project_raw

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
      echo "Erro: projeto repetido. Informe outro nome."
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
    break
  done
done

if ask_yes_no "Deseja criar a pasta assets?" "n"; then
  CREATE_ASSETS=1
fi

if ask_yes_no "Deseja criar a pasta docs?" "n"; then
  CREATE_DOCS=1
fi

mkdir -p "${ARTICLE_DIR}/projects"
touch "${ARTICLE_DIR}/projects/.gitkeep"

if [[ ${CREATE_ASSETS} -eq 1 ]]; then
  mkdir -p "${ARTICLE_DIR}/assets"
  touch "${ARTICLE_DIR}/assets/.gitkeep"
fi

if [[ ${CREATE_DOCS} -eq 1 ]]; then
  mkdir -p "${ARTICLE_DIR}/docs"
  touch "${ARTICLE_DIR}/docs/.gitkeep"
fi

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

optional_dirs_output=""
if [[ ${CREATE_ASSETS} -eq 1 ]]; then
  optional_dirs_output+="- ${ARTICLE_DIR}/assets"$'\n'
fi
if [[ ${CREATE_DOCS} -eq 1 ]]; then
  optional_dirs_output+="- ${ARTICLE_DIR}/docs"$'\n'
fi

cat <<EOF
Artigo criado com sucesso:
- ${ARTICLE_DIR}
- ${README_FILE}

Projetos criados:
$(for project in "${PROJECT_NAMES[@]}"; do printf -- "- %s\n" "${ARTICLE_DIR}/projects/${project}"; done)

EOF

if [[ -n "${optional_dirs_output}" ]]; then
  cat <<EOF
Pastas opcionais criadas:
${optional_dirs_output}
EOF
fi

cat <<EOF
Proximo passo:
1. Colocar codigo em articles/${ARTICLE_ID}/projects/<project-name>
2. Completar objetivos/comandos em ${README_FILE}
EOF