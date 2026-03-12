package com.example.account.exception;

/**
 * Exception thrown when an account is not found.
 */
public class AccountNotFoundException extends RuntimeException {
    
    private final String accountId;
    
    public AccountNotFoundException(String accountId) {
        super(accountId);
        this.accountId = accountId;
    }
    
    public String getAccountId() {
        return accountId;
    }
}

