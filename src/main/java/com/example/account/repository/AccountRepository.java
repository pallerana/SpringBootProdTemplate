package com.example.account.repository;

import com.example.account.model.account.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Account entity.
 */
@Repository
public interface AccountRepository extends JpaRepository<AccountEntity, Long>, JpaSpecificationExecutor<AccountEntity> {

    Optional<AccountEntity> findByAccountId(String accountId);
}


