package br.com.devsuperior.dev_xp_ai;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class DevControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final String CORRELATION_ID = UUID.randomUUID().toString();

    @BeforeEach
    void limparTabela() {
        jdbcTemplate.execute("DELETE FROM tb_developer");
    }

    private String payloadValido() {
        return """
                {
                    "fullName": "Maria da Silva",
                    "email": "maria@example.com",
                    "nickname": "maria_dev",
                    "uf": "SP",
                    "yearsOfExperience": 5,
                    "primaryLanguage": "Java",
                    "interestedInAi": true,
                    "skills": ["Spring Boot", "Docker"]
                }
                """;
    }

    private String payloadOutro() {
        return """
                {
                    "fullName": "Carlos Eduardo",
                    "email": "carlos@example.com",
                    "nickname": "carlos_dev",
                    "uf": "RJ",
                    "yearsOfExperience": 3,
                    "primaryLanguage": "Python",
                    "interestedInAi": false,
                    "skills": ["Django", "FastAPI"]
                }
                """;
    }

    // =========================================================
    //  POST /developers
    // =========================================================
    @Nested
    @DisplayName("POST /developers")
    class CriarDeveloper {

        @Test
        @DisplayName("Deve criar developer e retornar 201 com Location e body correto")
        void deveCriarDeveloperQuandoDadosValidos() throws Exception {
            mockMvc.perform(post("/developers")
                            .header("correlationId", CORRELATION_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payloadValido()))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", matchesPattern("/developers/\\d+")))
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.fullName").value("Maria Da Silva"))
                    .andExpect(jsonPath("$.email").value("maria@example.com"))
                    .andExpect(jsonPath("$.nickname").value("maria_dev"))
                    .andExpect(jsonPath("$.uf").value("SP"))
                    .andExpect(jsonPath("$.yearsOfExperience").value(5))
                    .andExpect(jsonPath("$.primaryLanguage").value("Java"))
                    .andExpect(jsonPath("$.interestedInAi").value(true))
                    .andExpect(jsonPath("$.skills", hasSize(2)));
        }

        @Test
        @DisplayName("Deve normalizar email para lowercase e fullName para title case")
        void deveNormalizarCamposQuandoCriarDeveloper() throws Exception {
            String payloadComCaseMixto = """
                    {
                        "fullName": "JOAO CARLOS SILVA",
                        "email": "JOAO@EXAMPLE.COM",
                        "nickname": "joao.dev",
                        "uf": "mg",
                        "yearsOfExperience": 2,
                        "primaryLanguage": "JAVA",
                        "interestedInAi": false,
                        "skills": ["spring boot"]
                    }
                    """;

            mockMvc.perform(post("/developers")
                            .header("correlationId", CORRELATION_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payloadComCaseMixto))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.email").value("joao@example.com"))
                    .andExpect(jsonPath("$.fullName").value("Joao Carlos Silva"))
                    .andExpect(jsonPath("$.uf").value("MG"))
                    .andExpect(jsonPath("$.primaryLanguage").value("Java"));
        }

        @Test
        @DisplayName("Deve retornar 400 quando fullName for muito curto")
        void deveRetornar400QuandoFullNameForMuitoCurto() throws Exception {
            String payload = """
                    {
                        "fullName": "Ana",
                        "email": "ana@example.com",
                        "nickname": "ana_dev",
                        "uf": "SP",
                        "yearsOfExperience": 1,
                        "primaryLanguage": "Java",
                        "interestedInAi": true,
                        "skills": ["Spring"]
                    }
                    """;

            mockMvc.perform(post("/developers")
                            .header("correlationId", CORRELATION_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.details").isArray())
                    .andExpect(jsonPath("$.details[0]").value(containsString("fullName")));
        }

        @Test
        @DisplayName("Deve retornar 400 quando email for invalido")
        void deveRetornar400QuandoEmailForInvalido() throws Exception {
            String payload = """
                    {
                        "fullName": "Maria da Silva",
                        "email": "email-invalido",
                        "nickname": "maria_dev",
                        "uf": "SP",
                        "yearsOfExperience": 5,
                        "primaryLanguage": "Java",
                        "interestedInAi": true,
                        "skills": ["Spring Boot"]
                    }
                    """;

            mockMvc.perform(post("/developers")
                            .header("correlationId", CORRELATION_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.details").isArray())
                    .andExpect(jsonPath("$.details[0]").value(containsString("email")));
        }

        @Test
        @DisplayName("Deve retornar 400 quando nickname for invalido")
        void deveRetornar400QuandoNicknameForInvalido() throws Exception {
            String payload = """
                    {
                        "fullName": "Maria da Silva",
                        "email": "maria@example.com",
                        "nickname": "ab",
                        "uf": "SP",
                        "yearsOfExperience": 5,
                        "primaryLanguage": "Java",
                        "interestedInAi": true,
                        "skills": ["Spring Boot"]
                    }
                    """;

            mockMvc.perform(post("/developers")
                            .header("correlationId", CORRELATION_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.details[0]").value(containsString("nickname")));
        }

        @Test
        @DisplayName("Deve retornar 400 quando UF for invalida")
        void deveRetornar400QuandoUfForInvalida() throws Exception {
            String payload = """
                    {
                        "fullName": "Maria da Silva",
                        "email": "maria@example.com",
                        "nickname": "maria_dev",
                        "uf": "XX",
                        "yearsOfExperience": 5,
                        "primaryLanguage": "Java",
                        "interestedInAi": true,
                        "skills": ["Spring Boot"]
                    }
                    """;

            mockMvc.perform(post("/developers")
                            .header("correlationId", CORRELATION_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.details[0]").value(containsString("uf")));
        }

        @Test
        @DisplayName("Deve retornar 400 quando yearsOfExperience for negativo")
        void deveRetornar400QuandoYearsOfExperienceForNegativo() throws Exception {
            String payload = """
                    {
                        "fullName": "Maria da Silva",
                        "email": "maria@example.com",
                        "nickname": "maria_dev",
                        "uf": "SP",
                        "yearsOfExperience": -1,
                        "primaryLanguage": "Java",
                        "interestedInAi": true,
                        "skills": ["Spring Boot"]
                    }
                    """;

            mockMvc.perform(post("/developers")
                            .header("correlationId", CORRELATION_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.details[0]").value(containsString("yearsOfExperience")));
        }

        @Test
        @DisplayName("Deve retornar 400 quando skills estiver vazio")
        void deveRetornar400QuandoSkillsEstiverVazio() throws Exception {
            String payload = """
                    {
                        "fullName": "Maria da Silva",
                        "email": "maria@example.com",
                        "nickname": "maria_dev",
                        "uf": "SP",
                        "yearsOfExperience": 5,
                        "primaryLanguage": "Java",
                        "interestedInAi": true,
                        "skills": []
                    }
                    """;

            mockMvc.perform(post("/developers")
                            .header("correlationId", CORRELATION_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.details[0]").value(containsString("skills")));
        }

        @Test
        @DisplayName("Deve retornar 400 quando skills tiver mais de 10 itens")
        void deveRetornar400QuandoSkillsTiverMaisDe10Itens() throws Exception {
            String payload = """
                    {
                        "fullName": "Maria da Silva",
                        "email": "maria@example.com",
                        "nickname": "maria_dev",
                        "uf": "SP",
                        "yearsOfExperience": 5,
                        "primaryLanguage": "Java",
                        "interestedInAi": true,
                        "skills": ["s1","s2","s3","s4","s5","s6","s7","s8","s9","s10","s11"]
                    }
                    """;

            mockMvc.perform(post("/developers")
                            .header("correlationId", CORRELATION_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.details[0]").value(containsString("skills")));
        }

        @Test
        @DisplayName("Deve retornar 409 quando email ja estiver cadastrado")
        void deveRetornar409QuandoEmailJaCadastrado() throws Exception {
            mockMvc.perform(post("/developers")
                    .header("correlationId", CORRELATION_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payloadValido()));

            String payloadEmailDuplicado = """
                    {
                        "fullName": "Maria Souza",
                        "email": "maria@example.com",
                        "nickname": "maria_souza",
                        "uf": "SP",
                        "yearsOfExperience": 2,
                        "primaryLanguage": "Python",
                        "interestedInAi": false,
                        "skills": ["Django"]
                    }
                    """;

            mockMvc.perform(post("/developers")
                            .header("correlationId", CORRELATION_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payloadEmailDuplicado))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.details").isArray())
                    .andExpect(jsonPath("$.details[0]").value(containsString("email")));
        }

        @Test
        @DisplayName("Deve retornar 409 quando nickname ja estiver cadastrado")
        void deveRetornar409QuandoNicknameJaCadastrado() throws Exception {
            mockMvc.perform(post("/developers")
                    .header("correlationId", CORRELATION_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payloadValido()));

            String payloadNicknameDuplicado = """
                    {
                        "fullName": "Maria Souza",
                        "email": "outro@example.com",
                        "nickname": "maria_dev",
                        "uf": "SP",
                        "yearsOfExperience": 2,
                        "primaryLanguage": "Python",
                        "interestedInAi": false,
                        "skills": ["Django"]
                    }
                    """;

            mockMvc.perform(post("/developers")
                            .header("correlationId", CORRELATION_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payloadNicknameDuplicado))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.details").isArray())
                    .andExpect(jsonPath("$.details[0]").value(containsString("nickname")));
        }

        @Test
        @DisplayName("Deve retornar 400 quando correlationId nao for informado")
        void deveRetornar400QuandoCorrelationIdNaoInformado() throws Exception {
            mockMvc.perform(post("/developers")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payloadValido()))
                    .andExpect(status().isBadRequest());
        }
    }

    // =========================================================
    //  GET /developers
    // =========================================================
    @Nested
    @DisplayName("GET /developers")
    class ListarDevelopers {

        @BeforeEach
        void inserirDadosBase() throws Exception {
            mockMvc.perform(post("/developers")
                    .header("correlationId", CORRELATION_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payloadValido()));

            mockMvc.perform(post("/developers")
                    .header("correlationId", CORRELATION_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(payloadOutro()));
        }

        @Test
        @DisplayName("Deve retornar lista com todos os developers cadastrados")
        void deveRetornarListaQuandoListarTodos() throws Exception {
            mockMvc.perform(get("/developers")
                            .header("correlationId", CORRELATION_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].email").value("maria@example.com"))
                    .andExpect(jsonPath("$[1].email").value("carlos@example.com"));
        }

        @Test
        @DisplayName("Deve filtrar developers por UF valida")
        void deveFiltrarPorUfQuandoUfValida() throws Exception {
            mockMvc.perform(get("/developers")
                            .header("correlationId", CORRELATION_ID)
                            .param("uf", "SP"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].uf").value("SP"));
        }

        @Test
        @DisplayName("Deve filtrar developers por linguagem")
        void deveFiltrarPorLinguagemQuandoParametroInformado() throws Exception {
            mockMvc.perform(get("/developers")
                            .header("correlationId", CORRELATION_ID)
                            .param("language", "Python"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].primaryLanguage").value("Python"));
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando nenhum developer combinar com o filtro")
        void deveRetornarListaVaziaQuandoNenhumResultado() throws Exception {
            mockMvc.perform(get("/developers")
                            .header("correlationId", CORRELATION_ID)
                            .param("language", "COBOL"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$", hasSize(0)));
        }

        @Test
        @DisplayName("Deve retornar 400 quando filtro UF for invalido")
        void deveRetornar400QuandoFiltroUfInvalido() throws Exception {
            mockMvc.perform(get("/developers")
                            .header("correlationId", CORRELATION_ID)
                            .param("uf", "ZZ"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.details").isArray());
        }

        @Test
        @DisplayName("Deve retornar 400 quando correlationId nao for informado no GET")
        void deveRetornar400QuandoCorrelationIdNaoInformadoNoGet() throws Exception {
            mockMvc.perform(get("/developers"))
                    .andExpect(status().isBadRequest());
        }
    }

    // =========================================================
    //  GET /developers/{id}
    // =========================================================
    @Nested
    @DisplayName("GET /developers/{id}")
    class BuscarDeveloperPorId {

        @Test
        @DisplayName("Deve retornar developer quando id existir")
        void deveRetornarDeveloperQuandoIdExistir() throws Exception {
            MvcResult result = mockMvc.perform(post("/developers")
                            .header("correlationId", CORRELATION_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payloadValido()))
                    .andReturn();

            String location = result.getResponse().getHeader("Location");
            String id = extrairIdDaLocation(location);

            mockMvc.perform(get("/developers/" + id)
                            .header("correlationId", CORRELATION_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(Long.parseLong(id)))
                    .andExpect(jsonPath("$.email").value("maria@example.com"))
                    .andExpect(jsonPath("$.skills").isArray());
        }

        @Test
        @DisplayName("Deve retornar 404 quando id nao existir")
        void deveRetornar404QuandoIdNaoExistir() throws Exception {
            mockMvc.perform(get("/developers/99999")
                            .header("correlationId", CORRELATION_ID))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.details").isArray())
                    .andExpect(jsonPath("$.details[0]").value(containsString("99999")));
        }

        @Test
        @DisplayName("Deve retornar 400 quando correlationId nao for informado no GET por id")
        void deveRetornar400QuandoCorrelationIdNaoInformadoNoBuscarPorId() throws Exception {
            mockMvc.perform(get("/developers/1"))
                    .andExpect(status().isBadRequest());
        }
    }

    // =========================================================
    //  PUT /developers/{id}/experience
    // =========================================================
    @Nested
    @DisplayName("PUT /developers/{id}/experience")
    class AtualizarExperiencia {

        @Test
        @DisplayName("Deve atualizar yearsOfExperience e retornar 200 com body atualizado")
        void deveAtualizarExperienciaQuandoDadosValidos() throws Exception {
            MvcResult result = mockMvc.perform(post("/developers")
                            .header("correlationId", CORRELATION_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payloadValido()))
                    .andReturn();

            String id = extrairIdDaLocation(result.getResponse().getHeader("Location"));

            String payload = """
                    { "yearsOfExperience": 10 }
                    """;

            mockMvc.perform(put("/developers/" + id + "/experience")
                            .header("correlationId", CORRELATION_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.yearsOfExperience").value(10))
                    .andExpect(jsonPath("$.email").value("maria@example.com"));
        }

        @Test
        @DisplayName("Deve retornar 400 quando yearsOfExperience for invalido no update")
        void deveRetornar400QuandoYearsOfExperienceForInvalidoNoUpdate() throws Exception {
            MvcResult result = mockMvc.perform(post("/developers")
                            .header("correlationId", CORRELATION_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payloadValido()))
                    .andReturn();

            String id = extrairIdDaLocation(result.getResponse().getHeader("Location"));

            String payload = """
                    { "yearsOfExperience": 100 }
                    """;

            mockMvc.perform(put("/developers/" + id + "/experience")
                            .header("correlationId", CORRELATION_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.details").isArray())
                    .andExpect(jsonPath("$.details[0]").value(containsString("yearsOfExperience")));
        }

        @Test
        @DisplayName("Deve retornar 400 quando yearsOfExperience for negativo no update")
        void deveRetornar400QuandoYearsOfExperienceForNegativoNoUpdate() throws Exception {
            MvcResult result = mockMvc.perform(post("/developers")
                            .header("correlationId", CORRELATION_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payloadValido()))
                    .andReturn();

            String id = extrairIdDaLocation(result.getResponse().getHeader("Location"));

            String payload = """
                    { "yearsOfExperience": -5 }
                    """;

            mockMvc.perform(put("/developers/" + id + "/experience")
                            .header("correlationId", CORRELATION_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.details[0]").value(containsString("yearsOfExperience")));
        }

        @Test
        @DisplayName("Deve retornar 404 quando id nao existir no update de experiencia")
        void deveRetornar404QuandoIdNaoExistirNoUpdateDeExperiencia() throws Exception {
            String payload = """
                    { "yearsOfExperience": 5 }
                    """;

            mockMvc.perform(put("/developers/99999/experience")
                            .header("correlationId", CORRELATION_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").exists())
                    .andExpect(jsonPath("$.details").isArray())
                    .andExpect(jsonPath("$.details[0]").value(containsString("99999")));
        }

        @Test
        @DisplayName("Deve retornar 400 quando correlationId nao for informado no PUT")
        void deveRetornar400QuandoCorrelationIdNaoInformadoNoPut() throws Exception {
            String payload = """
                    { "yearsOfExperience": 5 }
                    """;

            mockMvc.perform(put("/developers/1/experience")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload))
                    .andExpect(status().isBadRequest());
        }
    }

    // =========================================================
    //  Helper
    // =========================================================
    private String extrairIdDaLocation(String location) {
        Matcher matcher = Pattern.compile("/(\\d+)$").matcher(location);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new IllegalArgumentException("Nao foi possivel extrair id da Location: " + location);
    }
}


