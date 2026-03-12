package com.example.account.service.account.impl;

import com.example.account.dto.account.request.AccountUpdateRequestDTO;
import com.example.account.dto.account.response.AccountUpdateResponseDTO;
import com.example.account.exception.AccountNotFoundException;
import com.example.account.model.account.AccountEntity;
import com.example.account.repository.AccountRepository;
import com.example.account.service.account.IAccountUpdateService;
import com.example.account.validation.AccountValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation for account update operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountUpdateService implements IAccountUpdateService {
    
    private final AccountRepository accountRepository;
    private final AccountValidator accountValidator;
    
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public AccountUpdateResponseDTO updateAccount(String accountId, AccountUpdateRequestDTO request) {
        log.info("Updating account with ID: {}", accountId);
        
        // Validate request
        accountValidator.validateUpdateAccount(request);
        
        // Find existing account
        AccountEntity existingAccount = accountRepository.findByAccountId(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
        
        // Update fields (PUT - full update)
        existingAccount.setAccountName(StringUtils.isNotBlank(request.getAccountName()) ? request.getAccountName() : existingAccount.getAccountName());
        existingAccount.setEmail(StringUtils.isNotBlank(request.getEmail()) ? request.getEmail() : existingAccount.getEmail());
        existingAccount.setCountryCode(StringUtils.isNotBlank(request.getCountryCode()) ? request.getCountryCode() : existingAccount.getCountryCode());
        existingAccount.setCurrency(StringUtils.isNotBlank(request.getCurrencyCode()) ? request.getCurrencyCode() : existingAccount.getCurrency());
        existingAccount.setWebsite(StringUtils.isNotBlank(request.getWebsite()) ? request.getWebsite() : existingAccount.getWebsite());
        existingAccount.setCountry(StringUtils.isNotBlank(request.getCountry()) ? request.getCountry() : existingAccount.getCountry());
        existingAccount.setAddressLine1(StringUtils.isNotBlank(request.getAddressLine1()) ? request.getAddressLine1() : existingAccount.getAddressLine1());
        existingAccount.setAddressLine2(StringUtils.isNotBlank(request.getAddressLine2()) ? request.getAddressLine2() : existingAccount.getAddressLine2());
        existingAccount.setCity(StringUtils.isNotBlank(request.getCity()) ? request.getCity() : existingAccount.getCity());
        existingAccount.setState(StringUtils.isNotBlank(request.getState()) ? request.getState() : existingAccount.getState());
        existingAccount.setZipcode(StringUtils.isNotBlank(request.getZipcode()) ? request.getZipcode() : existingAccount.getZipcode());
        existingAccount.setStatus(StringUtils.isNotBlank(request.getStatus()) ? request.getStatus() : existingAccount.getStatus());
        
        // Update timestamp
        existingAccount.updateTimestamp();
        
        // Save account
        AccountEntity savedAccount = accountRepository.save(existingAccount);
        log.info("Account updated successfully with ID: {}", accountId);
        
        return AccountUpdateResponseDTO.builder()
                .accountId(savedAccount.getAccountId())
                .accountName(savedAccount.getAccountName())
                .status(savedAccount.getStatus())
                .build();
    }
    
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public AccountUpdateResponseDTO patchAccount(String accountId, AccountUpdateRequestDTO request) {
        log.info("Patching account with ID: {}", accountId);
        
        // Validate request
        accountValidator.validateUpdateAccount(request);
        
        // Find existing account
        AccountEntity existingAccount = accountRepository.findByAccountId(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
        
        // Update only non-null fields (PATCH - partial update)
        if (StringUtils.isNotBlank(request.getAccountName())) {
            existingAccount.setAccountName(request.getAccountName());
        }
        if (StringUtils.isNotBlank(request.getEmail())) {
            existingAccount.setEmail(request.getEmail());
        }
        if (StringUtils.isNotBlank(request.getCountryCode())) {
            existingAccount.setCountryCode(request.getCountryCode());
        }
        if (StringUtils.isNotBlank(request.getCurrencyCode())) {
            existingAccount.setCurrency(request.getCurrencyCode());
        }
        if (StringUtils.isNotBlank(request.getWebsite())) {
            existingAccount.setWebsite(request.getWebsite());
        }
        if (StringUtils.isNotBlank(request.getCountry())) {
            existingAccount.setCountry(request.getCountry());
        }
        if (StringUtils.isNotBlank(request.getAddressLine1())) {
            existingAccount.setAddressLine1(request.getAddressLine1());
        }
        if (StringUtils.isNotBlank(request.getAddressLine2())) {
            existingAccount.setAddressLine2(request.getAddressLine2());
        }
        if (StringUtils.isNotBlank(request.getCity())) {
            existingAccount.setCity(request.getCity());
        }
        if (StringUtils.isNotBlank(request.getState())) {
            existingAccount.setState(request.getState());
        }
        if (StringUtils.isNotBlank(request.getZipcode())) {
            existingAccount.setZipcode(request.getZipcode());
        }
        if (StringUtils.isNotBlank(request.getStatus())) {
            existingAccount.setStatus(request.getStatus());
        }
        
        // Update timestamp
        existingAccount.updateTimestamp();
        
        // Save account
        AccountEntity savedAccount = accountRepository.save(existingAccount);
        log.info("Account patched successfully with ID: {}", accountId);
        
        return AccountUpdateResponseDTO.builder()
                .accountId(savedAccount.getAccountId())
                .accountName(savedAccount.getAccountName())
                .status(savedAccount.getStatus())
                .build();
    }
}

