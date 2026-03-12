package com.example.account.service.account;

import com.example.account.dto.account.response.AccountDeleteResponseDTO;

/**
 * Service interface for account deletion operations.
 */
public interface IAccountDeleteService {
    
    AccountDeleteResponseDTO deleteAccount(String accountId);
}

