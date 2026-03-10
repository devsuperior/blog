package br.com.devsuperior.dev_xp_ai.service;

import br.com.devsuperior.dev_xp_ai.dto.DeveloperCreateRequest;
import br.com.devsuperior.dev_xp_ai.dto.DeveloperResponse;
import br.com.devsuperior.dev_xp_ai.dto.UpdateExperienceRequest;
import br.com.devsuperior.dev_xp_ai.entity.DeveloperEntity;
import br.com.devsuperior.dev_xp_ai.exception.ConflictException;
import br.com.devsuperior.dev_xp_ai.exception.DeveloperNotFoundException;
import br.com.devsuperior.dev_xp_ai.repository.DeveloperRepository;
import br.com.devsuperior.dev_xp_ai.util.TextNormalizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class DeveloperService {

    private static final Logger log = LoggerFactory.getLogger(DeveloperService.class);

    private static final Pattern SIMPLE_EMAIL_PATTERN = Pattern.compile("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");
    private static final Pattern NICKNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9._-]{3,30}$");
    private static final Set<String> VALID_UFS = Set.of(
            "AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO", "MA", "MT",
            "MS", "MG", "PA", "PB", "PR", "PE", "PI", "RJ", "RN", "RS", "RO",
            "RR", "SC", "SP", "SE", "TO"
    );

    private final DeveloperRepository developerRepository;

    public DeveloperService(DeveloperRepository developerRepository) {
        this.developerRepository = developerRepository;
    }

    public DeveloperResponse createDeveloper(DeveloperCreateRequest request) {
        log.info("[correlationId={}] Iniciando criacao de developer: email={}", MDC.get("correlationId"), request.email());

        List<String> errors = validateCreateRequest(request);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join("; ", errors));
        }

        String normalizedEmail = TextNormalizer.toLowerCaseTrimmed(request.email());
        String normalizedNickname = TextNormalizer.toLowerCaseTrimmed(request.nickname());
        String normalizedUf = TextNormalizer.toUpperCaseTrimmed(request.uf());
        String normalizedName = TextNormalizer.toTitleCase(request.fullName());
        String normalizedLanguage = TextNormalizer.toTitleCase(request.primaryLanguage());
        List<String> normalizedSkillsList = request.skills().stream()
                .map(TextNormalizer::toTitleCase)
                .toList();
        String skillsCsv = TextNormalizer.serializeSkills(normalizedSkillsList);

        if (developerRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new ConflictException("Email ja cadastrado",
                    List.of("Ja existe um desenvolvedor com este email."));
        }

        if (developerRepository.existsByNicknameIgnoreCase(normalizedNickname)) {
            throw new ConflictException("Nickname ja cadastrado",
                    List.of("Ja existe um desenvolvedor com este nickname."));
        }

        DeveloperEntity entity = new DeveloperEntity(
                null,
                normalizedName,
                normalizedEmail,
                normalizedNickname,
                normalizedUf,
                request.yearsOfExperience(),
                normalizedLanguage,
                request.interestedInAi(),
                skillsCsv
        );

        DeveloperEntity saved = developerRepository.save(entity);

        log.info("[correlationId={}] Developer criado com sucesso: id={}", MDC.get("correlationId"), saved.getId());
        return toResponse(saved);
    }

    public List<DeveloperResponse> listDevelopers(String uf, String language) {
        log.info("[correlationId={}] Listando developers: uf={}, language={}", MDC.get("correlationId"), uf, language);

        String normalizedUf = null;
        if (uf != null && !uf.isBlank()) {
            normalizedUf = uf.trim().toUpperCase(Locale.ROOT);
            if (!VALID_UFS.contains(normalizedUf)) {
                throw new IllegalArgumentException("O filtro UF deve ser uma sigla de estado brasileira valida.");
            }
        }

        String normalizedLanguage = (language != null && !language.isBlank()) ? language.trim() : null;

        List<DeveloperResponse> result = developerRepository
                .findAllByFilters(normalizedUf, normalizedLanguage)
                .stream()
                .map(this::toResponse)
                .toList();

        log.info("[correlationId={}] Listagem concluida: {} resultado(s)", MDC.get("correlationId"), result.size());
        return result;
    }

    public DeveloperResponse getDeveloperById(Long id) {
        log.info("[correlationId={}] Buscando developer por id={}", MDC.get("correlationId"), id);

        DeveloperEntity entity = developerRepository.findById(id)
                .orElseThrow(() -> new DeveloperNotFoundException(id));

        log.info("[correlationId={}] Developer encontrado: id={}", MDC.get("correlationId"), id);
        return toResponse(entity);
    }

    public DeveloperResponse updateExperience(Long id, UpdateExperienceRequest request) {
        log.info("[correlationId={}] Atualizando experiencia do developer id={}", MDC.get("correlationId"), id);

        List<String> errors = validateExperienceUpdate(request);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join("; ", errors));
        }

        DeveloperEntity entity = developerRepository.findById(id)
                .orElseThrow(() -> new DeveloperNotFoundException(id));

        entity.setYearsOfExperience(request.yearsOfExperience());
        DeveloperEntity updated = developerRepository.save(entity);

        log.info("[correlationId={}] Experiencia atualizada para id={}: yearsOfExperience={}", MDC.get("correlationId"), id, updated.getYearsOfExperience());
        return toResponse(updated);
    }

    private List<String> validateCreateRequest(DeveloperCreateRequest request) {
        List<String> errors = new ArrayList<>();
        if (request == null) {
            errors.add("O corpo da requisicao e obrigatorio.");
            return errors;
        }
        if (!hasText(request.fullName()) || request.fullName().trim().length() < 5 || request.fullName().trim().length() > 120) {
            errors.add("fullName deve ter entre 5 e 120 caracteres.");
        }
        if (!hasText(request.email()) || !SIMPLE_EMAIL_PATTERN.matcher(request.email().trim()).matches()) {
            errors.add("email deve ter um formato valido.");
        }
        if (!hasText(request.nickname()) || !NICKNAME_PATTERN.matcher(request.nickname().trim()).matches()) {
            errors.add("nickname deve ter de 3 a 30 caracteres e usar apenas letras, numeros, ponto, underscore ou hifen.");
        }
        if (!hasText(request.uf()) || !VALID_UFS.contains(request.uf().trim().toUpperCase(Locale.ROOT))) {
            errors.add("uf deve ser uma sigla de estado brasileira valida.");
        }
        if (request.yearsOfExperience() == null || request.yearsOfExperience() < 0 || request.yearsOfExperience() > 60) {
            errors.add("yearsOfExperience deve estar entre 0 e 60.");
        }
        if (!hasText(request.primaryLanguage()) || request.primaryLanguage().trim().length() > 50) {
            errors.add("primaryLanguage e obrigatorio e deve ter no maximo 50 caracteres.");
        }
        if (request.interestedInAi() == null) {
            errors.add("interestedInAi e obrigatorio.");
        }
        if (request.skills() == null || request.skills().isEmpty()) {
            errors.add("skills deve conter ao menos um item.");
        } else {
            if (request.skills().size() > 10) {
                errors.add("skills pode conter no maximo 10 itens.");
            }
            for (String skill : request.skills()) {
                if (!hasText(skill) || skill.trim().length() < 2 || skill.trim().length() > 30) {
                    errors.add("cada skill deve ter entre 2 e 30 caracteres.");
                    break;
                }
            }
        }
        return errors;
    }

    private List<String> validateExperienceUpdate(UpdateExperienceRequest request) {
        List<String> errors = new ArrayList<>();
        if (request == null || request.yearsOfExperience() == null) {
            errors.add("yearsOfExperience e obrigatorio.");
            return errors;
        }
        if (request.yearsOfExperience() < 0 || request.yearsOfExperience() > 60) {
            errors.add("yearsOfExperience deve estar entre 0 e 60.");
        }
        return errors;
    }

    private DeveloperResponse toResponse(DeveloperEntity entity) {
        return new DeveloperResponse(
                entity.getId(),
                entity.getFullName(),
                entity.getEmail(),
                entity.getNickname(),
                entity.getUf(),
                entity.getYearsOfExperience(),
                entity.getPrimaryLanguage(),
                entity.getInterestedInAi(),
                TextNormalizer.deserializeSkills(entity.getSkills())
        );
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}

