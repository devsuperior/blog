package br.com.devsuperior.dev_xp_ai.dto;

import java.util.List;

public record ErrorResponse(String message, List<String> details) {
}

