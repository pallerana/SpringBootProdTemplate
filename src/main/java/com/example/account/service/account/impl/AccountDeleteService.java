package com.example.account.service.account.impl;

import com.example.account.dto.account.response.AccountDeleteResponseDTO;
import com.example.account.exception.AccountNotFoundException;
import com.example.account.model.account.AccountEntity;
import com.example.account.repository.AccountRepository;
import com.example.account.service.account.IAccountDeleteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation for account deletion operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountDeleteService implements IAccountDeleteService {
    
    private final AccountRepository accountRepository;
    
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public AccountDeleteResponseDTO deleteAccount(String accountId) {
        log.info("Deleting account with ID: {}", accountId);
        
        // Get account before deletion to return reference ID in response
        AccountEntity accountEntity = accountRepository.findByAccountId(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
        
        // Delete account
        accountRepository.delete(accountEntity);
        log.info("Account deleted successfully with ID: {}", accountId);
        
        return AccountDeleteResponseDTO.builder()
                .accountId(accountEntity.getAccountId())
                .message("Account deleted successfully")
                .deleted(true)
                .build();
    }
}

