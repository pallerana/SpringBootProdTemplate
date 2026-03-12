package com.example.account.validation;

import com.example.account.dto.account.request.AccountCreateRequestDTO;
import com.example.account.dto.account.request.AccountUpdateRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Account validator for business logic validation.
 * This is a service-level validator, not a constraint validator.
 * For constraint validation, use @Valid and standard validation annotations.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AccountValidator {
    
    /**
     * Validate account creation request.
     */
    public void validateCreateAccount(AccountCreateRequestDTO request) {
        log.debug("Validating account creation request");
        // Add specific validation logic here
        // Example: Check for duplicate accounts, validate business rules, etc.
    }
    
    /**
     * Validate account update request.
     */
    public void validateUpdateAccount(AccountUpdateRequestDTO request) {
        log.debug("Validating account update request");
        // Add specific validation logic here
    }
}

