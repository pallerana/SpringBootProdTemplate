package com.example.account.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Transaction prefix enum for generating unique reference IDs.
 */
@Getter
@AllArgsConstructor
public enum TransactionPrefixEnum {
    ACCOUNT("ACC-");

    private final String value;
}

