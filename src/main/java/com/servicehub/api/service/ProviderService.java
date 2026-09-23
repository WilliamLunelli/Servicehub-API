package com.servicehub.api.service;

import com.servicehub.api.exception.DuplicateProviderException;
import com.servicehub.api.exception.ProviderNotFoundException;
import com.servicehub.api.model.Provider;
import com.servicehub.api.model.dto.ProviderPatchRequest;
import com.servicehub.api.model.dto.ProviderRequest;
import com.servicehub.api.model.dto.ProviderResponse;
import com.servicehub.api.repository.ProviderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProviderService {

    private final ProviderRepository providerRepository;

    public ProviderService(ProviderRepository providerRepository) {
        this.providerRepository = providerRepository;
    }

    @Transactional(readOnly = true)
    public List<ProviderResponse> findAll() {
        return providerRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProviderResponse findById(Long id) {
        return toResponse(getProviderOrThrow(id));
    }

    @Transactional
    public ProviderResponse create(ProviderRequest request) {
        if (providerRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new DuplicateProviderException(request.getEmail());
        }

        Provider provider = Provider.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .specialty(request.getSpecialty())
                .description(request.getDescription())
                .hourlyRate(request.getHourlyRate())
                .active(true)
                .build();

        return toResponse(providerRepository.save(provider));
    }

    @Transactional
    public ProviderResponse replace(Long id, ProviderRequest request) {
        Provider provider = getProviderOrThrow(id);
        assertEmailAvailable(request.getEmail(), id);

        provider.setName(request.getName());
        provider.setEmail(request.getEmail());
        provider.setPhone(request.getPhone());
        provider.setSpecialty(request.getSpecialty());
        provider.setDescription(request.getDescription());
        provider.setHourlyRate(request.getHourlyRate());

        return toResponse(providerRepository.save(provider));
    }

    @Transactional
    public ProviderResponse patch(Long id, ProviderPatchRequest request) {
        Provider provider = getProviderOrThrow(id);

        if (request.getEmail() != null) {
            assertEmailAvailable(request.getEmail(), id);
            provider.setEmail(request.getEmail());
        }
        if (request.getName() != null) {
            provider.setName(request.getName());
        }
        if (request.getPhone() != null) {
            provider.setPhone(request.getPhone());
        }
        if (request.getSpecialty() != null) {
            provider.setSpecialty(request.getSpecialty());
        }
        if (request.getDescription() != null) {
            provider.setDescription(request.getDescription());
        }
        if (request.getHourlyRate() != null) {
            provider.setHourlyRate(request.getHourlyRate());
        }
        if (request.getActive() != null) {
            provider.setActive(request.getActive());
        }

        return toResponse(providerRepository.save(provider));
    }

    @Transactional
    public void delete(Long id) {
        Provider provider = getProviderOrThrow(id);
        providerRepository.delete(provider);
    }

    private void assertEmailAvailable(String email, Long currentId) {
        providerRepository.findByEmailIgnoreCase(email)
                .filter(existing -> !existing.getId().equals(currentId))
                .ifPresent(existing -> {
                    throw new DuplicateProviderException(email);
                });
    }

    private Provider getProviderOrThrow(Long id) {
        return providerRepository.findById(id)
                .orElseThrow(() -> new ProviderNotFoundException(id));
    }

    private ProviderResponse toResponse(Provider provider) {
        return ProviderResponse.builder()
                .id(provider.getId())
                .name(provider.getName())
                .email(provider.getEmail())
                .phone(provider.getPhone())
                .specialty(provider.getSpecialty())
                .description(provider.getDescription())
                .hourlyRate(provider.getHourlyRate())
                .active(provider.isActive())
                .build();
    }
}
