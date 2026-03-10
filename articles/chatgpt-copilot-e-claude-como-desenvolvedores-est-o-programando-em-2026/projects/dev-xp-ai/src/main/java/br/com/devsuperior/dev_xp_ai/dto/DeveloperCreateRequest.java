package br.com.devsuperior.dev_xp_ai.dto;

import java.util.List;

public record DeveloperCreateRequest(
        String fullName,
        String email,
        String nickname,
        String uf,
        Integer yearsOfExperience,
        String primaryLanguage,
        Boolean interestedInAi,
        List<String> skills
) {
}

