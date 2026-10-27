package com.servicehub.api.security;

import com.servicehub.api.model.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String SECRET = "test-secret-with-at-least-32-characters-long-0123456789";
    private static final String OTHER_SECRET = "another-secret-with-at-least-32-characters-long-9876543210";

    private final JwtService jwtService = new JwtService(SECRET, 3_600_000L);

    @Test
    @DisplayName("deveGerarTokenValidoComEmailERoleQuandoDadosCorretos")
    void deveGerarTokenValidoComEmailERoleQuandoDadosCorretos() {
        String token = jwtService.generateToken("maria@example.com", Role.ADMIN);

        assertThat(jwtService.isTokenValid(token)).isTrue();
        assertThat(jwtService.extractEmail(token)).isEqualTo("maria@example.com");
        assertThat(jwtService.extractRole(token)).isEqualTo("ADMIN");
    }

    @Test
    void devePossuirTresPartesELegivelSemDadoSensivelNoPayload() {
        String token = jwtService.generateToken("maria@example.com", Role.CLIENT);

        String[] parts = token.split("\\.");
        String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);

        assertThat(parts).hasSize(3);
        // o payload e apenas Base64URL: qualquer um le. Por isso nunca vai senha nem dado sensivel.
        assertThat(payload).contains("maria@example.com").contains("CLIENT");
        assertThat(payload).doesNotContainIgnoringCase("password").doesNotContainIgnoringCase("senha");
    }

    @Test
    void deveRejeitarTokenQuandoPayloadForAdulterado() {
        String token = jwtService.generateToken("maria@example.com", Role.CLIENT);
        String[] parts = token.split("\\.");
        String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
        String forgedPayload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payload.replace("CLIENT", "ADMIN").getBytes(StandardCharsets.UTF_8));

        String forgedToken = parts[0] + "." + forgedPayload + "." + parts[2];

        assertThat(jwtService.isTokenValid(forgedToken)).isFalse();
    }

    @Test
    void deveRejeitarTokenQuandoEstiverExpirado() {
        JwtService expiredService = new JwtService(SECRET, -1_000L);
        String expiredToken = expiredService.generateToken("maria@example.com", Role.CLIENT);

        assertThat(jwtService.isTokenValid(expiredToken)).isFalse();
    }

    @Test
    void deveRejeitarTokenQuandoAssinadoComOutraChave() {
        String foreignToken = new JwtService(OTHER_SECRET, 3_600_000L)
                .generateToken("maria@example.com", Role.ADMIN);

        assertThat(jwtService.isTokenValid(foreignToken)).isFalse();
    }

    @Test
    void deveRejeitarTextoQueNaoEUmTokenJwt() {
        assertThat(jwtService.isTokenValid("isto-nao-e-um-jwt")).isFalse();
        assertThat(jwtService.isTokenValid("")).isFalse();
    }
}
