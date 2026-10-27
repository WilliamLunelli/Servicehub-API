package com.servicehub.api.model.dto;

/**
 * Tipos de conta que podem ser escolhidos no cadastro. ADMIN nao existe aqui de proposito:
 * o perfil administrador nunca pode ser pedido pelo cliente (protecao contra mass assignment).
 */
public enum AccountType {
    CLIENT,
    PROVIDER
}
