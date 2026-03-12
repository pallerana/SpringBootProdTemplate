package com.example.account.service.account.impl;

import com.example.account.dto.account.request.AccountUpdateRequestDTO;
import com.example.account.dto.account.response.AccountUpdateResponseDTO;
import com.example.account.exception.AccountNotFoundException;
import com.example.account.model.account.AccountEntity;
import com.example.account.repository.AccountRepository;
import com.example.account.validation.AccountValidator;
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
 * Unit tests for AccountUpdateService.
 */
@ExtendWith(MockitoExtension.class)
class AccountUpdateServiceTest {

    private static final String TEST_ACCOUNT_ID = "ACC-000001";

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountValidator accountValidator;

    @InjectMocks
    private AccountUpdateService accountUpdateService;

    @Test
    void testUpdateAccount_Success() {
        // Arrange
        AccountUpdateRequestDTO request = AccountUpdateRequestDTO.builder()
                .accountName("Updated Account")
                .email("updated@example.com")
                .build();

        AccountEntity existingAccount = AccountEntity.builder()
                .accountId(TEST_ACCOUNT_ID)
                .accountName("Original Account")
                .email("original@example.com")
                .build();
        existingAccount.setId(1L);

        when(accountRepository.findByAccountId(TEST_ACCOUNT_ID))
                .thenReturn(Optional.of(existingAccount));
        when(accountRepository.save(any(AccountEntity.class))).thenReturn(existingAccount);
        doNothing().when(accountValidator).validateUpdateAccount(any(AccountUpdateRequestDTO.class));

        // Act
        AccountUpdateResponseDTO result = accountUpdateService.updateAccount(TEST_ACCOUNT_ID, request);

        // Assert
        assertNotNull(result);
        assertEquals(TEST_ACCOUNT_ID, result.getAccountId());
        verify(accountRepository).findByAccountId(TEST_ACCOUNT_ID);
        verify(accountRepository).save(any(AccountEntity.class));
    }

    @Test
    void testUpdateAccount_NotFound() {
        // Arrange
        AccountUpdateRequestDTO request = AccountUpdateRequestDTO.builder()
                .accountName("Updated Account")
                .build();

        when(accountRepository.findByAccountId(TEST_ACCOUNT_ID))
                .thenReturn(Optional.empty());
        doNothing().when(accountValidator).validateUpdateAccount(any(AccountUpdateRequestDTO.class));

        // Act & Assert
        assertThrows(AccountNotFoundException.class, () -> 
                accountUpdateService.updateAccount(TEST_ACCOUNT_ID, request));

        verify(accountRepository).findByAccountId(TEST_ACCOUNT_ID);
        verify(accountRepository, never()).save(any(AccountEntity.class));
    }

    @Test
    void testPatchAccount_Success() {
        // Arrange
        AccountUpdateRequestDTO request = AccountUpdateRequestDTO.builder()
                .accountName("Patched Account")
                .build();

        AccountEntity existingAccount = AccountEntity.builder()
                .accountId(TEST_ACCOUNT_ID)
                .accountName("Original Account")
                .email("original@example.com")
                .build();
        existingAccount.setId(1L);

        when(accountRepository.findByAccountId(TEST_ACCOUNT_ID))
                .thenReturn(Optional.of(existingAccount));
        when(accountRepository.save(any(AccountEntity.class))).thenReturn(existingAccount);
        doNothing().when(accountValidator).validateUpdateAccount(any(AccountUpdateRequestDTO.class));

        // Act
        AccountUpdateResponseDTO result = accountUpdateService.patchAccount(TEST_ACCOUNT_ID, request);

        // Assert
        assertNotNull(result);
        assertEquals(TEST_ACCOUNT_ID, result.getAccountId());
        verify(accountRepository).save(any(AccountEntity.class));
    }

    @Test
    void testPatchAccount_PartialUpdate() {
        // Arrange
        AccountUpdateRequestDTO request = AccountUpdateRequestDTO.builder()
                .email("newemail@example.com")
                .build(); // Only email, not accountName

        AccountEntity existingAccount = AccountEntity.builder()
                .accountId(TEST_ACCOUNT_ID)
                .accountName("Original Account")
                .email("original@example.com")
                .build();
        existingAccount.setId(1L);

        when(accountRepository.findByAccountId(TEST_ACCOUNT_ID))
                .thenReturn(Optional.of(existingAccount));
        when(accountRepository.save(any(AccountEntity.class))).thenReturn(existingAccount);
        doNothing().when(accountValidator).validateUpdateAccount(any(AccountUpdateRequestDTO.class));

        // Act
        AccountUpdateResponseDTO result = accountUpdateService.patchAccount(TEST_ACCOUNT_ID, request);

        // Assert
        assertNotNull(result);
        verify(accountRepository).save(any(AccountEntity.class));
    }

    @Test
    void testPatchAccount_UpdateAllFields() {
        // Arrange
        AccountUpdateRequestDTO request = AccountUpdateRequestDTO.builder()
                .accountName("Updated Account")
                .email("updated@example.com")
                .countryCode("USA")
                .currencyCode("USD")
                .website("https://www.example.com")
                .country("United States")
                .addressLine1("123 Main St")
                .addressLine2("Suite 100")
                .city("New York")
                .state("NY")
                .zipcode("10001")
                .status("ACTIVE")
                .build();

        AccountEntity existingAccount = AccountEntity.builder()
                .accountId(TEST_ACCOUNT_ID)
                .accountName("Original Account")
                .email("original@example.com")
                .build();
        existingAccount.setId(1L);

        when(accountRepository.findByAccountId(TEST_ACCOUNT_ID))
                .thenReturn(Optional.of(existingAccount));
        when(accountRepository.save(any(AccountEntity.class))).thenReturn(existingAccount);
        doNothing().when(accountValidator).validateUpdateAccount(any(AccountUpdateRequestDTO.class));

        // Act
        AccountUpdateResponseDTO result = accountUpdateService.patchAccount(TEST_ACCOUNT_ID, request);

        // Assert
        assertNotNull(result);
        verify(accountRepository).save(any(AccountEntity.class));
    }

    @Test
    void testPatchAccount_NoFieldsToUpdate() {
        // Arrange
        AccountUpdateRequestDTO request = AccountUpdateRequestDTO.builder()
                .build(); // Empty request

        AccountEntity existingAccount = AccountEntity.builder()
                .accountId(TEST_ACCOUNT_ID)
                .accountName("Original Account")
                .email("original@example.com")
                .build();
        existingAccount.setId(1L);

        when(accountRepository.findByAccountId(TEST_ACCOUNT_ID))
                .thenReturn(Optional.of(existingAccount));
        when(accountRepository.save(any(AccountEntity.class))).thenReturn(existingAccount);
        doNothing().when(accountValidator).validateUpdateAccount(any(AccountUpdateRequestDTO.class));

        // Act
        AccountUpdateResponseDTO result = accountUpdateService.patchAccount(TEST_ACCOUNT_ID, request);

        // Assert
        assertNotNull(result);
        verify(accountRepository).save(any(AccountEntity.class));
    }

    @Test
    void testUpdateAccount_AllFields() {
        // Arrange
        AccountUpdateRequestDTO request = AccountUpdateRequestDTO.builder()
                .accountName("Updated Account")
                .email("updated@example.com")
                .countryCode("USA")
                .currencyCode("USD")
                .website("https://www.example.com")
                .country("United States")
                .addressLine1("123 Main St")
                .addressLine2("Suite 100")
                .city("New York")
                .state("NY")
                .zipcode("10001")
                .status("ACTIVE")
                .build();

        AccountEntity existingAccount = AccountEntity.builder()
                .accountId(TEST_ACCOUNT_ID)
                .accountName("Original Account")
                .email("original@example.com")
                .build();
        existingAccount.setId(1L);

        when(accountRepository.findByAccountId(TEST_ACCOUNT_ID))
                .thenReturn(Optional.of(existingAccount));
        when(accountRepository.save(any(AccountEntity.class))).thenReturn(existingAccount);
        doNothing().when(accountValidator).validateUpdateAccount(any(AccountUpdateRequestDTO.class));

        // Act
        AccountUpdateResponseDTO result = accountUpdateService.updateAccount(TEST_ACCOUNT_ID, request);

        // Assert
        assertNotNull(result);
        verify(accountRepository).save(any(AccountEntity.class));
    }
}

