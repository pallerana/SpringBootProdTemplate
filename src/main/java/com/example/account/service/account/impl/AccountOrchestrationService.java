package com.example.account.service.account.impl;

import com.example.account.dto.account.request.AccountCreateRequestDTO;
import com.example.account.dto.account.request.AccountListRequestDTO;
import com.example.account.dto.account.request.AccountUpdateRequestDTO;
import com.example.account.dto.account.response.AccountCreateResponseDTO;
import com.example.account.dto.account.response.AccountDeleteResponseDTO;
import com.example.account.dto.account.response.AccountDetailsResponseDTO;
import com.example.account.dto.account.response.AccountListResponseDTO;
import com.example.account.dto.account.response.AccountUpdateResponseDTO;
import com.example.account.service.account.IAccountDeleteService;
import com.example.account.service.account.IAccountOrchestrationService;
import com.example.account.service.account.IAccountRetrievalService;
import com.example.account.service.account.IAccountUpdateService;
import com.example.account.service.account.ICreateAccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Orchestrator service implementation for account operations.
 * Delegates to granular services for specific operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountOrchestrationService implements IAccountOrchestrationService {
    
    private final ICreateAccountService createAccountService;
    private final IAccountRetrievalService accountRetrievalService;
    private final IAccountUpdateService accountUpdateService;
    private final IAccountDeleteService accountDeleteService;
    
    @Override
    public AccountCreateResponseDTO createAccount(AccountCreateRequestDTO request) {
        log.info("Orchestrating account creation for account: {}", request.getAccountName());
        AccountCreateResponseDTO response = createAccountService.createAccount(request);
        log.info("Account creation completed with account ID: {}", response.getAccountId());
        return response;
    }
    
    @Override
    public AccountUpdateResponseDTO updateAccount(String accountId, AccountUpdateRequestDTO request) {
        log.info("Orchestrating account update for account ID: {}", accountId);
        return accountUpdateService.updateAccount(accountId, request);
    }
    
    @Override
    public AccountDetailsResponseDTO getAccount(String accountId) {
        log.debug("Orchestrating account retrieval for account ID: {}", accountId);
        return accountRetrievalService.getAccount(accountId);
    }
    
    @Override
    public AccountListResponseDTO getAccounts(AccountListRequestDTO request) {
        log.debug("Orchestrating account list retrieval with filters");
        return accountRetrievalService.getAccounts(request);
    }
    
    @Override
    public AccountUpdateResponseDTO patchAccount(String accountId, AccountUpdateRequestDTO request) {
        log.info("Orchestrating account patch for account ID: {}", accountId);
        return accountUpdateService.patchAccount(accountId, request);
    }
    
    @Override
    public AccountDeleteResponseDTO deleteAccount(String accountId) {
        log.info("Orchestrating account deletion for account ID: {}", accountId);
        AccountDeleteResponseDTO response = accountDeleteService.deleteAccount(accountId);
        log.info("Account deletion completed with ID: {}", accountId);
        return response;
    }
}

