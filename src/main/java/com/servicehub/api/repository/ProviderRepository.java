package com.servicehub.api.repository;

import com.servicehub.api.model.Provider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProviderRepository extends JpaRepository<Provider, Long> {

    Optional<Provider> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);
}
