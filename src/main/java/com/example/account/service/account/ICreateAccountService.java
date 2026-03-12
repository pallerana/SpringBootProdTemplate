package com.example.account.service.account;

import com.example.account.dto.account.request.AccountCreateRequestDTO;
import com.example.account.dto.account.response.AccountCreateResponseDTO;

/**
 * Service interface for account creation operations.
 */
public interface ICreateAccountService {
    
    AccountCreateResponseDTO createAccount(AccountCreateRequestDTO request);
}

