package com.example.account.service.account.impl;

import com.example.account.dto.account.response.AccountDeleteResponseDTO;
import com.example.account.exception.AccountNotFoundException;
import com.example.account.model.account.AccountEntity;
import com.example.account.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AccountDeleteService.
 */
@ExtendWith(MockitoExtension.class)
class AccountDeleteServiceTest {

    private static final String TEST_ACCOUNT_ID = "ACC-000001";

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountDeleteService accountDeleteService;

    @Test
    void testDeleteAccount_Success() {
        // Arrange
        AccountEntity accountEntity = AccountEntity.builder()
                .accountId(TEST_ACCOUNT_ID)
                .accountName("Test Account")
                .build();
        accountEntity.setId(1L);

        when(accountRepository.findByAccountId(TEST_ACCOUNT_ID))
                .thenReturn(Optional.of(accountEntity));
        doNothing().when(accountRepository).delete(any(AccountEntity.class));

        // Act
        AccountDeleteResponseDTO result = accountDeleteService.deleteAccount(TEST_ACCOUNT_ID);

        // Assert
        assertNotNull(result);
        assertEquals(TEST_ACCOUNT_ID, result.getAccountId());
        assertTrue(result.isDeleted());
        verify(accountRepository).findByAccountId(TEST_ACCOUNT_ID);
        verify(accountRepository).delete(accountEntity);
    }

    @Test
    void testDeleteAccount_NotFound() {
        // Arrange
        when(accountRepository.findByAccountId(TEST_ACCOUNT_ID))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(AccountNotFoundException.class, () -> 
                accountDeleteService.deleteAccount(TEST_ACCOUNT_ID));

        verify(accountRepository).findByAccountId(TEST_ACCOUNT_ID);
        verify(accountRepository, never()).delete(any(AccountEntity.class));
    }
}

