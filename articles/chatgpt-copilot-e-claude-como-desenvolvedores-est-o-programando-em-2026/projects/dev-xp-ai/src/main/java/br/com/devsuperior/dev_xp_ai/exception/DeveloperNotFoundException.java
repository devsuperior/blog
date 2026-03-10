package br.com.devsuperior.dev_xp_ai.exception;

public class DeveloperNotFoundException extends RuntimeException {

    private final Long id;

    public DeveloperNotFoundException(Long id) {
        super("Nao existe desenvolvedor com id " + id + ".");
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}

