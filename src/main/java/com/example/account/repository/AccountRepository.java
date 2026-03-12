package com.example.account.repository;

import com.example.account.model.account.AccountEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Account entity.
 */
@Repository
public interface AccountRepository extends JpaRepository<AccountEntity, Long> {
    
    Optional<AccountEntity> findByAccountId(String accountId);
    
    @Query("SELECT a FROM AccountEntity a WHERE " +
           "(:accountName IS NULL OR LOWER(a.accountName) LIKE LOWER(CONCAT('%', :accountName, '%'))) AND " +
           "(:accountId IS NULL OR a.accountId = :accountId) AND " +
           "(:currency IS NULL OR a.currency = :currency) AND " +
           "(:countryCode IS NULL OR a.countryCode = :countryCode) AND " +
           "(:city IS NULL OR LOWER(a.city) LIKE LOWER(CONCAT('%', :city, '%'))) AND " +
           "(:state IS NULL OR a.state = :state) AND " +
           "(:zipcode IS NULL OR a.zipcode = :zipcode) AND " +
           "(:status IS NULL OR a.status = :status)")
    Page<AccountEntity> findAccountsWithFilters(
            @Param("accountName") String accountName,
            @Param("accountId") String accountId,
            @Param("currency") String currency,
            @Param("countryCode") String countryCode,
            @Param("city") String city,
            @Param("state") String state,
            @Param("zipcode") String zipcode,
            @Param("status") String status,
            Pageable pageable);
}

