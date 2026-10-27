package com.servicehub.api.exception;

public class EmailAlreadyUsedException extends RuntimeException {

    public EmailAlreadyUsedException(String email) {
        super("Ja existe um usuario cadastrado com o e-mail " + email);
    }
}
