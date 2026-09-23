package com.servicehub.api.exception;

public class ProviderNotFoundException extends RuntimeException {

    public ProviderNotFoundException(Long id) {
        super("Prestador de servico com id " + id + " nao foi encontrado");
    }
}
