package com.servicehub.api.service;

import com.servicehub.api.exception.DuplicateProviderException;
import com.servicehub.api.exception.ProviderNotFoundException;
import com.servicehub.api.model.Provider;
import com.servicehub.api.model.dto.ProviderPatchRequest;
import com.servicehub.api.model.dto.ProviderRequest;
import com.servicehub.api.model.dto.ProviderResponse;
import com.servicehub.api.repository.ProviderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProviderServiceTest {

    @Mock
    private ProviderRepository providerRepository;

    @InjectMocks
    private ProviderService providerService;

    private ProviderRequest validRequest() {
        return new ProviderRequest("Carlos Souza", "carlos@example.com", "(11) 98765-4321",
                "Eletricista", "Instalacoes eletricas", new BigDecimal("85.50"));
    }

    private Provider existingProvider() {
        return Provider.builder()
                .id(1L)
                .name("Carlos Souza")
                .email("carlos@example.com")
                .specialty("Eletricista")
                .hourlyRate(new BigDecimal("85.50"))
                .active(true)
                .build();
    }

    @Test
    void deveCriarPrestadorAtivoQuandoEmailDisponivel() {
        when(providerRepository.existsByEmailIgnoreCase("carlos@example.com")).thenReturn(false);
        when(providerRepository.save(any(Provider.class))).thenAnswer(invocation -> {
            Provider provider = invocation.getArgument(0);
            provider.setId(1L);
            return provider;
        });

        ProviderResponse response = providerService.create(validRequest());

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.isActive()).isTrue();
        assertThat(response.getEmail()).isEqualTo("carlos@example.com");
        verify(providerRepository, times(1)).save(any(Provider.class));
    }

    @Test
    void deveLancarExcecaoQuandoEmailJaCadastrado() {
        when(providerRepository.existsByEmailIgnoreCase("carlos@example.com")).thenReturn(true);

        assertThatThrownBy(() -> providerService.create(validRequest()))
                .isInstanceOf(DuplicateProviderException.class);

        verify(providerRepository, never()).save(any(Provider.class));
    }

    @Test
    void deveRemoverTagsHtmlDosCamposDeTextoAoCriar() {
        ProviderRequest request = new ProviderRequest("<script>alert(1)</script>Carlos Souza",
                "carlos@example.com", null, "Eletricista", "<b>Instalacoes</b>", new BigDecimal("85.50"));
        when(providerRepository.existsByEmailIgnoreCase("carlos@example.com")).thenReturn(false);
        when(providerRepository.save(any(Provider.class))).then(returnsFirstArg());

        providerService.create(request);

        ArgumentCaptor<Provider> captor = ArgumentCaptor.forClass(Provider.class);
        verify(providerRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).doesNotContain("<").doesNotContain(">");
        assertThat(captor.getValue().getDescription()).isEqualTo("Instalacoes");
    }

    @Test
    void deveLancarNotFoundQuandoIdNaoExiste() {
        when(providerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> providerService.findById(99L))
                .isInstanceOf(ProviderNotFoundException.class);
    }

    @Test
    void deveAlterarApenasOsCamposEnviadosNoPatch() {
        Provider provider = existingProvider();
        when(providerRepository.findById(1L)).thenReturn(Optional.of(provider));
        when(providerRepository.save(any(Provider.class))).then(returnsFirstArg());

        ProviderPatchRequest patch = new ProviderPatchRequest();
        patch.setHourlyRate(new BigDecimal("100.00"));

        ProviderResponse response = providerService.patch(1L, patch);

        assertThat(response.getHourlyRate()).isEqualByComparingTo("100.00");
        assertThat(response.getName()).isEqualTo("Carlos Souza");
        assertThat(response.getSpecialty()).isEqualTo("Eletricista");
    }

    @Test
    void deveListarTodosQuandoEspecialidadeNaoInformada() {
        when(providerRepository.findAll()).thenReturn(List.of(existingProvider()));

        List<ProviderResponse> result = providerService.findAll(null);

        assertThat(result).hasSize(1);
        verify(providerRepository, never()).searchBySpecialty(any());
    }

    @Test
    void deveBuscarPorEspecialidadeQuandoFiltroInformado() {
        when(providerRepository.searchBySpecialty("Eletricista")).thenReturn(List.of(existingProvider()));

        List<ProviderResponse> result = providerService.findAll("Eletricista");

        assertThat(result).hasSize(1);
        verify(providerRepository, never()).findAll();
    }

    @Test
    void deveRemoverPrestadorQuandoExistir() {
        Provider provider = existingProvider();
        when(providerRepository.findById(1L)).thenReturn(Optional.of(provider));

        providerService.delete(1L);

        verify(providerRepository, times(1)).delete(provider);
    }
}
