package com.example.account.service.account.impl;

import com.example.account.dto.account.request.AccountCreateRequestDTO;
import com.example.account.dto.account.response.AccountCreateResponseDTO;
import com.example.account.mapper.AccountMapper;
import com.example.account.model.account.AccountEntity;
import com.example.account.repository.AccountRepository;
import com.example.account.service.account.ICreateAccountService;
import com.example.account.util.AccountUtil;
import com.example.account.validation.AccountValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation for account creation operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CreateAccountService implements ICreateAccountService {
    
    private final AccountRepository accountRepository;
    private final AccountValidator accountValidator;
    private final AccountMapper accountMapper = AccountMapper.INSTANCE;
    
    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public AccountCreateResponseDTO createAccount(AccountCreateRequestDTO request) {
        log.info("Creating account with name: {}", request.getAccountName());
        
        // Validate request
        accountValidator.validateCreateAccount(request);
        
        // Generate unique account ID
        String accountId = AccountUtil.generateAccountId();
        
        // Build entity
        AccountEntity accountEntity = AccountEntity.builder()
                .accountId(accountId)
                .accountName(request.getAccountName())
                .email(request.getEmail())
                .countryCode(StringUtils.isNotBlank(request.getCountryCode()) ? request.getCountryCode() : null)
                .currency(StringUtils.isNotBlank(request.getCurrencyCode()) ? request.getCurrencyCode() : null)
                .website(StringUtils.isNotBlank(request.getWebsite()) ? request.getWebsite() : null)
                .country(StringUtils.isNotBlank(request.getCountry()) ? request.getCountry() : null)
                .addressLine1(StringUtils.isNotBlank(request.getAddressLine1()) ? request.getAddressLine1() : null)
                .addressLine2(StringUtils.isNotBlank(request.getAddressLine2()) ? request.getAddressLine2() : null)
                .city(StringUtils.isNotBlank(request.getCity()) ? request.getCity() : null)
                .state(StringUtils.isNotBlank(request.getState()) ? request.getState() : null)
                .zipcode(StringUtils.isNotBlank(request.getZipcode()) ? request.getZipcode() : null)
                .build();
        
        // Initialize defaults (sets status to PENDING and timestamps)
        accountEntity.initializeDefaults();
        
        // Save account
        AccountEntity savedAccount = accountRepository.save(accountEntity);
        log.info("Account created successfully with ID: {}", accountId);
        
        return accountMapper.toCreateResponseDTO(savedAccount);
    }
}

