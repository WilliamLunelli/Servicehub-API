package com.servicehub.api.exception;

public class DuplicateProviderException extends RuntimeException {

    public DuplicateProviderException(String email) {
        super("Ja existe um prestador de servico cadastrado com o e-mail " + email);
    }
}
