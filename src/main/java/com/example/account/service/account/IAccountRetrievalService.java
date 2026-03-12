package com.example.account.service.account;

import com.example.account.dto.account.request.AccountListRequestDTO;
import com.example.account.dto.account.response.AccountDetailsResponseDTO;
import com.example.account.dto.account.response.AccountListResponseDTO;

/**
 * Service interface for account retrieval operations.
 */
public interface IAccountRetrievalService {
    
    AccountDetailsResponseDTO getAccount(String accountId);
    
    AccountListResponseDTO getAccounts(AccountListRequestDTO request);
}

