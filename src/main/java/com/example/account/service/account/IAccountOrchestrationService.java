package com.example.account.service.account;

import com.example.account.dto.account.request.AccountCreateRequestDTO;
import com.example.account.dto.account.request.AccountListRequestDTO;
import com.example.account.dto.account.request.AccountUpdateRequestDTO;
import com.example.account.dto.account.response.AccountCreateResponseDTO;
import com.example.account.dto.account.response.AccountDeleteResponseDTO;
import com.example.account.dto.account.response.AccountDetailsResponseDTO;
import com.example.account.dto.account.response.AccountListResponseDTO;
import com.example.account.dto.account.response.AccountUpdateResponseDTO;

/**
 * Orchestrator service interface for account operations.
 */
public interface IAccountOrchestrationService {
    
    AccountCreateResponseDTO createAccount(AccountCreateRequestDTO request);
    
    AccountUpdateResponseDTO updateAccount(String accountId, AccountUpdateRequestDTO request);
    
    AccountDetailsResponseDTO getAccount(String accountId);
    
    AccountListResponseDTO getAccounts(AccountListRequestDTO request);
    
    AccountUpdateResponseDTO patchAccount(String accountId, AccountUpdateRequestDTO request);
    
    AccountDeleteResponseDTO deleteAccount(String accountId);
}

