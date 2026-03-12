package com.example.account.service.account;

import com.example.account.dto.account.request.AccountUpdateRequestDTO;
import com.example.account.dto.account.response.AccountUpdateResponseDTO;

/**
 * Service interface for account update operations.
 */
public interface IAccountUpdateService {
    
    AccountUpdateResponseDTO updateAccount(String accountId, AccountUpdateRequestDTO request);
    
    AccountUpdateResponseDTO patchAccount(String accountId, AccountUpdateRequestDTO request);
}

