package br.com.devsuperior.dev_xp_ai.controller;

import br.com.devsuperior.dev_xp_ai.dto.DeveloperCreateRequest;
import br.com.devsuperior.dev_xp_ai.dto.DeveloperResponse;
import br.com.devsuperior.dev_xp_ai.dto.ErrorResponse;
import br.com.devsuperior.dev_xp_ai.dto.UpdateExperienceRequest;
import br.com.devsuperior.dev_xp_ai.exception.ConflictException;
import br.com.devsuperior.dev_xp_ai.exception.DeveloperNotFoundException;
import br.com.devsuperior.dev_xp_ai.service.DeveloperService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/developers")
public class DeveloperController {

    private static final Logger log = LoggerFactory.getLogger(DeveloperController.class);

    private final DeveloperService developerService;

    public DeveloperController(DeveloperService developerService) {
        this.developerService = developerService;
    }

    @PostMapping
    public ResponseEntity<?> createDeveloper(
            @RequestHeader("correlationId") UUID correlationId,
            @RequestBody DeveloperCreateRequest request
    ) {
        MDC.put("correlationId", correlationId.toString());
        log.info("[correlationId={}] POST /developers - iniciando criacao", correlationId);
        try {
            DeveloperResponse response = developerService.createDeveloper(request);
            log.info("[correlationId={}] POST /developers - criado id={}", correlationId, response.id());
            return ResponseEntity.created(URI.create("/developers/" + response.id())).body(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Falha de validacao", List.of(ex.getMessage().split("; "))));
        } catch (ConflictException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse(ex.getMessage(), ex.getDetails()));
        } finally {
            MDC.remove("correlationId");
        }
    }

    @GetMapping
    public ResponseEntity<?> listDevelopers(
            @RequestHeader("correlationId") UUID correlationId,
            @RequestParam(required = false) String uf,
            @RequestParam(required = false) String language
    ) {
        MDC.put("correlationId", correlationId.toString());
        log.info("[correlationId={}] GET /developers - uf={}, language={}", correlationId, uf, language);
        try {
            List<DeveloperResponse> response = developerService.listDevelopers(uf, language);
            log.info("[correlationId={}] GET /developers - retornando {} resultado(s)", correlationId, response.size());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Filtro invalido", List.of(ex.getMessage().split("; "))));
        } finally {
            MDC.remove("correlationId");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDeveloperById(
            @RequestHeader("correlationId") UUID correlationId,
            @PathVariable Long id
    ) {
        MDC.put("correlationId", correlationId.toString());
        log.info("[correlationId={}] GET /developers/{} - buscando", correlationId, id);
        try {
            DeveloperResponse response = developerService.getDeveloperById(id);
            log.info("[correlationId={}] GET /developers/{} - encontrado", correlationId, id);
            return ResponseEntity.ok(response);
        } catch (DeveloperNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Desenvolvedor nao encontrado", List.of(ex.getMessage())));
        } finally {
            MDC.remove("correlationId");
        }
    }

    @PutMapping("/{id}/experience")
    public ResponseEntity<?> updateExperience(
            @RequestHeader("correlationId") UUID correlationId,
            @PathVariable Long id,
            @RequestBody UpdateExperienceRequest request
    ) {
        MDC.put("correlationId", correlationId.toString());
        log.info("[correlationId={}] PUT /developers/{}/experience - iniciando update", correlationId, id);
        try {
            DeveloperResponse response = developerService.updateExperience(id, request);
            log.info("[correlationId={}] PUT /developers/{}/experience - atualizado", correlationId, id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Falha de validacao", List.of(ex.getMessage().split("; "))));
        } catch (DeveloperNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Desenvolvedor nao encontrado", List.of(ex.getMessage())));
        } finally {
            MDC.remove("correlationId");
        }
    }
}

