package com.example.account.repository;

import com.example.account.model.IdempotencyKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for IdempotencyKey entity.
 */
@Repository
public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKey, Long> {
    
    /**
     * Find idempotency key by the key value.
     * 
     * @param idempotencyKey The idempotency key value
     * @return Optional IdempotencyKey
     */
    Optional<IdempotencyKey> findByIdempotencyKey(String idempotencyKey);
}

