package com.servicehub.api.repository;

import com.servicehub.api.model.Provider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProviderRepository extends JpaRepository<Provider, Long> {

    Optional<Provider> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    // Parametro nomeado (:specialty): o valor nunca e concatenado na consulta (protecao contra SQL injection)
    @Query("SELECT p FROM Provider p WHERE LOWER(p.specialty) LIKE LOWER(CONCAT('%', :specialty, '%'))")
    List<Provider> searchBySpecialty(@Param("specialty") String specialty);
}
